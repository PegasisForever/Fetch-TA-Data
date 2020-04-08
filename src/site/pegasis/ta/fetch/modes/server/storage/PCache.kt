package site.pegasis.ta.fetch.modes.server.storage

import org.json.simple.JSONObject
import site.pegasis.ta.fetch.models.CourseList
import site.pegasis.ta.fetch.models.TimeLine
import site.pegasis.ta.fetch.modes.server.parsers.toCourseList
import site.pegasis.ta.fetch.modes.server.parsers.toTimeLine
import site.pegasis.ta.fetch.modes.server.serializers.serialize
import site.pegasis.ta.fetch.tools.*
import java.util.*
import kotlin.collections.HashMap

object PCache {
    private val courseListCacheMap = HashMap<String, CourseList>()
    private val archivedCourseListCacheMap = HashMap<String, CourseList>()
    private val timeLineCacheMap = HashMap<String, TimeLine>()
    private var announcementCache: String? = null

    fun clearCache() {
        courseListCacheMap.clear()
        archivedCourseListCacheMap.clear()
        timeLineCacheMap.clear()
        announcementCache = null
    }

    @Synchronized
    suspend fun save(number: String, courseList: CourseList) {
        courseListCacheMap[number] = courseList
        val str = courseList.serialize().toJSONString()
        str.writeToFile("data/courselists/$number.json")
        str.writeToFile("data/courselists-history/$number/${Date().time}.json")
    }

    @Synchronized
    suspend fun saveArchive(number: String, courseList: CourseList) {
        archivedCourseListCacheMap[number] = courseList
        courseList.serialize().toJSONString().writeToFile("data/courselists-archived/$number.json")
    }

    @Synchronized
    suspend fun save(number: String, timeLine: TimeLine) {
        timeLineCacheMap[number] = timeLine
        timeLine.serialize().toJSONString().writeToFile("data/timelines/$number.json")
    }

    suspend fun getAnnouncement(): String {
        if (announcementCache == null) {
            announcementCache = readFile("data/announcement.txt")
        }
        return announcementCache!!
    }

    suspend fun isExistsBefore(number: String): Boolean {
        return isFileExists("data/courselists/$number.json")
    }

    suspend fun readCourseList(number: String): CourseList {
        return if (courseListCacheMap.containsKey(number)) {
            courseListCacheMap[number]!!
        } else {
            try {
                val text = readFile("data/courselists/$number.json")
                val courseList = (jsonParser.parse(text) as JSONObject).toCourseList()
                courseListCacheMap[number] = courseList
                courseList
            } catch (e: java.nio.file.NoSuchFileException) {
                CourseList()
            } catch (e: Throwable) {
                log(
                    LogLevel.ERROR,
                    "Error when reading course list of $number",
                    e
                )
                CourseList()
            }
        }
    }

    suspend fun readArchivedCourseList(number: String): CourseList {
        return if (archivedCourseListCacheMap.containsKey(number)) {
            archivedCourseListCacheMap[number]!!
        } else {
            try {
                val text = readFile("data/courselists-archived/$number.json")
                val courseList = (jsonParser.parse(text) as JSONObject).toCourseList()
                archivedCourseListCacheMap[number] = courseList
                courseList
            } catch (e: java.nio.file.NoSuchFileException) {
                CourseList()
            } catch (e: Throwable) {
                log(
                    LogLevel.ERROR,
                    "Error when reading archived course list of $number",
                    e
                )
                CourseList()
            }
        }
    }

    suspend fun readTimeLine(number: String): TimeLine {
        return if (timeLineCacheMap.containsKey(number)) {
            timeLineCacheMap[number]!!
        } else {
            try {
                val text = readFile("data/timelines/$number.json")
                val timeLine = (jsonParser.parse(text) as JSONObject).toTimeLine()
                timeLineCacheMap[number] = timeLine
                timeLine
            } catch (e: java.nio.file.NoSuchFileException) {
                TimeLine()
            } catch (e: Throwable) {
                log(
                    LogLevel.ERROR,
                    "Error when reading time line of $number",
                    e
                )
                TimeLine()
            }
        }
    }

}

suspend fun CourseList.save(number: String) {
    PCache.save(number, this)
}

suspend fun CourseList.saveArchive(number: String) {
    PCache.saveArchive(number, this)
}

suspend fun TimeLine.save(number: String) {
    PCache.save(number, this)
}
