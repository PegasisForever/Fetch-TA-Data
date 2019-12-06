package models

import java.time.ZonedDateTime

open class TAUpdate {
    var time = ZonedDateTime.now()
}

class TimeLine : ArrayList<TAUpdate>() {
    fun removeUpdateContainsRemovedCourses() {
        val removedCourseNames = ArrayList<String>()
        this.forEach {
            if (it is CourseRemoved) {
                removedCourseNames.add(it.courseName)
            }
        }

        removedCourseNames.forEach { courseName ->
            this.removeIf {
                when (it) {
                    is AssignmentAdded -> it.courseName == courseName
                    is AssignmentUpdated -> it.courseName == courseName
                    is CourseAdded -> it.courseName == courseName
                    is CourseRemoved -> it.courseName == courseName
                    else -> error("Impossible. $it")
                }
            }
        }
    }
}

class AssignmentAdded : TAUpdate() {
    var courseName: String? = null
    var assignment = Assignment()
    var assignmentAvg: Double? = null
    var overallBefore: Double? = null
    var overallAfter = 0.0
}

class AssignmentUpdated : TAUpdate() {
    var courseName: String? = null
    var assignmentName = ""
    var assignmentBefore = Assignment()
    var assignmentAvgBefore: Double? = null
    var overallBefore: Double? = null
    var assignmentAfter = Assignment()
    var assignmentAvgAfter: Double? = null
    var overallAfter: Double? = null
}

class CourseAdded : TAUpdate() {
    var courseName = ""
    var courseBlock: String? = null
}

class CourseRemoved : TAUpdate() {
    var courseName = ""
    var courseBlock: String? = null
}