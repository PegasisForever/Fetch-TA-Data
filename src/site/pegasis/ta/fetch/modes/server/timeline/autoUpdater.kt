package site.pegasis.ta.fetch.modes.server.timeline

import site.pegasis.ta.fetch.*
import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.models.CourseList
import site.pegasis.ta.fetch.models.TimeLine
import site.pegasis.ta.fetch.models.User
import site.pegasis.ta.fetch.modes.server.storage.*
import site.pegasis.ta.fetch.fetchdata.fetchUserCourseList
import java.time.ZonedDateTime
import java.util.concurrent.atomic.AtomicBoolean

fun performUpdate(user: User, newData: CourseList? = null): TimeLine {
    val studentNumber = user.number
    val password = user.password
    var updates = TimeLine()

    try {
        val compareResult = compareCourses(
            oldIn = PCache.readCourseList(studentNumber),
            newIn = newData ?: fetchUserCourseList(studentNumber, password)
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

        LastUpdateTime[studentNumber] = ZonedDateTime.now()
    } catch (e: LoginException) {
        logInfo("Error while performing update for user ${studentNumber}: Login error")
    } catch (e: Exception) {
        if (e.isTimeoutException()) {
            logWarn("Error while performing update for user ${studentNumber}: Connect timeout")
        } else {
            logError("Error while performing update for user ${studentNumber}", e)
        }
    }
    return updates
}

fun runFollowUpUpdate(number: String, newData: CourseList) {
    User.get(number)?.let {
        performUpdate(it, newData)
    }
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


fun updateAutoUpdateThread() {
    if (Config.autoUpdateEnabled) {
        startAutoUpdateThread()
    } else {
        stopAutoUpdateThread()
    }
}

private var autoUpdateThread: Thread? = null
private val autoUpdateThreadRunning = AtomicBoolean(false)

fun startAutoUpdateThread() {
    if (autoUpdateThreadRunning.get()) return

    val thread = Thread({
        autoUpdateThreadRunning.set(true)
        log(LogLevel.INFO, "Auto update thread started")

        try {
            while (autoUpdateThreadRunning.get()) {
                val startTime = System.currentTimeMillis()
                User.allUsers.forEach { user ->
                    if (!autoUpdateThreadRunning.get()) throw InterruptedException()
                    val updates = performUpdate(user)
                    logInfo("Auto performed update for user ${user.number}, ${updates.size} updates")
                }

                val interval = Config.autoUpdateIntervalMinute * 60 * 1000
                val remainTime = interval - (System.currentTimeMillis() - startTime)
                logInfo("Auto update done, ${(System.currentTimeMillis() - startTime) / 1000 / 60} minutes.")
                if (remainTime > 0) Thread.sleep(remainTime)
            }
        } catch (e: InterruptedException) {
            logInfo("Thread interrupted")
        }

        autoUpdateThreadRunning.set(false)
        logInfo("Thread stopped")
    }, "AutoUpdateThread")
    thread.start()

    autoUpdateThread = thread
}

fun stopAutoUpdateThread() {
    if (!autoUpdateThreadRunning.get()) return

    autoUpdateThreadRunning.set(false)
    autoUpdateThread?.interrupt()
}