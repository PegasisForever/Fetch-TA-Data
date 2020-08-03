package site.pegasis.ta.fetch.models

import site.pegasis.ta.fetch.tools.isCloseTo
import site.pegasis.ta.fetch.tools.near
import site.pegasis.ta.fetch.tools.threshold
import java.time.ZonedDateTime

open class TAUpdate {
    var time = ZonedDateTime.now()!!
}

class TimeLine : ArrayList<TAUpdate>() {
    fun removeUpdateContainsRemovedCourses(timeLineWithCourseRemovedUpdate: TimeLine = this) {
        val removedCourseNames = HashSet<String>()
        timeLineWithCourseRemovedUpdate.forEach {
            if (it is CourseRemoved) {
                removedCourseNames.add(it.courseName)
            }
        }

        if(removedCourseNames.isNotEmpty()){
            removeIf { update ->
                when (update) {
                    is AssignmentAdded -> update.courseName in removedCourseNames
                    is AssignmentUpdated -> update.courseName in removedCourseNames
                    is CourseAdded -> update.courseName in removedCourseNames
                    is CourseRemoved -> update.courseName in removedCourseNames
                    else -> error("Impossible. $update")
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

    override fun equals(other: Any?): Boolean {
        return other is AssignmentAdded &&
            time.isCloseTo(other.time) &&
            courseName == other.courseName &&
            assignment == other.assignment &&
            assignmentAvg near other.assignmentAvg threshold 0.0001 &&
            overallBefore near other.overallBefore threshold 0.0001 &&
            overallAfter == other.overallAfter
    }

    override fun hashCode(): Int {
        var result = courseName?.hashCode() ?: 0
        result = 31 * result + assignment.hashCode()
        result = 31 * result + (assignmentAvg?.hashCode() ?: 0)
        result = 31 * result + (overallBefore?.hashCode() ?: 0)
        result = 31 * result + overallAfter.hashCode()
        result = 31 * result + time.hashCode()
        return result
    }
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

    override fun equals(other: Any?): Boolean {
        return other is AssignmentUpdated &&
            time.isCloseTo(other.time) &&
            courseName == other.courseName &&
            assignmentName == other.assignmentName &&
            assignmentBefore == other.assignmentBefore &&
            assignmentAvgBefore near other.assignmentAvgBefore threshold 0.0001 &&
            overallBefore near other.overallBefore threshold 0.0001 &&
            assignmentAfter == other.assignmentAfter &&
            assignmentAvgAfter near other.assignmentAvgAfter threshold 0.0001 &&
            overallAfter near other.overallAfter threshold 0.0001
    }

    override fun hashCode(): Int {
        var result = courseName?.hashCode() ?: 0
        result = 31 * result + assignmentName.hashCode()
        result = 31 * result + assignmentBefore.hashCode()
        result = 31 * result + (assignmentAvgBefore?.hashCode() ?: 0)
        result = 31 * result + (overallBefore?.hashCode() ?: 0)
        result = 31 * result + assignmentAfter.hashCode()
        result = 31 * result + (assignmentAvgAfter?.hashCode() ?: 0)
        result = 31 * result + (overallAfter?.hashCode() ?: 0)
        result = 31 * result + time.hashCode()
        return result
    }
}

class CourseAdded : TAUpdate() {
    var courseName = ""
    var courseBlock: String? = null

    override fun equals(other: Any?): Boolean {
        return other is CourseAdded &&
            time.isCloseTo(other.time) &&
            courseName == other.courseName &&
            courseBlock == other.courseBlock
    }

    override fun hashCode(): Int {
        var result = courseName.hashCode()
        result = 31 * result + (courseBlock?.hashCode() ?: 0)
        result = 31 * result + time.hashCode()
        return result
    }
}

class CourseRemoved : TAUpdate() {
    var courseName = ""
    var courseBlock: String? = null

    override fun equals(other: Any?): Boolean {
        return other is CourseRemoved &&
            time.isCloseTo(other.time) &&
            courseName == other.courseName &&
            courseBlock == other.courseBlock
    }

    override fun hashCode(): Int {
        var result = courseName.hashCode()
        result = 31 * result + (courseBlock?.hashCode() ?: 0)
        result = 31 * result + time.hashCode()
        return result
    }
}
