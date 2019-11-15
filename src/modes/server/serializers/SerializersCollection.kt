package modes.server.serializers

import models.CourseList
import modes.server.latestApiVersion
import modes.server.timeline.TimeLine
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import modes.server.serializers.CourseListPublicSerializer.Companion.serializeCourseList as serializePublicCourseList
import modes.server.serializers.CourseListSerializerV4.Companion.serializeCourseList as serializeCourseListV4
import modes.server.serializers.TimeLineSerializerV4.Companion.serializeTimeLine as serializeTimeLineV4

fun JSONArray.wrapVersion(version: Int): JSONObject {
    val obj = JSONObject()
    obj["version"] = version
    obj["data"] = this
    return obj
}

val CourseListSerializers = mapOf<Int, (CourseList) -> JSONArray>(
    4 to ::serializeCourseListV4
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
    4 to ::serializeTimeLineV4
)

fun TimeLine.serialize(version: Int = latestApiVersion): JSONObject {
    val serializer = TimeLineSerializers[version] ?: error("Cannot get time line serializer for API V$version")
    val json = serializer(this)
    return json.wrapVersion(version)
}