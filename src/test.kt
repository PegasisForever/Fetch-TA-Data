import models.Course
import modes.server.parsers.CourseListParser.Companion.parseCourseList
import modes.server.parsers.TimeLineParser.Companion.parseTimeLine
import modes.server.serializers.CourseListSerializerV2.Companion.serializeCourseList
import modes.server.serializers.TimeLineSerializerV2.Companion.serializeTimeLine
import modes.server.timeline.compareCourseList
import java.io.File

fun main() {
    val studentNumber="349891234"
    val fileNames = ArrayList<String>()
    val dir = File("./ta-archive/")
    dir.listFiles()?.forEach { file ->
        if (file.name.indexOf("$studentNumber-summary") != -1) {
            fileNames.add(file.name)
        }
    }
    fileNames.sort()


    for (i in 0 until fileNames.size) {
        val zdt = fileNames[i].substring(0, 13).toLong().toZonedDateTime()

        var oldCourseList:ArrayList<Course>?=null
        try{
            oldCourseList=parseCourseList(readFile("data/courselists/$studentNumber.json"))
        }catch (e:Exception){
            println("cannot read old course list")
        }

        val newCourseList = readLocalHtml(fileNames[i], zdt)

        if (oldCourseList==null){
            serializeCourseList(newCourseList).writeToFile("data/courselists/$studentNumber.json")
            "[]".writeToFile("data/timelines/$studentNumber.json")
        }else{
            val timeline=parseTimeLine(readFile("data/timelines/$studentNumber.json"))
            val updates=compareCourseList(oldCourseList,newCourseList,zdt)
            timeline.addAll(updates)

            serializeCourseList(newCourseList).writeToFile("data/courselists/$studentNumber.json")
            serializeTimeLine(timeline).writeToFile("data/timelines/$studentNumber.json")
        }
    }

}