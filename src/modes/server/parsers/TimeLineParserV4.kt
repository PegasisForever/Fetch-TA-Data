package modes.server.parsers

import models.*
import modes.server.parsers.CourseListParserV4.parseAssignment
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import toZonedDateTime

object TimeLineParserV4 {
    private fun parseAssignmentAdded(json: JSONObject) = AssignmentAdded().apply {
        courseName = json["course_name"] as String?
        assignment = parseAssignment(json["assignment"] as JSONObject)
        assignmentAvg = json["assignment_avg"] as Double?
        overallBefore = json["overall_before"] as Double?
        overallAfter = json["overall_after"] as Double
        time = (json["time"] as String).toZonedDateTime()
    }

    private fun parseAssignmentUpdated(json: JSONObject) = AssignmentUpdated().apply {
        courseName = json["course_name"] as String?
        assignmentName = json["assignment_name"] as String
        assignmentBefore = parseAssignment(json["assignment_before"] as JSONObject)
        assignmentAfter = parseAssignment(json["assignment_after"] as JSONObject)
        time = (json["time"] as String).toZonedDateTime()
    }

    private fun parseCourseAdded(json: JSONObject) = CourseAdded().apply {
        courseName = json["course_name"] as String
        courseBlock = json["course_block"] as String?
        time = (json["time"] as String).toZonedDateTime()
    }

    private fun parseCourseRemoved(json: JSONObject) = CourseRemoved().apply {
        courseName = json["course_name"] as String
        courseBlock = json["course_block"] as String?
        time = (json["time"] as String).toZonedDateTime()
    }

    fun parseTimeLine(json: JSONArray) = TimeLine().apply {
        json.forEach { taUpdate ->
            val taUpdateJSON = taUpdate as JSONObject
            add(
                when (taUpdateJSON["category"]) {
                    "assignment_added" -> parseAssignmentAdded(taUpdateJSON)
                    "assignment_updated" -> parseAssignmentUpdated(taUpdateJSON)
                    "course_added" -> parseCourseAdded(taUpdateJSON)
                    "course_removed" -> parseCourseRemoved(taUpdateJSON)
                    else -> throw Exception("Cannot parse $taUpdateJSON")
                }
            )
        }
    }

}


