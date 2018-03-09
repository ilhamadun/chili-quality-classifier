/**
 * Copyright 2017 Ilham Imaduddin. All rights reserved.
 * Use of this source code is governed by MIT license that can be
 * found in the LICENSE file.
 */

package id.ac.ugm.chiliquality.imgproc

import org.opencv.core.Core.bitwise_and
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc.*

/**
 * Detect contour from a mask and filter contour smaller than
 * `size_threshold`.
 */
fun findSignificantContour(mask: Mat, sizeThreshold: Double): List<MatOfPoint> {
    val contours = mutableListOf<MatOfPoint>()
    val hierarchy = Mat()

    findContours(mask, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE)

    return filterSmallContour(mask, contours, sizeThreshold)
}

/**
 * Filter contour smaller than `size_threshold` percent of the `mask` area.
 */
private fun filterSmallContour(mask: Mat, contours: List<MatOfPoint>, sizeThreshold: Double):
        List<MatOfPoint> {
    val significant = mutableListOf<MatOfPoint>()
    val minSize = mask.size().area() * sizeThreshold

    for (contour in contours) {
        val area = contourArea(contour)

        if (area > minSize) {
            significant.add(contour)
        }
    }

    return significant
}

/**
 * Remove image section that's not covered with the contours.
 */
fun subtractImageByContours(image: Mat, contours: List<MatOfPoint>): Mat {
    val mask = Mat(image.size(), CvType.CV_8UC1, Scalar(0.0))
    fillPoly(mask, contours, Scalar(255.0))

    val reduced = Mat()
    bitwise_and(image, image, reduced, mask)

    return reduced
}
