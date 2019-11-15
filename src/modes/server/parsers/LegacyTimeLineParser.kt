package modes.server.parsers

import jsonParser
import modes.server.parsers.LegacyCourseListParser.Companion.parseAssignment
import modes.server.timeline.*
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import toZonedDateTime

class LegacyTimeLineParser {
    companion object {
        private fun parseAssignmentAdded(json: JSONObject): AssignmentAdded {
            val assignmentAdded = AssignmentAdded()
            assignmentAdded.courseName = json["course_name"] as String
            assignmentAdded.assignment = parseAssignment(json["assignment"] as JSONObject)
            assignmentAdded.assignmentAvg = json["assignment_avg"] as Double?
            assignmentAdded.overallBefore = json["overall_before"] as Double?
            assignmentAdded.overallAfter = json["overall_after"] as Double
            assignmentAdded.time = (json["time"] as String).toZonedDateTime()

            return assignmentAdded
        }

        private fun parseAssignmentUpdated(json: JSONObject): AssignmentUpdated {
            val assignmentUpdated = AssignmentUpdated()
            assignmentUpdated.courseName = json["course_name"] as String
            assignmentUpdated.assignmentName = json["assignment_name"] as String
            assignmentUpdated.assignmentBefore = parseAssignment(json["assignment_before"] as JSONObject)   //remember to change after updated
            assignmentUpdated.assignmentAfter = parseAssignment(json["assignment_after"] as JSONObject)
            assignmentUpdated.time = (json["time"] as String).toZonedDateTime()

            return assignmentUpdated
        }

        private fun parseCourseAdded(json: JSONObject): CourseAdded {
            val courseAdded = CourseAdded()
            courseAdded.courseName = json["course_name"] as String
            courseAdded.courseBlock = json["course_block"] as String
            courseAdded.time = (json["time"] as String).toZonedDateTime()

            return courseAdded
        }

        private fun parseCourseRemoved(json:JSONObject):CourseRemoved{
            val courseRemoved=CourseRemoved()
            courseRemoved.courseName = json["course_name"] as String
            courseRemoved.courseBlock = json["course_block"] as String
            courseRemoved.time = (json["time"] as String).toZonedDateTime()

            return courseRemoved
        }

        fun parseTimeLine(str: String): TimeLine {
            val list = TimeLine()

            (jsonParser.parse(str) as JSONArray).forEach {taUpdate->
                val taUpdateJSON = taUpdate as JSONObject
                list.add(when(taUpdateJSON["category"]){
                    "assignment_added"-> parseAssignmentAdded(taUpdateJSON)
                    "assignment_updated"-> parseAssignmentUpdated(taUpdateJSON)
                    "course_added"-> parseCourseAdded(taUpdateJSON)
                    "course_removed"-> parseCourseRemoved(taUpdateJSON)
                    else->throw Exception("Cannot parse $taUpdateJSON")
                })
            }

            return list
        }

    }
}

