package site.pegasis.ta.fetch.modes.server.parsers

import site.pegasis.ta.fetch.models.*
import site.pegasis.ta.fetch.modes.server.parsers.CourseListParserV8.parseAssignment
import site.pegasis.ta.fetch.tools.toZonedDateTime

object TimeLineParserV9 {
    private fun parseAssignmentAdded(json: Map<*,*>) = AssignmentAdded().apply {
        courseName = json["course_name"] as String?
        assignment = parseAssignment(json["assignment"] as Map<*,*>)
        assignmentAvg = json["assignment_avg"] as Double?
        overallBefore = json["overall_before"] as Double?
        overallAfter = json["overall_after"] as Double
        time = (json["time"] as String).toZonedDateTime()
    }

    private fun parseAssignmentUpdated(json: Map<*,*>) = AssignmentUpdated().apply {
        courseName = json["course_name"] as String?
        assignmentName = json["assignment_name"] as String
        assignmentBefore = parseAssignment(json["assignment_before"] as Map<*,*>)
        assignmentAvgBefore = json["assignment_avg_before"] as Double?
        overallBefore = json["overall_before"] as Double?
        assignmentAfter = parseAssignment(json["assignment_after"] as Map<*,*>)
        assignmentAvgAfter = json["assignment_avg_after"] as Double?
        overallAfter = json["overall_after"] as Double?
        time = (json["time"] as String).toZonedDateTime()
    }

    private fun parseCourseAdded(json: Map<*,*>) = CourseAdded().apply {
        courseName = json["course_name"] as String
        courseBlock = json["course_block"] as String?
        time = (json["time"] as String).toZonedDateTime()
    }

    private fun parseCourseRemoved(json: Map<*,*>) = CourseRemoved().apply {
        courseName = json["course_name"] as String
        courseBlock = json["course_block"] as String?
        time = (json["time"] as String).toZonedDateTime()
    }

    fun parseTimeLine(json: List<*>) = TimeLine().apply {
        json.forEach { taUpdate ->
            val taUpdateJSON = taUpdate as Map<*,*>
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


