package modes.server.timeline

import java.time.ZonedDateTime

open class TAUpdate{
    var time=ZonedDateTime.now()
}

class AssignmentAdded:TAUpdate(){
    var courseName=""
    var assignmentName=""
    var assignmentAvg=0.0
    var firstAssignment=false
    var overallBefore=0.0
    var overallAfter=0.0
}

class AssignmentUpdated:TAUpdate(){

}

class CourseArchived:TAUpdate(){

}
class CourseAdded:TAUpdate(){

}
class CourseReplaced:TAUpdate(){

}