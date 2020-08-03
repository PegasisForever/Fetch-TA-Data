package site.pegasis.ta.fetch.modes.server.controller

import com.mongodb.client.model.Filters
import kotlinx.coroutines.runBlocking
import org.bson.Document
import picocli.CommandLine
import site.pegasis.ta.fetch.models.CourseAdded
import site.pegasis.ta.fetch.models.CourseList
import site.pegasis.ta.fetch.models.CourseRemoved
import site.pegasis.ta.fetch.models.TimeLine
import site.pegasis.ta.fetch.modes.server.database
import site.pegasis.ta.fetch.modes.server.parsers.toCourseList
import site.pegasis.ta.fetch.modes.server.storage.Config
import site.pegasis.ta.fetch.modes.server.storage.CourseListDB
import site.pegasis.ta.fetch.modes.server.storage.UserDB
import site.pegasis.ta.fetch.modes.server.timeline.compareCourses
import site.pegasis.ta.fetch.tools.serverBuildNumber
import site.pegasis.ta.fetch.tools.toZonedDateTime
import java.io.PrintWriter
import java.util.*
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "regen",
    description = ["Regenerate timeline and archived course list for a user."],
    mixinStandardHelpOptions = true,
    version = ["BN$serverBuildNumber"]
)
class Regen(private val printWriter: PrintWriter) : Callable<Unit> {
    @CommandLine.Parameters(index = "0", description = ["Use all to regen for all users."])
    private var studentNumber = ""

    @CommandLine.Option(
        names = ["--override", "-o"],
        description = ["Override files when contents are different."]
    )
    private var override = false

    private var courseListHistoryCollection = database.getCollection(CourseListDB.HISTORY_COURSE_LIST_COLLECTION_NAME)

    override fun call() {
        runBlocking {
            if (studentNumber != "all") {
                regenForStudent(studentNumber)
            } else {
                UserDB.forEach { user ->
                    regenForStudent(user.number)
                }
            }
        }
    }

    private suspend fun regenForStudent(number: String) {
        val historyList = courseListHistoryCollection
            .find(Filters.eq("_id", number))
            .limit(1)
            .firstOrNull()
            ?.getList("history", Document::class.java)
            ?.map { (it["time"] as Date).toZonedDateTime() to it.toCourseList() }
            ?: return

        val timeLine = TimeLine()
        val archivedCourseList = CourseList()
        var oldCourseList: CourseList? = null

        historyList.forEach { (time, newCourseList) ->
            val compareResult = compareCourses(oldCourseList ?: CourseList(), newCourseList, time)
            if (oldCourseList != null) {
                if (Config.isEnableCourseActions(time)) {
                    archivedCourseList += compareResult.archivedCourseList
                    timeLine += compareResult.updates
                } else {
                    timeLine += compareResult.updates.filter { !(it is CourseAdded || it is CourseRemoved) }
                }
                timeLine.removeUpdateContainsRemovedCourses(compareResult.updates)
            }
            oldCourseList = compareResult.courseList
        }
        archivedCourseList.removeDuplicate()

        if (override) {
            CourseListDB.save(number, timeLine)
            CourseListDB.saveArchive(number, archivedCourseList)
        } else if (checkSame(number, timeLine, archivedCourseList)) {
            CourseListDB.save(number, timeLine)
            CourseListDB.saveArchive(number, archivedCourseList)
        }
    }

    private suspend fun checkSame(number: String, newTimeLine: TimeLine, newArchivedCourseList: CourseList): Boolean {
        val storedTimeLine = CourseListDB.readTimeLine(number)
        if (!storedTimeLine.containsAll(newTimeLine)) {
            printWriter.println("Generated timeline for $number is not same as stored timeline, use -o to override.")
            return false
        }

        val storedArchivedCourseList = CourseListDB.readArchivedCourseList(number)
        if (storedArchivedCourseList != newArchivedCourseList) {
            printWriter.println("Generated archived course list for $number is not same as stored timeline, use -o to override.")
            return false
        }

        return true
    }
}
