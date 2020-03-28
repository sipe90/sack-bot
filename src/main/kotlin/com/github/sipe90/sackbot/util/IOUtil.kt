package com.github.sipe90.sackbot.util

fun getExtension(fileName: String): String? {
    val last = fileName.lastIndexOf('.')
    return if (last < 0 || last == fileName.lastIndex) null else fileName.substring(last + 1)
}

fun stripExtension(fileName: String): String {
    val last = fileName.lastIndexOf('.')
    return if (last > 0) fileName.substring(0, last) else fileName
}