package id.ac.ugm.chiliquality

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import id.ac.ugm.chiliquality.imgproc.createSegment
import id.ac.ugm.chiliquality.imgproc.featureExtraction

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.Utils.bitmapToMat
import org.opencv.core.Mat
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils.matToBitmap


class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_IMAGE_CAPTURE = 1

        private const val MAX_IMAGE_SIZE = 200
    }

    private var currentPhotoPath: String? = null

    private val loaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i("OpenCV", "OpenCV loaded successfully")
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { _ ->
            dispatchTakePictureIntent()
        }
    }

    public override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, loaderCallback)
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!")
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Take picture with other camera application.
     */
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (takePictureIntent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null

            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
            }

            if (photoFile != null) {
                val photoUri = FileProvider.getUriForFile(
                        this, "id.ac.ugm.chiliquality.file_provider", photoFile)

                Log.i(TAG, "Saving image to: " + currentPhotoPath)

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    /**
     * Create image file to store the image from camera.
     */
    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "CHILI_" + timestamp
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)

        currentPhotoPath = image.absolutePath

        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            processImage()
        }
    }

    private fun processImage() {
        val bitmap = openAndResize()

        val image = Mat()
        bitmapToMat(bitmap, image)

        val segmented = createSegment(image)
        val features = featureExtraction(segmented)

        Log.i(TAG, String.format("Red: %d", features.red))
        Log.i(TAG, String.format("Green: %d", features.green))
        Log.i(TAG, String.format("Blue: %d", features.blue))

        matToBitmap(segmented.image, bitmap)
        image_result.setImageBitmap(bitmap)

        red_value.text = features.red.toString()
        green_value.text = features.green.toString()
        blue_value.text = features.blue.toString()
        vitamin_c_value.text = vitaminC(features).toString()
        carotene_value.text = carotene(features).toString()
    }

    private fun openAndResize(): Bitmap {
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)

        val photoWidth = bmOptions.outWidth
        val photoHeight = bmOptions.outHeight

        val scaleFactor = if (photoWidth > photoHeight) {
            photoWidth / MAX_IMAGE_SIZE
        } else {
            photoHeight / MAX_IMAGE_SIZE
        }

        Log.i(TAG, String.format("Scale factor: %d", scaleFactor))

        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor

        return BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
    }
}
