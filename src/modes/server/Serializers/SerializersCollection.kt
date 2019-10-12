package modes.server.Serializers

import modes.server.Serializers.CourseListSerializerV1.Companion.serializeCourseList as serializeCourseListV1
import modes.server.Serializers.CourseListSerializerV2.Companion.serializeCourseList as serializeCourseListV2
import models.Course

var CourseListSerializers= mapOf<Int,(ArrayList<Course>)-> String>(
    1 to ::serializeCourseListV1,
    2 to ::serializeCourseListV2
)