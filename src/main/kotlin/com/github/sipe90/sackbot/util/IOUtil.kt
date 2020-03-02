package com.github.sipe90.sackbot.util

fun stripExtension(fileName: String): String {
    val last = fileName.lastIndexOf('.')
    return if (last > 0) fileName.substring(0, last) else fileName
}