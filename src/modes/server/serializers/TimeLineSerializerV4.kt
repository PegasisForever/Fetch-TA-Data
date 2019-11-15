package modes.server.serializers

import modes.server.serializers.CourseListSerializerV4.Companion.serializeAssignment
import modes.server.timeline.*
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import toJSONString

class TimeLineSerializerV4 {
    companion object {
        private fun serializeAssignmentAdded(assignmentAdded: AssignmentAdded): JSONObject {
            val obj = JSONObject()
            obj["category"] = "assignment_added"
            obj["course_name"] = assignmentAdded.courseName
            obj["assignment"] = serializeAssignment(assignmentAdded.assignment)
            obj["assignment_avg"] = assignmentAdded.assignmentAvg
            obj["overall_before"] = assignmentAdded.overallBefore
            obj["overall_after"] = assignmentAdded.overallAfter
            obj["time"] = assignmentAdded.time.toJSONString()

            return obj
        }

        private fun serializeAssignmentUpdated(assignmentUpdated: AssignmentUpdated): JSONObject {
            val obj = JSONObject()
            obj["category"] = "assignment_updated"
            obj["course_name"] = assignmentUpdated.courseName
            obj["assignment_name"] = assignmentUpdated.assignmentName
            obj["assignment_before"] = serializeAssignment(assignmentUpdated.assignmentBefore)
            obj["assignment_after"] = serializeAssignment(assignmentUpdated.assignmentAfter)
            obj["time"] = assignmentUpdated.time.toJSONString()

            return obj
        }

        private fun serializeCourseAdded(courseAdded: CourseAdded): JSONObject {
            val obj = JSONObject()
            obj["category"] = "course_added"
            obj["course_name"] = courseAdded.courseName
            obj["course_block"] = courseAdded.courseBlock
            obj["time"] = courseAdded.time.toJSONString()

            return obj
        }

        private fun serializeCourseRemoved(courseRemoved: CourseRemoved): JSONObject {
            val obj = JSONObject()
            obj["category"] = "course_removed"
            obj["course_name"] = courseRemoved.courseName
            obj["course_block"] = courseRemoved.courseBlock
            obj["time"] = courseRemoved.time.toJSONString()

            return obj
        }

        fun serializeTimeLine(updateList: TimeLine): JSONArray {
            val array = JSONArray()
            updateList.forEach { taUpdate ->
                array.add(
                    when (taUpdate) {
                        is AssignmentAdded -> serializeAssignmentAdded(taUpdate)
                        is AssignmentUpdated -> serializeAssignmentUpdated(taUpdate)
                        is CourseAdded -> serializeCourseAdded(taUpdate)
                        is CourseRemoved -> serializeCourseRemoved(taUpdate)
                        else -> null
                    }
                )
            }

            return array
        }
    }
}