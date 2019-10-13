import modes.server.parsers.TimeLineParser.Companion.parseTimeLine
import modes.server.serializers.TimeLineSerializerV2.Companion.serializeTimeLine
import modes.server.timeline.TAUpdate
import modes.server.timeline.compareCourseList
import java.io.File

fun main() {
    val fileNames = ArrayList<String>()
    val dir = File("./ta-archive/")
    dir.listFiles()?.forEach { file ->
        if (file.name.indexOf("349891234-summary") != -1) {
            fileNames.add(file.name)
        }
    }
    fileNames.sort()


    val updateList = ArrayList<TAUpdate>()
    for (i in 0..fileNames.size - 2) {
        val zdt = fileNames[i + 1].substring(0, 13).toLong().toZonedDateTime()

        val oldCourseList = readLocalHtml(fileNames[i], zdt)
        val newCourseList = readLocalHtml(fileNames[i + 1], zdt)

        updateList.addAll(compareCourseList(oldCourseList, newCourseList, zdt))
    }

    println(serializeTimeLine(updateList))
}