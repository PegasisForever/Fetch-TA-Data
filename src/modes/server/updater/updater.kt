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

fun performUpdate(user: User, newData: ArrayList<Course>? = null): ArrayList<TAUpdate> {
    val studentNumber = user.number
    val password = user.password
    var updates = ArrayList<TAUpdate>()

    var oldCourseList: ArrayList<Course>? = null
    try {
        oldCourseList = CourseListParser.parseCourseList(readFile("data/courselists/$studentNumber.json"))
    } catch (ignored: Exception) {
    }

    try {
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

        sendNotifications(user, updates)
    } catch (e: Exception) {
        log(LogLevel.ERROR, "Error while performing update for user ${studentNumber}", e)
    }

    return updates
}

fun runFollowUpUpdate(number: String, newData: ArrayList<Course>, hash: Int, routeName: String) {
    val user = User.get(number)
    user?.let {
        performUpdate(it, newData)
    }
    log(LogLevel.INFO, "Request #$hash ${routeName} :: Follow up update done")
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
                val updates = performUpdate(user)
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