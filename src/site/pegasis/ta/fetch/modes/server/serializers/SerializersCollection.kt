package site.pegasis.ta.fetch.modes.server.serializers

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.models.CourseList
import site.pegasis.ta.fetch.models.TimeLine
import site.pegasis.ta.fetch.modes.server.LATEST_API_VERSION
import site.pegasis.ta.fetch.modes.server.LATEST_PUBLIC_API_VERSION
import site.pegasis.ta.fetch.modes.server.serializers.CourseListPublicSerializerV1.serializeCourseList as serializePublicCourseListV1
import site.pegasis.ta.fetch.modes.server.serializers.CourseListPublicSerializerV2.serializeCourseList as serializePublicCourseListV2
import site.pegasis.ta.fetch.modes.server.serializers.CourseListSerializerV10.serializeCourseList as serializeCourseListV10
import site.pegasis.ta.fetch.modes.server.serializers.CourseListSerializerV11.serializeCourseList as serializeCourseListV11
import site.pegasis.ta.fetch.modes.server.serializers.CourseListSerializerV12.serializeCourseList as serializeCourseListV12
import site.pegasis.ta.fetch.modes.server.serializers.CourseListSerializerV4.serializeCourseList as serializeCourseListV4
import site.pegasis.ta.fetch.modes.server.serializers.CourseListSerializerV5.serializeCourseList as serializeCourseListV5
import site.pegasis.ta.fetch.modes.server.serializers.CourseListSerializerV8.serializeCourseList as serializeCourseListV8
import site.pegasis.ta.fetch.modes.server.serializers.TimeLineSerializerV4.serializeTimeLine as serializeTimeLineV4
import site.pegasis.ta.fetch.modes.server.serializers.TimeLineSerializerV5.serializeTimeLine as serializeTimeLineV5
import site.pegasis.ta.fetch.modes.server.serializers.TimeLineSerializerV6.serializeTimeLine as serializeTimeLineV6
import site.pegasis.ta.fetch.modes.server.serializers.TimeLineSerializerV9.serializeTimeLine as serializeTimeLineV9

fun JSONArray.wrapVersion(version: Int): JSONObject {
    val obj = JSONObject()
    obj["version"] = version
    obj["data"] = this
    return obj
}

private val CourseListSerializers = mapOf<Int, (CourseList) -> JSONArray>(
    4 to ::serializeCourseListV4,
    5 to ::serializeCourseListV5,
    6 to ::serializeCourseListV5,
    7 to ::serializeCourseListV5,
    8 to ::serializeCourseListV8,
    9 to ::serializeCourseListV8,
    10 to ::serializeCourseListV10,
    11 to ::serializeCourseListV11,
    12 to ::serializeCourseListV12,
    13 to ::serializeCourseListV12,
)

fun CourseList.serialize(version: Int = LATEST_API_VERSION): JSONObject {
    val serializer = CourseListSerializers[version] ?: error("Cannot get course list serializer for API V$version")
    val json = serializer(this)
    return json.wrapVersion(version)
}

private val CourseListPublicSerializers = mapOf<Int, (CourseList) -> JSONArray>(
    1 to ::serializePublicCourseListV1,
    2 to ::serializePublicCourseListV2
)

fun CourseList.serializePublic(version: Int = LATEST_PUBLIC_API_VERSION): JSONArray {
    val serializer = CourseListPublicSerializers[version] ?: error("Cannot get course list public serializer for API V$version")
    return serializer(this)
}


private val TimeLineSerializers = mapOf<Int, (TimeLine) -> JSONArray>(
    4 to ::serializeTimeLineV4,
    5 to ::serializeTimeLineV5,
    6 to ::serializeTimeLineV6,
    7 to ::serializeTimeLineV6,
    8 to ::serializeTimeLineV6,
    9 to ::serializeTimeLineV9,
    10 to ::serializeTimeLineV9,
    11 to ::serializeTimeLineV9,
    12 to ::serializeTimeLineV9,
    13 to ::serializeTimeLineV9,
)

fun TimeLine.serialize(version: Int = LATEST_API_VERSION): JSONObject {
    val serializer = TimeLineSerializers[version] ?: error("Cannot get time line serializer for API V$version")
    val json = serializer(this)
    return json.wrapVersion(version)
}
