package site.pegasis.ta.fetch.tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

suspend fun String.writeToFile(path: String) = withContext(Dispatchers.IO) {
    val file = File(path)
    file.parentFile.mkdirs()
    file.writeText(this@writeToFile)
}

suspend fun String.appendToFile(path: String) = withContext(Dispatchers.IO) {
    val file = File(path)
    file.parentFile.mkdirs()
    file.appendText(this@appendToFile)
}

suspend fun readFile(path: String) = withContext(Dispatchers.IO) {
    String(Files.readAllBytes(Paths.get(path)))
}


suspend fun isFileExists(path: String) = withContext(Dispatchers.IO) {
    File(path).isFile
}


suspend fun fileExists(path: String) = withContext(Dispatchers.IO) {
    val tmpDir = File(path)
    tmpDir.exists()
}

suspend fun readFile(file: File) = withContext(Dispatchers.IO) {
    file.readText()
}