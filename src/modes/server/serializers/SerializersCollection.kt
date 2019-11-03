package modes.server.serializers

import modes.server.serializers.CourseListSerializerV1.Companion.serializeCourseList as serializeCourseListV1
import modes.server.serializers.CourseListSerializerV2.Companion.serializeCourseList as serializeCourseListV2
import modes.server.serializers.CourseListSerializerV3.Companion.serializeCourseList as serializeCourseListV3

import models.Course

var CourseListSerializers= mapOf<Int,(ArrayList<Course>)-> String>(
    1 to ::serializeCourseListV1,
    2 to ::serializeCourseListV2,
    3 to ::serializeCourseListV3
)