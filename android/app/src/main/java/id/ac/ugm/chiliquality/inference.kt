package id.ac.ugm.chiliquality

import id.ac.ugm.chiliquality.imgproc.Descriptor

fun vitaminC(features: Descriptor): Int {
    val red = 0.713 * features.red
    val green = 1.774 * features.green
    val blue = 2.569 * features.blue

    return (112.304 + red - green + blue).toInt()
}

fun carotene(features: Descriptor): Int {
    val red = 1.238 * features.red
    val green = 8.033 * features.green
    val blue = 3.894 * features.blue

    return (310.983 - red - green + blue).toInt()
}
