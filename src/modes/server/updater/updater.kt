package modes.server.updater

import LogLevel
import log
import models.CourseList
import models.User
import modes.server.parsers.toCourseList
import modes.server.parsers.toTimeLine
import modes.server.sendFCM
import modes.server.serializers.serialize
import modes.server.timeline.AssignmentAdded
import modes.server.timeline.TAUpdate
import modes.server.timeline.TimeLine
import modes.server.timeline.compareCourses
import readFile
import webpage.LoginPage
import writeToFile
import java.util.concurrent.atomic.AtomicBoolean

//updates, courselist, timeline
fun performUpdate(user: User, newData: CourseList? = null): HashMap<String, Any> {
    val studentNumber = user.number
    val password = user.password
    var updates = TimeLine()
    var newCourseList = CourseList()
    var timeLine = TimeLine()

    var oldCourseList: CourseList? = null
    try {
        oldCourseList = readFile("data/courselists/$studentNumber.json").toCourseList()
    } catch (ignored: Exception) {
    }

    try {
        newCourseList = newData ?: LoginPage().gotoSummaryPage(studentNumber, password).fillDetails().courses
        if (oldCourseList == null) {
            newCourseList.serialize().toJSONString().writeToFile("data/courselists/$studentNumber.json")
            "[]".writeToFile("data/timelines/$studentNumber.json")
        } else {
            timeLine = readFile("data/timelines/$studentNumber.json").toTimeLine()
            updates = compareCourses(oldCourseList, newCourseList)
            timeLine.addAll(updates)

            newCourseList.serialize().toJSONString().writeToFile("data/courselists/$studentNumber.json")
            timeLine.serialize().toJSONString().writeToFile("data/timelines/$studentNumber.json")
        }

        sendNotifications(user, updates)
    } catch (e: Exception) {
        log(LogLevel.ERROR, "Error while performing update for user ${studentNumber}", e)
    }
    return hashMapOf("updates" to updates, "courselist" to newCourseList, "timeline" to timeLine)
}

//courselist, timeline
fun runFollowUpUpdate(number: String, newData: CourseList, hash: Int, routeName: String): HashMap<String, Any> {
    var courseList = newData
    var timeline = TimeLine()
    val user = User.get(number)
    user?.let {
        val out = performUpdate(it, newData)
        courseList = out["courselist"] as CourseList
        timeline = out["timeline"] as TimeLine
    }
    log(LogLevel.INFO, "Request #$hash ${routeName} :: Follow up update done")
    return hashMapOf("courselist" to courseList, "timeline" to timeline)
}

fun sendNotifications(user: User, updateList: ArrayList<TAUpdate>) {
    updateList.forEach { taUpdate ->
        when (taUpdate) {
            is AssignmentAdded -> {
                user.devices.forEach { device ->
                    if (device.receive && device.token != "") {
                        NotificationStrings.getAssignmentAddedNoti(device.language, taUpdate)?.let {
                            val deviceExists = sendFCM(device.token, it)
                            if (!deviceExists) {
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