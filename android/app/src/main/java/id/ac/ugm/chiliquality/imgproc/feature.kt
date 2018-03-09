/**
 * Copyright 2017 Ilham Imaduddin. All rights reserved.
 * Use of this source code is governed by MIT license that can be
 * found in the LICENSE file.
 */

package id.ac.ugm.chiliquality.imgproc

import org.opencv.core.Core.mean
import org.opencv.imgproc.Imgproc.contourArea

data class Descriptor(val green: Int, val blue: Int,  val red: Int, val area: Double)

fun featureExtraction(segmented: Segmented): Descriptor {
    val meanColor = mean(segmented.image, segmented.mask)
    val red = meanColor.`val`[0].toInt()
    val green = meanColor.`val`[1].toInt()
    val blue = meanColor.`val`[2].toInt()

    val area = segmented.contours.sumByDouble { contourArea(it) }

    return Descriptor(green, blue, red, area)
}
