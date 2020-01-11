package site.pegasis.ta.fetch.modes.server.controller

import picocli.CommandLine
import site.pegasis.ta.fetch.jsonParser
import site.pegasis.ta.fetch.models.CourseList
import site.pegasis.ta.fetch.models.TimeLine
import site.pegasis.ta.fetch.modes.server.parsers.toCourseList
import site.pegasis.ta.fetch.modes.server.storage.PCache
import site.pegasis.ta.fetch.modes.server.timeline.compareCourses
import site.pegasis.ta.fetch.serverBuildNumber
import site.pegasis.ta.fetch.torontoZoneID
import java.io.File
import java.io.PrintWriter
import java.time.Instant
import java.time.ZonedDateTime
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "regen",
    description = ["Regenerate timeline and archived course list for a user"],
    mixinStandardHelpOptions = true,
    version = ["BN$serverBuildNumber"]
)
class Regen(private val printWriter: PrintWriter) : Callable<Unit> {
    @CommandLine.Parameters(index = "0", description = ["Use all to regen for all users"])
    private var studentNumber = ""

    @CommandLine.Option(
        names = ["--override", "-o"],
        description = ["If override files when contents are different"]
    )
    private var override = false

    override fun call() {
        if (studentNumber != "all") {
            regenForStudent(studentNumber)
        } else {
            File("data/courselists-history")
                .listFiles { file, _ -> file.isDirectory }!!
                .forEach { file ->
                    regenForStudent(file.name)
                }
        }
    }

    private fun regenForStudent(number: String) {
        val timeLine = TimeLine()
        val archivedCourseList = CourseList()
        var oldCourseList: CourseList? = null

        File("data/courselists-history/$number")
            .listFiles()!!
            .sorted()
            .map { file ->
                jsonParser.parse(file.readText()).toCourseList() to
                    ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.nameWithoutExtension.toLong()), torontoZoneID)
            }
            .forEach { (newCourseList, time) ->
                val compareResult = compareCourses(
                    oldCourseList ?: CourseList(), newCourseList, time)

                if (oldCourseList != null) {
                    archivedCourseList += compareResult.archivedCourseList
                    timeLine += compareResult.updates
                }
                oldCourseList = compareResult.courseList
            }

        if (override) {
            PCache.save(number, timeLine)
            PCache.saveArchive(number, archivedCourseList)
        } else if (checkSame(number, timeLine, archivedCourseList)) {
            PCache.save(number, timeLine)
            PCache.saveArchive(number, archivedCourseList)
        }
    }

    private fun checkSame(number: String, newTimeLine: TimeLine, newArchivedCourseList: CourseList): Boolean {
        val storedTimeLine = PCache.readTimeLine(number)
        if (!storedTimeLine.containsAll(newTimeLine)) {
            printWriter.println("Generated timeline for $number is not same as stored timeline, use -o to override.")
            return false
        }

        val storedArchivedCourseList = PCache.readArchivedCourseList(number)
        if (storedArchivedCourseList != newArchivedCourseList) {
            printWriter.println("Generated archived course list for $number is not same as stored timeline, use -o to override.")
            return false
        }

        return true
    }
}