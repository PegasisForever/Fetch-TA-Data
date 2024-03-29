package site.pegasis.ta.fetch.modes.server.timeline

import kotlinx.coroutines.*
import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.fetchdata.fetchUserCourseList
import site.pegasis.ta.fetch.models.CourseList
import site.pegasis.ta.fetch.models.TimeLine
import site.pegasis.ta.fetch.models.User
import site.pegasis.ta.fetch.modes.server.storage.*
import site.pegasis.ta.fetch.tools.*
import java.time.ZonedDateTime

suspend fun performUpdate(user: User, newData: CourseList? = null): TimeLine {
    val studentNumber = user.number
    val password = user.password
    var updates = TimeLine()

    try {
        UserUpdateStatusDB.lockAutoUpdate(studentNumber) {
            val compareResult = compareCourses(
                oldIn = CourseListDB.readCourseList(studentNumber),
                newIn = newData ?: fetchUserCourseList(studentNumber, password)
            )
            updates = compareResult.updates
            //When a user login for the first time, there will be 4 "course added" update,
            //this prevents sending 4 notifications to the user.
            val isExistsBefore = CourseListDB.isExistsBefore(studentNumber)

            //save new course list
            compareResult.courseList.save(studentNumber)

            if (isExistsBefore) {
                //append updates to timeline
                if (updates.isNotEmpty()){
                    val timeLine = CourseListDB.readTimeLine(studentNumber)
                    timeLine += updates
                    timeLine.removeUpdateContainsRemovedCourses(updates)
                    timeLine.save(studentNumber)
                }

                //append new archived courses to file
                if (compareResult.archivedCourseList.isNotEmpty()){
                    val archivedCourseList = CourseListDB.readArchivedCourseList(studentNumber)
                    archivedCourseList += compareResult.archivedCourseList
                    archivedCourseList.saveArchive(studentNumber)
                }

                sendNotifications(user, updates)
            }
        }
    } catch (e: LoginException) {
        logInfo("Error while performing update for user ${studentNumber}: Login error")
    } catch (e: Exception) {
        if (e.isConnectionException()) {
            logWarn("Error while performing update for user ${studentNumber}: Connect timeout")
        } else {
            logError("Error while performing update for user ${studentNumber}", e)
        }
    }
    return updates
}

suspend fun runFollowUpUpdate(number: String, newData: CourseList) {
    UserDB.get(number)?.let {
        performUpdate(it, newData)
    }
}

suspend fun sendNotifications(user: User, updateList: TimeLine) {
    val availableDevices = user.devices.filter { it.receive && it.token != "" }
    updateList.forEach { taUpdate ->
        availableDevices.forEach { device ->
            NotificationStrings.getNoti(device.language, taUpdate)?.let { notification ->
                if (!sendFCM(device.token, notification)) UserDB.removeDevice(user.number, device)
            }
        }
    }
}


suspend fun updateAutoUpdateThread() {
    if (Config.autoUpdateEnabled) {
        startAutoUpdateThread()
    } else {
        stopAutoUpdateThread()
    }
}

private var autoUpdateJob: Job? = null
private val autoUpdateJobRunning
    get() = autoUpdateJob?.isActive ?: false

fun startAutoUpdateThread() {
    if (autoUpdateJobRunning) return

    autoUpdateJob = GlobalScope.launch {
        log(LogLevel.INFO, "Auto update job started")

        try {
            while (isActive) {
                val startTime = System.currentTimeMillis()
                UserDB.forEach { user ->
                    if (!isActive) throw error("cancelled")
                    val userUpdateStatus = UserUpdateStatusDB.get(user.number)
                    when {
                        userUpdateStatus.isAutoUpdating -> {
                            logInfo("User ${user.number} is locked for auto updating, skipping")
                        }
                        userUpdateStatus.lastUpdateTime?.fromNowSeconds() ?: Long.MAX_VALUE < Config.getUpdateInterval() * 60 -> {
                            logInfo("User ${user.number} is recently updated, skipping")
                        }
                        else -> {
                            val updates = performUpdate(user)
                            logInfo("Auto performed update for user ${user.number}, ${updates.size} updates")
                        }
                    }
                }

                val interval = Config.getUpdateInterval() * 60 * 1000
                val remainTime = interval - (System.currentTimeMillis() - startTime)
                logInfo("Auto update done, ${(System.currentTimeMillis() - startTime) / 1000 / 60} minutes.")
                if (remainTime > 0) {
                    logInfo("Next auto update will be ${ZonedDateTime.now().plusSeconds(remainTime / 1000).toJSONString()}.")
                    delay(remainTime)
                } else {
                    logInfo("Next auto update starts now.")
                }
            }
        } finally {
            logInfo("Auto update job stopped")
        }
    }
}

suspend fun stopAutoUpdateThread() {
    autoUpdateJob?.cancelAndJoin()
}
