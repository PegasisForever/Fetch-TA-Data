package site.pegasis.ta.fetch.modes.server.timeline

import site.pegasis.ta.fetch.models.*
import site.pegasis.ta.fetch.tools.findAndRemove
import site.pegasis.ta.fetch.tools.torontoZoneID
import java.time.ZonedDateTime

fun compareAssignments(
    oldCourse: Course,
    newCourse: Course,
    compareTime: ZonedDateTime = ZonedDateTime.now(torontoZoneID)
): ArrayList<TAUpdate> {
    val updateList = ArrayList<TAUpdate>()
    val old = oldCourse.assignments ?: AssignmentList()
    val new = newCourse.assignments ?: AssignmentList()

    new.forEach { assignment ->
        val oldAssignment = old.find { it.name == assignment.name }

        if (oldAssignment == null) { //new assignment
            updateList += AssignmentAdded().apply {
                courseName = newCourse.displayName
                this.assignment = assignment
                assignmentAvg = assignment.getAverage(newCourse.weightTable!!) * 100
                overallBefore = oldCourse.overallMark?.getMarkValue()
                overallAfter = newCourse.overallMark!!.getMarkValue()
                time = compareTime
            }
        } else if (assignment != oldAssignment) { //assignment updated
            updateList += AssignmentUpdated().apply {
                courseName = newCourse.displayName
                assignmentName = assignment.name
                assignmentBefore = oldAssignment
                assignmentAvgBefore = oldAssignment.getAverage(oldCourse.weightTable!!) * 100
                overallBefore = oldCourse.overallMark?.getMarkValue()
                assignmentAfter = assignment
                assignmentAvgAfter = assignment.getAverage(newCourse.weightTable!!) * 100
                overallAfter = newCourse.overallMark?.getMarkValue()
                time = compareTime
            }
        }
        if (oldAssignment?.time != null) {
            assignment.time = oldAssignment.time
        }
    }

    return updateList
}

class CourseCompareResult(
    val courseList: CourseList,
    val updates: TimeLine,
    val archivedCourseList: CourseList
)

fun compareCourses(
    oldIn: CourseList,
    newIn: CourseList,
    compareTime: ZonedDateTime = ZonedDateTime.now(torontoZoneID)
): CourseCompareResult {
    val old = oldIn.copy()
    val old2 = old.shadowCopy()
    val new = newIn.copy()
    val courseListResult = CourseList()
    val archivedCourseListResult = CourseList()
    val updateList = TimeLine()

    //for each course in new course list, test if it's new added, mark hidden by teacher, or normal
    new.forEach { newCourse ->
        val oldCourse = old.findAndRemove { newCourse.isSameName(it) }
        courseListResult += if (oldCourse == null) { //this course is only in the new course list
            updateList += CourseAdded().apply {
                courseName = newCourse.displayName
                courseBlock = newCourse.block
                time = compareTime
            }
            newCourse
        } else if (newCourse.overallMark == null && oldCourse.overallMark != null) { //teacher hides the mark in new course
            oldCourse.apply {
                cached = true
            }
        } else { //this course is in both new and old course list
            updateList += compareAssignments(oldCourse, newCourse, compareTime)
            newCourse
        }
    }

    //find removed courses
    old2.forEach { oldCourse ->
        val isRemoved = new.find { oldCourse.isSameName(it) } == null
        if (isRemoved) {
            updateList += CourseRemoved().apply {
                courseName = oldCourse.displayName
                courseBlock = oldCourse.block
                time = compareTime
            }
            archivedCourseListResult += oldCourse
        }
    }

    return CourseCompareResult(
        courseListResult,
        updateList,
        archivedCourseListResult
    )
}


