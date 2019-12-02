package modes.server.timeline

import models.Assignment
import models.AssignmentAdded
import models.TAUpdate
import toRoundString
import kotlin.math.abs

class Notification(var title: String, var body: String) {
    override fun toString(): String {
        return "Notification title: $title body: $body"
    }
}

fun Assignment.getDisplayName(language: String): String {
    return if (this.name.isBlank()) {
        when (language) {
            "en" -> "Unknown Assignment"
            "zh" -> "未命名作业"
            else -> "Unknown Assignment"
        }
    } else {
        this.name
    }
}

object NotificationStrings {
    fun getNoti(language: String, update: TAUpdate): Notification? {
        return when (update) {
            is AssignmentAdded -> getAssignmentAddedNoti(language, update)
            else -> null
        }
    }

    fun getAssignmentAddedNoti(language: String, update: AssignmentAdded): Notification? {
        return when {
            //Course first assignment, finished and have weight
            update.overallBefore == null && update.assignment.isFinished() && !update.assignment.isNoWeight() && update.assignmentAvg != null -> {
                when (language) {
                    "en" -> Notification(
                        "First assessment in ${update.courseName}",
                        "You got average ${update.assignmentAvg?.toRoundString(1)}% in \"${update.assignment.getDisplayName(
                            language
                        )}\"."
                    )
                    "zh" -> Notification(
                        "${update.courseName}第一次发布了分数",
                        "您在“${update.assignment.getDisplayName(language)}”中获得了平均" + "${update.assignmentAvg?.toRoundString(
                            1
                        )}分"
                    )
                    else -> null
                }
            }

            //Course first assignment, not finished
            update.overallBefore == null && !update.assignment.isFinished() -> {
                when (language) {
                    "en" -> Notification(
                        "New unfinished assessment in ${update.courseName}",
                        "Teacher didn't finish marking or you didn't hand in " +
                                "\"${update.assignment.getDisplayName(language)}\"."
                    )
                    "zh" -> Notification(
                        "${update.courseName}发布了未完成的分数",
                        "老师未完成批改或您未上交“${update.assignment.getDisplayName(language)}”"
                    )
                    else -> null
                }
            }

            //Course first assignment, no weight
            update.overallBefore == null && update.assignment.isNoWeight() -> {
                when (language) {
                    "en" -> Notification(
                        "New no weight assessment in ${update.courseName}",
                        "You got average ${update.assignmentAvg?.toRoundString(1)}% in \"${update.assignment.getDisplayName(
                            language
                        )}\". " +
                                "(No weight)"
                    )
                    "zh" -> Notification(
                        "${update.courseName}第一次发布了分数",
                        "您在“${update.assignment.getDisplayName(language)}”中获得了平均\" + \"${update.assignmentAvg?.toRoundString(
                            1
                        )}分" +
                                "（无权重）"
                    )
                    else -> null
                }
            }

            //Assignment finished and have weight
            update.overallBefore != null && update.assignment.isFinished() && !update.assignment.isNoWeight() && update.assignmentAvg != null -> {
                when (language) {
                    "en" -> {
                        val compareText = when {
                            //same
                            abs(update.overallBefore!! - update.overallAfter) < 0.1 -> {
                                "Your course overall remains the same."
                            }

                            //dropped
                            update.overallBefore!! > update.overallAfter -> {
                                "Your course overall dropped from ${update.overallBefore!!.toRoundString(1)}% " +
                                        "to ${update.overallAfter.toRoundString(1)}%. " +
                                        "(-${(update.overallBefore!! - update.overallAfter).toRoundString(1)}%)"
                            }

                            //increased
                            update.overallBefore!! < update.overallAfter -> {
                                "Your course overall increased from ${update.overallBefore!!.toRoundString(1)}% " +
                                        "to ${update.overallAfter.toRoundString(1)}%. " +
                                        "(+${(update.overallAfter - update.overallBefore!!).toRoundString(1)}%)"
                            }

                            //wtf
                            else -> throw Exception("Cannot generate compare text, before: ${update.overallBefore} after: ${update.overallAfter}")
                        }

                        Notification(
                            "New assessment in ${update.courseName}",
                            "You got average ${update.assignmentAvg?.toRoundString(1)}% in " +
                                    "\"${update.assignment.getDisplayName(language)}\". " +
                                    compareText
                        )
                    }
                    "zh" -> {
                        val compareText = when {
                            //same
                            abs(update.overallBefore!! - update.overallAfter) < 0.1 -> {
                                "课程总分保持不变"
                            }

                            //dropped
                            update.overallBefore!! > update.overallAfter -> {
                                "课程总分从 ${update.overallBefore!!.toRoundString(1)}% " +
                                        "下降到了 ${update.overallAfter.toRoundString(1)}%" +
                                        "（-${(update.overallBefore!! - update.overallAfter).toRoundString(1)}%）"
                            }

                            //increased
                            update.overallBefore!! < update.overallAfter -> {
                                "课程总分从 ${update.overallBefore!!.toRoundString(1)}% " +
                                        "升高到了 ${update.overallAfter.toRoundString(1)}%" +
                                        "（+${(update.overallAfter + update.overallBefore!!).toRoundString(1)}%）"
                            }

                            //wtf
                            else -> throw Exception("Cannot generate compare text, before: ${update.overallBefore} after: ${update.overallAfter}")
                        }

                        Notification(
                            "${update.courseName}发布了新的分数",
                            "您在“${update.assignment.getDisplayName(language)}”中获得了平均" +
                                    "${update.assignmentAvg?.toRoundString(1)}分，" +
                                    compareText
                        )
                    }
                    else -> null
                }
            }

            //Assignment not finished
            update.overallBefore != null && !update.assignment.isFinished() -> {
                when (language) {
                    "en" -> Notification(
                        "New unfinished assessment in ${update.courseName}",
                        "Teacher didn't finish marking or you didn't hand in " +
                                "\"${update.assignment.getDisplayName(language)}\". " +
                                "Your course overall remains the same."
                    )
                    "zh" -> Notification(
                        "${update.courseName}发布了未完成的分数",
                        "老师未完成批改或您未上交“${update.assignment.getDisplayName(language)}”，您的课程总分保持不变"
                    )
                    else -> null
                }
            }

            //Assignment don't have weight
            update.overallBefore != null && update.assignment.isNoWeight() -> {
                when (language) {
                    "en" -> Notification(
                        "New no weight assessment in ${update.courseName}",
                        "You got average ${update.assignmentAvg?.toRoundString(1)}% in " +
                                "\"${update.assignment.getDisplayName(language)}\". (No weight)" +
                                "Your course overall remains the same."
                    )
                    "zh" -> Notification(
                        "${update.courseName}发布了新的分数",
                        "您在“${update.assignment.getDisplayName(language)}”中获得了平均" +
                                "${update.assignmentAvg?.toRoundString(1)}分（无权重）"
                    )
                    else -> null
                }
            }

            else -> null
        }
    }
}
