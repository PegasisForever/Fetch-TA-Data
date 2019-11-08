package modes.server

import modes.server.serializers.latestApiVersion
import readFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths


fun convertOldFormat() {

    val courseListVersion = readFile("data/version.txt").toInt()
    if (courseListVersion < latestApiVersion) {
        val walk = Files.walk(Paths.get("data/courselists"))
        val files = walk.map { it.toFile() }.filter { it.isFile }
        files.forEach { file ->
            println(file.readText())
        }
    }
}