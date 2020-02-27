package site.pegasis.ta.fetch.tools

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

fun String.writeToFile(path: String) {
    val file = File(path)
    file.parentFile.mkdirs()
    file.writeText(this)
}

fun String.appendToFile(path: String) {
    val file = File(path)
    file.parentFile.mkdirs()
    file.appendText(this)
}

fun readFile(path: String): String {
    return String(Files.readAllBytes(Paths.get(path)))
}

fun isFileExists(path: String): Boolean {
    return File(path).isFile
}

fun fileExists(path: String): Boolean {
    val tmpDir = File(path)
    return tmpDir.exists()
}

fun readFile(file: File): String {
    return file.readText()
}