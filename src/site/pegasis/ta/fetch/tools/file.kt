package site.pegasis.ta.fetch.tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

suspend fun String.writeToFile(path: String) = withContext(Dispatchers.IO) {
    val file = File(path)
    file.parentFile?.mkdirs()
    val stream = FileOutputStream(file)
    stream.write(this@writeToFile.toByteArray(Charsets.UTF_8))
    stream.close()
}

suspend fun String.appendToFile(path: String) = withContext(Dispatchers.IO) {
    val file = File(path)
    file.parentFile?.mkdirs()
    val stream = FileOutputStream(file, true)
    stream.write(this@appendToFile.toByteArray(Charsets.UTF_8))
    stream.close()
}

suspend fun readFile(path: String) = withContext(Dispatchers.IO) {
    val file = File(path)
    val stream=file.inputStream()
    val text = stream.readBytes().toString(Charsets.UTF_8)
    stream.close()
    text
}

suspend fun fileExists(path: String) = withContext(Dispatchers.IO) {
    val tmpDir = File(path)
    tmpDir.exists()
}
