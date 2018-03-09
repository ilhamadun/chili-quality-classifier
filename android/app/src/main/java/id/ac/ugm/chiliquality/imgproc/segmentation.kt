/**
 * Copyright 2017 Ilham Imaduddin. All rights reserved.
 * Use of this source code is governed by MIT license that can be
 * found in the LICENSE file.
 */

package id.ac.ugm.chiliquality.imgproc

import android.util.Log
import org.opencv.core.*
import org.opencv.core.Core.*
import org.opencv.imgproc.Imgproc.*

data class Segmented(val image: Mat, val mask: Mat, val contours: List<MatOfPoint>)

fun createSegment(image: Mat): Segmented {
//    val edgeMask = edgeDetection(image)
//    val edgeContours = findSignificantContour(edgeMask, 0.05)
//    val edgeImage = subtractImageByContours(image, edgeContours)

    val colorMask = colorThreshold(image)
    val colorContours = findSignificantContour(colorMask, 0.03)
    val colorImage = subtractImageByContours(image, colorContours)

    return Segmented(colorImage, colorMask, colorContours)
}

private fun edgeDetection(image: Mat): Mat {
    val processedImage = Mat()
    GaussianBlur(image, processedImage, Size(3.0, 3.0), 0.0)

    val channels = mutableListOf<Mat>()
    split(image, channels)

    channels.indices
            .forEach { channels[it] = sobel(channels[it]) }

    val edges: Mat = maxEdges(channels)
    val meanEdge: Scalar = mean(edges)
    val minThreshold: Double = meanEdge.`val`[0]

    threshold(edges, edges, minThreshold, 255.0, THRESH_BINARY)

    return edges
}

private fun sobel(channel: Mat): Mat {
    val edgeX = Mat()
    Sobel(channel, edgeX, CvType.CV_16S, 1, 0)

    val edgeY = Mat()
    Sobel(channel, edgeY, CvType.CV_16S, 0, 1)

    val absEdgeX = Mat()
    convertScaleAbs(edgeX, absEdgeX)

    val absEdgeY = Mat()
    convertScaleAbs(edgeY, absEdgeY)

    val gradient = Mat()
    addWeighted(absEdgeX, 0.5, absEdgeY, 0.5, 0.0, gradient)

    return gradient
}

private fun maxEdges(channels: List<Mat>): Mat {
    val edges = Mat(channels[0].size(), CvType.CV_8S)
    val ch0Size = channels[0].size()
    Log.i("Segmentation", String.format("Ch0: %f %f %d", ch0Size.height, ch0Size.width, channels[0].channels()))

    val ch1Size = channels[1].size()
    Log.i("Segmentation", String.format("Ch1: %f %f %d", ch1Size.height, ch1Size.width, channels[1].channels()))

    val ch2Size = channels[2].size()
    Log.i("Segmentation", String.format("Ch2: %f %f %d", ch2Size.height, ch2Size.width, channels[2].channels()))

    Log.i("Segmentation", String.format("Edges: %f %f %d", edges.size().height, edges.size().width, edges.channels()))

//    max(edges, edges, edges)
//    max(channels[2], edges, edges)

    return channels[2]
}

private fun colorThreshold(image: Mat): Mat {
    val hsv = Mat()
    cvtColor(image, hsv, COLOR_BGR2HSV)

//    val greenMask = Mat()
//    inRange(hsv, Scalar(30.0, 20.0, 0.0), Scalar(70.0, 255.0, 255.0),
//            greenMask)
//
//    val lowerRedMask = Mat()
//    inRange(hsv, Scalar(0.0, 80.0, 20.0), Scalar(30.0, 255.0, 230.0),
//            lowerRedMask)
//
//    val upperRedMask = Mat()
//    inRange(hsv, Scalar(160.0, 20.0, 5.0), Scalar(180.0, 255.0, 255.0),
//            upperRedMask)

//    val total = Mat()
//    add(greenMask, lowerRedMask, total)
//    add(total, upperRedMask, total)

    val mask = Mat()
    inRange(hsv, Scalar(0.0, 100.0, 0.0), Scalar(255.0, 255.0, 240.0),
            mask)

    return mask
}