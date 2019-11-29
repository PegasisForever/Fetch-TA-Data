package modes.server.parsers

import models.CourseList
import modes.server.timeline.TimeLine
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import modes.server.parsers.CourseListParserV4.parseCourseList as CourseListParserV4
import modes.server.parsers.TimeLineParserV4.parseTimeLine as TimeLineParserV4

class UnwrappedData(val data: JSONArray, val version: Int)

fun unwrapVersion(obj: JSONObject): UnwrappedData {
    return UnwrappedData(obj["data"] as JSONArray, (obj["version"] as Long).toInt())
}

val CourseListParsers = mapOf<Int, (JSONArray) -> CourseList>(
    4 to ::CourseListParserV4
)

fun Any.toCourseList(): CourseList {
    if (this !is JSONObject) error("Not called on a JSONObject")
    val unwrappedData = unwrapVersion(this)
    val version = unwrappedData.version
    val data = unwrappedData.data

    val parser = CourseListParsers[version] ?: error("Cannot get course list parser for API V$version")
    return parser(data)
}


val TimeLineParsers = mapOf<Int, (JSONArray) -> TimeLine>(
    4 to ::TimeLineParserV4
)

fun Any.toTimeLine(): TimeLine {
    if (this !is JSONObject) error("Not called on a JSONObject")
    val unwrappedData = unwrapVersion(this)
    val version = unwrappedData.version
    val data = unwrappedData.data

    val parser = TimeLineParsers[version] ?: error("Cannot get time line parser for API V$version")
    return parser(data)
}