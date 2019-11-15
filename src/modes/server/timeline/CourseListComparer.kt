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

class CourseCompareResult(val courseList: CourseList, val updates: TimeLine)

fun compareCourses(
    old: CourseList,
    new: CourseList,
    compareTime: ZonedDateTime = ZonedDateTime.now()
): CourseCompareResult {
    val courseListResult = CourseList()
    val updateList = TimeLine()

    new.forEach { newCourse ->
        val oldCourse = old.find { it.code == newCourse.code }
        courseListResult += if (oldCourse == null) {
            val courseAdded = CourseAdded()
            courseAdded.courseName = newCourse.getDisplayName()
            courseAdded.courseBlock = newCourse.block
            courseAdded.time = compareTime
            updateList += courseAdded
            newCourse
        } else if (newCourse.overallMark == null && oldCourse.overallMark != null) {
            oldCourse.apply {
                cached = true
            }
        } else {
            updateList += compareAssignments(oldCourse, newCourse, compareTime)
            newCourse
        }
    }

    old.forEach { courseOld ->
        var isRemoved = true
        for (course in new) if (courseOld.code == course.code) {
            isRemoved = false
            break
        }

        if (isRemoved) {
            val courseRemoved = CourseRemoved()
            courseRemoved.courseName = courseOld.getDisplayName()
            courseRemoved.courseBlock = courseOld.block
            courseRemoved.time = compareTime
            updateList += courseRemoved
        }
    }


    return CourseCompareResult(courseListResult, updateList)
}


