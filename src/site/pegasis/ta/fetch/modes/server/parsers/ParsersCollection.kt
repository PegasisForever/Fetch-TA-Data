package site.pegasis.ta.fetch.modes.server.parsers

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.models.CourseList
import site.pegasis.ta.fetch.models.TimeLine
import site.pegasis.ta.fetch.modes.server.parsers.CourseListParserV10.parseCourseList as CourseListParserV10
import site.pegasis.ta.fetch.modes.server.parsers.CourseListParserV4.parseCourseList as CourseListParserV4
import site.pegasis.ta.fetch.modes.server.parsers.CourseListParserV8.parseCourseList as CourseListParserV8
import site.pegasis.ta.fetch.modes.server.parsers.TimeLineParserV4.parseTimeLine as TimeLineParserV4
import site.pegasis.ta.fetch.modes.server.parsers.TimeLineParserV6.parseTimeLine as TimeLineParserV6
import site.pegasis.ta.fetch.modes.server.parsers.TimeLineParserV9.parseTimeLine as TimeLineParserV9

class UnwrappedData(val data: JSONArray, val version: Int)

fun unwrapVersion(obj: JSONObject): UnwrappedData {
    return UnwrappedData(obj["data"] as JSONArray, (obj["version"] as Long).toInt())
}

val CourseListParsers = mapOf<Int, (JSONArray) -> CourseList>(
    4 to ::CourseListParserV4,
    5 to ::CourseListParserV4,
    6 to ::CourseListParserV4,
    7 to ::CourseListParserV4,
    8 to ::CourseListParserV8,
    9 to ::CourseListParserV8,
    10 to ::CourseListParserV10
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
    4 to ::TimeLineParserV4,
    5 to ::TimeLineParserV4,
    6 to ::TimeLineParserV6,
    7 to ::TimeLineParserV6,
    8 to ::TimeLineParserV6,
    9 to ::TimeLineParserV9,
    10 to ::TimeLineParserV9
)

fun Any.toTimeLine(): TimeLine {
    if (this !is JSONObject) error("Not called on a JSONObject")
    val unwrappedData = unwrapVersion(this)
    val version = unwrappedData.version
    val data = unwrappedData.data

    val parser = TimeLineParsers[version] ?: error("Cannot get time line parser for API V$version")
    return parser(data)
}