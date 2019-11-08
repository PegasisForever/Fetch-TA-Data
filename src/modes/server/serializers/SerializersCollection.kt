package modes.server.serializers

import modes.server.serializers.CourseListSerializerV1.Companion.serializeCourseList as serializeCourseListV1
import modes.server.serializers.CourseListSerializerV2.Companion.serializeCourseList as serializeCourseListV2
import modes.server.serializers.CourseListSerializerV3.Companion.serializeCourseList as serializeCourseListV3
import modes.server.serializers.TimeLineSerializerV2.Companion.serializeTimeLine as serializeTimeLineV2
import modes.server.serializers.TimeLineSerializerV3.Companion.serializeTimeLine as serializeTimeLineV3

import models.Course
import modes.server.timeline.TAUpdate

val latestApiVersion=3

var CourseListSerializers= mapOf<Int,(ArrayList<Course>)-> String>(
    1 to ::serializeCourseListV1,
    2 to ::serializeCourseListV2,
    3 to ::serializeCourseListV3
)

var TimeLineSerializers= mapOf<Int,(ArrayList<TAUpdate>)-> String>(
    2 to ::serializeTimeLineV2,
    3 to ::serializeTimeLineV3
)