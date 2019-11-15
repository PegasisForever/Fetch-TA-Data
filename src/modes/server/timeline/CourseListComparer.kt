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

fun compareCourses(
    old: CourseList,
    new: CourseList,
    compareTime: ZonedDateTime = ZonedDateTime.now()
): TimeLine {
    val updateList = TimeLine()

    for (i in 0 until new.size){
        val course=new[i]
        var oldCourse:Course?=null
        for (courseOld in old) if (courseOld.code == course.code) {
            oldCourse=courseOld
            updateList.addAll(compareAssignments(courseOld, course, compareTime))
        }

        if (oldCourse==null) {
            val courseAdded = CourseAdded()
            courseAdded.courseName = course.getDisplayName()
            courseAdded.courseBlock = course.block
            courseAdded.time = compareTime
            updateList.add(courseAdded)
        }else if(course.overallMark==null && oldCourse.overallMark!=null){
            new[i]=oldCourse
            new[i].cached=true
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
            updateList.add(courseRemoved)
        }
    }


    return updateList
}


