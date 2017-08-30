package id.ac.ugm.chiliquality

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.prediction_card.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"
    val REQUEST_IMAGE_CAPTURE = 1

    var currentPhotoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { _ ->
            dispatchTakePictureIntent()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Take picture with other camera application.
     */
    private fun dispatchTakePictureIntent() {
        val takePictureIntent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
            }

            if (photoFile != null) {
                val photoUri = FileProvider.getUriForFile(
                        this,
                        "id.ac.ugm.chiliquality.file_provider",
                        photoFile)

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
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "CHILI_" + timeStamp
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)

        currentPhotoPath = image.absolutePath
        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            setPicture()
        }
    }

    /**
     * Resize picture and set it on prediction_image ImageView.
     *
     * The image's width is resized to match the ImageView, and the height is resized to match
     * the aspect ratio.
     */
    private fun setPicture() {
        val targetWidth = prediction_image.width
        val targetHeight: Int = (targetWidth / 16) * 9

        Log.i(TAG, "Target width: " + targetWidth.toString())
        Log.i(TAG, "Target height: " + targetHeight.toString())

        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)

        val photoWidth = bmOptions.outWidth
        val photoHeight = bmOptions.outHeight

        val scaleFactor = Math.min(photoWidth / targetWidth, photoHeight / targetHeight)

        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor

        val bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
        prediction_image.setImageBitmap(bitmap)
        prediction_quality.text = "Mutu 1";
    }
}
