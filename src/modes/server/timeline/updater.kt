package modes.server.timeline

import LogLevel
import exceptions.LoginException
import log
import models.CourseList
import models.TimeLine
import models.User
import modes.server.PCache
import modes.server.save
import modes.server.saveArchive
import modes.server.sendFCM
import webpage.LoginPage
import java.util.concurrent.atomic.AtomicBoolean

fun performUpdate(user: User, newData: CourseList? = null): TimeLine {
    val studentNumber = user.number
    val password = user.password
    var updates = TimeLine()

    try {
        val compareResult = compareCourses(
            old = PCache.readCourseList(studentNumber),
            new = newData ?: LoginPage().gotoSummaryPage(studentNumber, password).fillDetails().courses
        )
        updates = compareResult.updates
        //When a user login for the first time, there will be 4 "course added" update,
        //this prevents sending 4 notifications to the user.
        val isExistsBefore = PCache.isExistsBefore(studentNumber)

        //save new course list
        compareResult.courseList.save(studentNumber)

        if (isExistsBefore) {
            //append updates to timeline
            val timeLine = PCache.readTimeLine(studentNumber)
            timeLine += updates
            timeLine.removeUpdateContainsRemovedCourses()
            timeLine.save(studentNumber)

            //append new archived courses to file
            val archivedCourseList = PCache.readArchivedCourseList(studentNumber)
            archivedCourseList += compareResult.archivedCourseList
            archivedCourseList.saveArchive(studentNumber)

            sendNotifications(user, updates)
        }
    } catch (e: LoginException) {
        log(LogLevel.INFO, "Error while performing update for user ${studentNumber}: Login error")
    } catch (e: Exception) {
        if (e.message?.indexOf("SocketTimeoutException") != -1) {
            log(LogLevel.WARN, "Error while performing update for user ${studentNumber}: Connect timeout")
        } else {
            log(LogLevel.ERROR, "Error while performing update for user ${studentNumber}", e)
        }
    }
    return updates
}

fun runFollowUpUpdate(number: String, newData: CourseList, hash: Int, routeName: String) {
    User.get(number)?.let {
        performUpdate(it, newData)
    }
    log(LogLevel.INFO, "Request #$hash $routeName :: Follow up update done")
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
                log(LogLevel.INFO, "Auto performed update for user ${user.number}, ${updates.size} updates")
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