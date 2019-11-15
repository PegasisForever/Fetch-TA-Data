import modes.server.parsers.LegacyCourseListParser
import modes.server.parsers.LegacyTimeLineParser
import modes.server.serializers.serialize
import java.nio.file.Files
import java.nio.file.Paths

fun convertLegacy() {
    Files.walk(Paths.get("data/courselists"))
        .map { it.toFile() }.filter { it.isFile }
        .forEach { file ->
            val text = readFile(file)
            val courseList = LegacyCourseListParser.parseCourseList(text)
            courseList.serialize().toJSONString().writeToFile(file.absolutePath)
        }


    Files.walk(Paths.get("data/timelines"))
        .map { it.toFile() }.filter { it.isFile }
        .forEach { file ->
            val text = readFile(file)
            val timeLine = LegacyTimeLineParser.parseTimeLine(text)
            timeLine.serialize().toJSONString().writeToFile(file.absolutePath)
        }

    println("Done")
}