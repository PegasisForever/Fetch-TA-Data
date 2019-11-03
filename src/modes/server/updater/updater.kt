package modes.server.updater

import log
import models.Course
import models.User
import modes.server.parsers.CourseListParser
import modes.server.parsers.TimeLineParser
import modes.server.sendFCM
import modes.server.serializers.CourseListSerializerV3.Companion.serializeCourseList
import modes.server.serializers.TimeLineSerializerV3.Companion.serializeTimeLine
import modes.server.timeline.AssignmentAdded
import modes.server.timeline.TAUpdate
import modes.server.timeline.compareCourseList
import readFile
import webpage.LoginPage
import writeToFile
import java.util.concurrent.atomic.AtomicBoolean

//updates, courselist, timeline
fun performUpdate(user: User, newData: ArrayList<Course>? = null): HashMap<String,Any> {
    val studentNumber = user.number
    val password = user.password
    var updates = ArrayList<TAUpdate>()
    var newCourseList=ArrayList<Course>()
    var timeline= ArrayList<TAUpdate>()

    var oldCourseList: ArrayList<Course>? = null
    try {
        oldCourseList = CourseListParser.parseCourseList(readFile("data/courselists/$studentNumber.json"))
    } catch (ignored: Exception) {
    }

    try {
        newCourseList = newData ?: LoginPage().gotoSummaryPage(studentNumber, password).fillDetails().courses
        if (oldCourseList == null) {
            serializeCourseList(newCourseList).writeToFile("data/courselists/$studentNumber.json")
            "[]".writeToFile("data/timelines/$studentNumber.json")
        } else {
            timeline = TimeLineParser.parseTimeLine(readFile("data/timelines/$studentNumber.json"))
            updates = compareCourseList(oldCourseList, newCourseList)
            timeline.addAll(updates)

            serializeCourseList(newCourseList).writeToFile("data/courselists/$studentNumber.json")
            serializeTimeLine(timeline).writeToFile("data/timelines/$studentNumber.json")
        }

        sendNotifications(user, updates)
    } catch (e: Exception) {
        log(LogLevel.ERROR, "Error while performing update for user ${studentNumber}", e)
    }
    return hashMapOf("updates" to updates,"courselist" to newCourseList,"timeline" to timeline)
}

//courselist, timeline
fun runFollowUpUpdate(number: String, newData: ArrayList<Course>, hash: Int, routeName: String): HashMap<String,Any> {
    var courseList=newData
    var timeline= ArrayList<TAUpdate>()
    val user = User.get(number)
    user?.let {
        val out= performUpdate(it, newData)
        courseList=out["courselist"] as ArrayList<Course>
        timeline=out["timeline"] as ArrayList<TAUpdate>
    }
    log(LogLevel.INFO, "Request #$hash ${routeName} :: Follow up update done")
    return hashMapOf("courselist" to courseList,"timeline" to timeline)
}

fun sendNotifications(user: User, updateList: ArrayList<TAUpdate>) {
    updateList.forEach { taUpdate ->
        when (taUpdate) {
            is AssignmentAdded -> {
                user.devices.forEach { device ->
                    if (device.receive && device.token != "") {
                        NotificationStrings.getAssignmentAddedNoti(device.language, taUpdate)?.let {
                            val deviceExists = sendFCM(device.token, it)
                            if (!deviceExists){
                                User.removeToken(device.token)
                            }
                        }
                    }
                }
            }
        }
    }
}

var autoUpdateThreadRunning = AtomicBoolean(false)
fun startAutoUpdateThread(intervalMinute: Int): Thread {
    val interval = intervalMinute * 60 * 1000
    val thread = Thread {
        autoUpdateThreadRunning.set(true)
        log(LogLevel.INFO, "Auto update thread started")
        while (autoUpdateThreadRunning.get()) {
            val startTime = System.currentTimeMillis()
            User.allUsers.forEach { user ->
                val updates = performUpdate(user)["updates"] as ArrayList<TAUpdate>
                log(LogLevel.INFO, "Performed update for user ${user.number}, ${updates.size} updates")
            }

            val remainTime = interval - (System.currentTimeMillis() - startTime)
            try {
                Thread.sleep(remainTime)
            } catch (e: InterruptedException) {
                log(LogLevel.INFO, "Thread interrupted")
            }
        }
        log(LogLevel.INFO, "Thread stopped")
    }
    thread.start()

    return thread
}