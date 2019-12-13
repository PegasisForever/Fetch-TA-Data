package modes.server.serializers

import models.CourseList
import models.TimeLine
import modes.server.latestApiVersion
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import modes.server.serializers.CourseListPublicSerializer.serializeCourseList as serializePublicCourseList
import modes.server.serializers.CourseListSerializerV4.serializeCourseList as serializeCourseListV4
import modes.server.serializers.CourseListSerializerV5.serializeCourseList as serializeCourseListV5
import modes.server.serializers.CourseListSerializerV8.serializeCourseList as serializeCourseListV8
import modes.server.serializers.TimeLineSerializerV4.serializeTimeLine as serializeTimeLineV4
import modes.server.serializers.TimeLineSerializerV5.serializeTimeLine as serializeTimeLineV5
import modes.server.serializers.TimeLineSerializerV6.serializeTimeLine as serializeTimeLineV6
import modes.server.serializers.TimeLineSerializerV9.serializeTimeLine as serializeTimeLineV9

fun JSONArray.wrapVersion(version: Int): JSONObject {
    val obj = JSONObject()
    obj["version"] = version
    obj["data"] = this
    return obj
}

val CourseListSerializers = mapOf<Int, (CourseList) -> JSONArray>(
    4 to ::serializeCourseListV4,
    5 to ::serializeCourseListV5,
    6 to ::serializeCourseListV5,
    7 to ::serializeCourseListV5,
    8 to ::serializeCourseListV8,
    9 to ::serializeCourseListV8
)

fun CourseList.serialize(version: Int = latestApiVersion): JSONObject {
    val serializer = CourseListSerializers[version] ?: error("Cannot get course list serializer for API V$version")
    val json = serializer(this)
    return json.wrapVersion(version)
}

fun CourseList.serializePublic(): JSONArray {
    return serializePublicCourseList(this)
}


val TimeLineSerializers = mapOf<Int, (TimeLine) -> JSONArray>(
    4 to ::serializeTimeLineV4,
    5 to ::serializeTimeLineV5,
    6 to ::serializeTimeLineV6,
    7 to ::serializeTimeLineV6,
    8 to ::serializeTimeLineV6,
    9 to ::serializeTimeLineV9
)

fun TimeLine.serialize(version: Int = latestApiVersion): JSONObject {
    val serializer = TimeLineSerializers[version] ?: error("Cannot get time line serializer for API V$version")
    val json = serializer(this)
    return json.wrapVersion(version)
}