package modes.server.timeline

import models.Assignment
import java.time.ZonedDateTime

open class TAUpdate{
    var time=ZonedDateTime.now()
}

class AssignmentAdded:TAUpdate(){
    var courseName=""
    var assignmentName=""
    var assignmentAvg=0.0
    var overallBefore:Double?=null
    var overallAfter=0.0

    override fun toString(): String {
        return assignmentName
    }
}

class AssignmentUpdated:TAUpdate(){
    var courseName=""
    var assignmentName=""
    var assignmentBefore=Assignment()
    var assignmentAfter=Assignment()
}

class CourseArchived:TAUpdate(){

}
class CourseAdded:TAUpdate(){
    var courseName=""
    var courseBlock=""
}

class CourseRemoved:TAUpdate(){
    var courseName=""
    var courseBlock=""
}