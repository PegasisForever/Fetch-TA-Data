package modes.server.timeline

import models.*
import java.time.ZonedDateTime

fun compareAssignments(
    oldCourse: Course,
    newCourse: Course,
    compareTime: ZonedDateTime = ZonedDateTime.now()
): ArrayList<TAUpdate> {
    val updateList = ArrayList<TAUpdate>()
    val old = oldCourse.assignments ?: ArrayList()
    val new = newCourse.assignments ?: ArrayList()

    new.forEach { assignment ->
        val oldAssignment = old.find { it.name == assignment.name }

        if (oldAssignment == null) { //new assignment
            updateList += AssignmentAdded().apply {
                courseName = newCourse.getDisplayName()
                this.assignment = assignment
                assignmentAvg = assignment.getAverage(newCourse.weightTable!!)
                overallBefore = oldCourse.overallMark
                overallAfter = newCourse.overallMark!!
                time = compareTime
            }
        } else if (!assignment.isSame(oldAssignment)) { //assignment updated
            updateList += AssignmentUpdated().apply {
                courseName = newCourse.getDisplayName()
                assignmentName = assignment.name
                assignmentBefore = oldAssignment
                assignmentAvgBefore = oldAssignment.getAverage(oldCourse.weightTable!!)
                overallBefore = oldCourse.overallMark
                assignmentAfter = assignment
                assignmentAvgAfter = assignment.getAverage(newCourse.weightTable!!)
                overallAfter = newCourse.overallMark
                time = compareTime
            }
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
    old: CourseList,
    new: CourseList,
    compareTime: ZonedDateTime = ZonedDateTime.now()
): CourseCompareResult {
    val courseListResult = CourseList()
    val archivedCourseListResult = CourseList()
    val updateList = TimeLine()

    //for each course in new course list, test if it's new added, mark hidden by teacher, or normal
    new.forEach { newCourse ->
        val oldCourse = old.find { newCourse.isSame(it) }
        courseListResult += if (oldCourse == null) { //this course is only in the new course list
            updateList += CourseAdded().apply {
                courseName = newCourse.getDisplayName()
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
    old.forEach { oldCourse ->
        val isRemoved = new.find { oldCourse.isSame(it) } == null
        if (isRemoved) {
            updateList += CourseRemoved().apply {
                courseName = oldCourse.getDisplayName()
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


