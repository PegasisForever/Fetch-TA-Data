package modes.server.updater

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import log
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
import java.util.concurrent.atomic.AtomicBoolean

fun performUpdate(user: User, newData: ArrayList<Course>? = null):ArrayList<TAUpdate> {
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

    sendNotifications(user, updates)
    return updates
}

fun runAsyncUpdate(number: String, newData: ArrayList<Course>, hash: Int) {
    GlobalScope.launch {
        val user = User.get(number)
        user?.let {
            performUpdate(it, newData)
        }
        log(LogLevel.INFO, "Request #$hash /getmark :: Follow up update done")
    }
}

fun sendNotifications(user: User, updateList: ArrayList<TAUpdate>) {
    //TODO
}

var autoUpdateThreadRunning = AtomicBoolean(false)
fun startAutoUpdateThread(intervalMinute: Int):Thread {
    val interval = intervalMinute * 60 * 1000
    val thread = Thread {
        autoUpdateThreadRunning.set(true)
        while (autoUpdateThreadRunning.get()) {
            val startTime = System.currentTimeMillis()
            User.allUsers.forEach {user->
                val updates = performUpdate(user)
                log(LogLevel.INFO, "Performed update for user ${user.number}, ${updates.size} updates")
            }

            val remainTime=interval-(System.currentTimeMillis()-startTime)
            try {
                Thread.sleep(remainTime)
            }catch (e:InterruptedException){
                log(LogLevel.INFO, "Thread interrupted")
            }
        }
        log(LogLevel.INFO, "Thread stopped")
    }
    thread.start()

    return thread
}