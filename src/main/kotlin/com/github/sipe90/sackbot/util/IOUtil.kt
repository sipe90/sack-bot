package com.github.sipe90.sackbot.util

import java.nio.file.Files
import java.nio.file.Paths

fun createParentDirs(path: String) {
    Files.createDirectories(Paths.get(path).parent)
}

fun getExtension(fileName: String): String? {
    val last = fileName.lastIndexOf('.')
    return if (last < 0 || last == fileName.lastIndex) null else fileName.substring(last + 1)
}

fun stripExtension(fileName: String): String {
    val last = fileName.lastIndexOf('.')
    return if (last > 0) fileName.substring(0, last) else fileName
}

fun withExtension(fileName: String, extension: String?): String {
    if (extension == null) return fileName
    return "$fileName.$extension"
}
