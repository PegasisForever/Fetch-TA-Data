package modes.server.updater

import models.Course
import models.User
import modes.server.parsers.CourseListParser
import modes.server.parsers.TimeLineParser
import modes.server.serializers.CourseListSerializerV2.Companion.serializeCourseList
import modes.server.serializers.TimeLineSerializerV2.Companion.serializeTimeLine
import modes.server.timeline.TAUpdate
import modes.server.timeline.compareCourseList
import readFile
import webpage.LoginPage
import writeToFile

fun performUpdate(user: User, newData: ArrayList<Course>? = null): ArrayList<TAUpdate> {
    val studentNumber = user.number
    val password = user.password
    var updates = ArrayList<TAUpdate>()

    var oldCourseList: ArrayList<Course>? = null
    try {
        oldCourseList = CourseListParser.parseCourseList(readFile("data/courselists/$studentNumber.json"))
    } catch (ignored: Exception) {
    }

    val newCourseList = newData ?: LoginPage().gotoSummaryPage(studentNumber, password).fillDetails().courses
    if (oldCourseList == null) {
        serializeCourseList(newCourseList).writeToFile("data/courselists/$studentNumber.json")
        "[]".writeToFile("data/timelines/$studentNumber.json")
    } else {
        val timeline = TimeLineParser.parseTimeLine(readFile("data/timelines/$studentNumber.json"))
        updates = compareCourseList(oldCourseList, newCourseList)
        timeline.addAll(updates)

        serializeCourseList(newCourseList).writeToFile("data/courselists/$studentNumber.json")
        serializeTimeLine(timeline).writeToFile("data/timelines/$studentNumber.json")
    }

    return updates
}

fun sendNotifications(user: User,updateList:ArrayList<TAUpdate>){
    //TODO
}

fun startAutoUpdateProcess(){

}