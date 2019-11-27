package modes.server

import jsonParser
import models.CourseList
import modes.server.parsers.toCourseList
import modes.server.parsers.toTimeLine
import modes.server.serializers.serialize
import modes.server.timeline.TimeLine
import org.json.simple.JSONObject
import readFile
import writeToFile

object PCache {
    private val courseListCacheMap = HashMap<String, CourseList>()
    private val archivedCourseListCacheMap = HashMap<String, CourseList>()
    private val timeLineCacheMap = HashMap<String, TimeLine>()

    fun save(number: String, courseList: CourseList) {
        courseListCacheMap[number] = courseList
        courseList.serialize().toJSONString().writeToFile("data/courselists/$number.json")
    }

    fun saveArchive(number: String, courseList: CourseList) {
        archivedCourseListCacheMap[number] = courseList
        courseList.serialize().toJSONString().writeToFile("data/courselists-archived/$number.json")
    }

    fun save(number: String, timeLine: TimeLine) {
        timeLineCacheMap[number] = timeLine
        timeLine.serialize().toJSONString().writeToFile("data/timelines/$number.json")
    }

    fun readCourseList(number: String): CourseList {
        return if (courseListCacheMap.containsKey(number)) {
            courseListCacheMap[number]!!
        } else {
            try {
                val text = readFile("data/courselists/$number.json")
                val courseList = (jsonParser.parse(text) as JSONObject).toCourseList()
                courseListCacheMap[number] = courseList
                courseList
            } catch (e: Exception) {
                CourseList()
            }
        }
    }

    fun readArchivedCourseList(number: String): CourseList {
        return if (archivedCourseListCacheMap.containsKey(number)) {
            archivedCourseListCacheMap[number]!!
        } else {
            try {
                val text = readFile("data/courselists-archived/$number.json")
                val courseList = (jsonParser.parse(text) as JSONObject).toCourseList()
                archivedCourseListCacheMap[number] = courseList
                courseList
            } catch (e: Exception) {
                CourseList()
            }
        }
    }

    fun readTimeLine(number: String): TimeLine {
        return if (timeLineCacheMap.containsKey(number)) {
            timeLineCacheMap[number]!!
        } else {
            try {
                val text = readFile("data/timelines/$number.json")
                val timeLine = (jsonParser.parse(text) as JSONObject).toTimeLine()
                timeLineCacheMap[number] = timeLine
                timeLine
            } catch (e: Exception) {
                TimeLine()
            }
        }
    }

}

fun CourseList.save(number: String) {
    PCache.save(number, this)
}

fun CourseList.saveArchive(number: String) {
    PCache.saveArchive(number, this)
}

fun TimeLine.save(number: String) {
    PCache.save(number, this)
}
