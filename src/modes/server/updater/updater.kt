package modes.server.updater

import LogLevel
import log
import models.CourseList
import models.User
import modes.server.PCache
import modes.server.save
import modes.server.sendFCM
import modes.server.timeline.TimeLine
import modes.server.timeline.compareCourses
import webpage.LoginPage
import java.util.concurrent.atomic.AtomicBoolean

fun performUpdate(user: User, newData: CourseList? = null): TimeLine {
    val studentNumber = user.number
    val password = user.password
    var updates = TimeLine()

    val oldCourseList = try {
        PCache.readCourseList(studentNumber)
    } catch (e: Exception) {
        null
    }

    try {
        val newCourseList = newData ?: LoginPage().gotoSummaryPage(studentNumber, password).fillDetails().courses
        if (oldCourseList == null) {
            newCourseList.save(studentNumber)
            TimeLine().save(studentNumber)
        } else {
            val compareResult = compareCourses(oldCourseList, newCourseList)
            updates = compareResult.updates

            val timeLine = PCache.readTimeLine(studentNumber)
            timeLine += updates
            timeLine.save(studentNumber)

            compareResult.courseList.save(studentNumber)
        }

        sendNotifications(user, updates)
    } catch (e: Exception) {
        log(LogLevel.ERROR, "Error while performing update for user ${studentNumber}", e)
    }
    return updates
}

fun runFollowUpUpdate(number: String, newData: CourseList, hash: Int, routeName: String) {
    User.get(number)?.let {
        performUpdate(it, newData)
    }
    log(LogLevel.INFO, "Request #$hash ${routeName} :: Follow up update done")
}

fun sendNotifications(user: User, updateList: TimeLine) {
    val availableDevices = user.devices.filter { it.receive && it.token != "" }
    updateList.forEach { taUpdate ->
        availableDevices.forEach { device ->
            NotificationStrings.getNoti(device.language, taUpdate)?.let { notification ->
                if (!sendFCM(device.token, notification)) User.removeToken(device.token)
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