package modes.server.updater

import modes.server.timeline.AssignmentAdded
import toRoundString

class Notification(var title: String, var body: String){
    override fun toString(): String {
        return "Notification title: $title body: $body"
    }
}

class NotificationStrings {
    companion object {
        fun getAssignmentAddedNoti(language: String, update: AssignmentAdded): Notification? {
            return when (language) {
                "zh" -> Notification(
                    "${update.courseName}发布了新的分数",
                    "你在${update.assignmentName}中获得了${update.assignmentAvg?.toRoundString(1)}分 " +
                            if (update.overallBefore != null) {
                                "课程总分：${update.overallBefore} ⟶ ${update.overallAfter}"
                            } else {
                                ""
                            }
                )
                "en" -> Notification(
                    "New mark in ${update.courseName}",
                    "You got avg ${update.assignmentAvg?.toRoundString(1)} in ${update.assignmentName} " +
                            if (update.overallBefore != null) {
                                "Course overall: ${update.overallBefore} ⟶ ${update.overallAfter}"
                            } else {
                                ""
                            }
                )
                else -> null
            }
        }
    }
}