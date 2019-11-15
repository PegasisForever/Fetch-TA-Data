package modes.server.timeline

import models.Assignment
import java.time.ZonedDateTime

open class TAUpdate {
    var time = ZonedDateTime.now()
}

class TimeLine : ArrayList<TAUpdate>()

class AssignmentAdded : TAUpdate() {
    var courseName: String? = null
    var assignment = Assignment()
    var assignmentAvg: Double? = null
    var overallBefore: Double? = null
    var overallAfter = 0.0

    override fun toString(): String {
        return assignment.name
    }
}

class AssignmentUpdated : TAUpdate() {
    var courseName: String? = null
    var assignmentName = ""
    var assignmentBefore = Assignment()
    var assignmentAfter = Assignment()
}

class CourseArchived : TAUpdate()

class CourseAdded : TAUpdate() {
    var courseName: String? = null
    var courseBlock: String? = null
}

class CourseRemoved : TAUpdate() {
    var courseName: String? = null
    var courseBlock: String? = null
}