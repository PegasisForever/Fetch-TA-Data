package modes.server.timeline

import models.Assignment
import models.Course
import models.CourseList
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
        var isNew = true
        var isUpdate = false
        var globalAssignmentOld: Assignment? = null
        for (assignmentOld in old) if (assignmentOld.name == assignment.name) {
            isNew = false
            if (!Assignment.isSame(assignment, assignmentOld)) {
                isUpdate = true
                globalAssignmentOld = assignmentOld
            }
            break
        }

        if (isNew) {
            val assignmentAdded = AssignmentAdded()
            assignmentAdded.courseName = newCourse.getDisplayName()
            assignmentAdded.assignment = assignment
            assignmentAdded.assignmentAvg = assignment.getAverage(newCourse.weightTable!!)
            assignmentAdded.overallBefore = oldCourse.overallMark
            assignmentAdded.overallAfter = newCourse.overallMark!!
            assignmentAdded.time = compareTime
            updateList.add(assignmentAdded)
        } else if (isUpdate) {
            val assignmentUpdated = AssignmentUpdated()
            assignmentUpdated.courseName = newCourse.getDisplayName()
            assignmentUpdated.assignmentName = assignment.name
            assignmentUpdated.assignmentBefore = globalAssignmentOld!!
            assignmentUpdated.assignmentAfter = assignment
            assignmentUpdated.time = compareTime
            updateList.add(assignmentUpdated)
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
        val oldCourse = old.find { newCourse.isSameCourse(it) }
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
        val isRemoved = new.find { oldCourse.isSameCourse(it) } == null
        if (isRemoved) {
            updateList += CourseRemoved().apply {
                courseName = oldCourse.getDisplayName()
                courseBlock = oldCourse.block
                time = compareTime
            }
            archivedCourseListResult += oldCourse
        }
    }

    return CourseCompareResult(courseListResult, updateList, archivedCourseListResult)
}


