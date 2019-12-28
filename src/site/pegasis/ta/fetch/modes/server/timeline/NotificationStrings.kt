package site.pegasis.ta.fetch.modes.server.timeline

import site.pegasis.ta.fetch.models.*
import site.pegasis.ta.fetch.toRoundString
import kotlin.math.abs

class Notification(var title: String, var body: String) {
    override fun toString(): String {
        return "Notification title: $title body: $body"
    }
}

fun Assignment.getDisplayName(language: String) = if (this.name.isBlank()) {
    when (language) {
        "en" -> "Unknown Assignment"
        "zh" -> "未命名作业"
        else -> "Unknown Assignment"
    }
} else {
    this.name
}


object NotificationStrings {
    fun getNoti(language: String, update: TAUpdate): Notification? {
        return when (update) {
            is CourseAdded -> getCourseAddedNoti(language, update)
            is CourseRemoved -> getCourseRemovedNoti(language, update)
            is AssignmentAdded -> getAssignmentAddedNoti(language, update)
            is AssignmentUpdated -> getAssignmentUpdatedNoti(language, update)
            else -> null
        }
    }

    private fun getCourseAddedNoti(language: String, update: CourseAdded) = with(update) {
        if (courseBlock != null) {
            when (language) {
                "zh" -> Notification(
                    "添加了新的课程",
                    "课程 “$courseName” 已添加到您课程表中的第${courseBlock}节课。"
                )
                "en" -> Notification(
                    "New Course Added",
                    "Course \"$courseName\" has been added to your time table at period $courseBlock."
                )
                else -> null
            }
        } else {
            when (language) {
                "zh" -> Notification(
                    "添加了新的课程",
                    "课程 “$courseName” 已添加到您的课程表中。"
                )
                "en" -> Notification(
                    "New Course Added",
                    "Course \"$courseName\" has been added to your time table."
                )
                else -> null
            }
        }
    }

    private fun getCourseRemovedNoti(language: String, update: CourseRemoved) = with(update) {
        if (courseBlock != null) {
            when (language) {
                "zh" -> Notification(
                    "移除了一个课程",
                    "课程 “$courseName” 已从您课程表中移除。"
                )
                "en" -> Notification(
                    "Course Removed",
                    "Course \"$courseName\" has been removed from your time table at period $courseBlock."
                )
                else -> null
            }
        } else {
            when (language) {
                "zh" -> Notification(
                    "移除了一个课程",
                    "课程 “$courseName” 已从您课程表中移除。"
                )
                "en" -> Notification(
                    "Course Removed",
                    "Course \"$courseName\" has been removed from your time table."
                )
                else -> null
            }
        }
    }

    private fun getAssignmentAddedNoti(language: String, update: AssignmentAdded) = with(update) {
        when {
            //Course first assignment, finished and have weight
            overallBefore == null && assignment.isFinished && !assignment.isNoWeight && assignmentAvg != null -> when (language) {
                "en" -> Notification(
                    "First assessment in $courseName",
                    "You got average ${assignmentAvg?.toRoundString(1)}% in \"${assignment.getDisplayName(
                        language
                    )}\"."
                )
                "zh" -> Notification(
                    "${courseName}第一次发布了分数",
                    "您在“${assignment.getDisplayName(language)}”中获得了平均" + "${assignmentAvg?.toRoundString(1)}分"
                )
                else -> null
            }

            //Course first assignment, not finished
            overallBefore == null && !assignment.isFinished -> when (language) {
                "en" -> Notification(
                    "New unfinished assessment in $courseName",
                    "Teacher didn't finish marking or you didn't hand in " +
                            "\"${assignment.getDisplayName(language)}\"."
                )
                "zh" -> Notification(
                    "${courseName}发布了未完成的分数",
                    "老师未完成批改或您未上交“${assignment.getDisplayName(language)}”"
                )
                else -> null
            }

            //Course first assignment, no weight
            overallBefore == null && assignment.isNoWeight -> when (language) {
                "en" -> Notification(
                    "New no weight assessment in $courseName",
                    "You got average ${assignmentAvg?.toRoundString(1)}% in \"${assignment.getDisplayName(
                        language
                    )}\". " +
                            "(No weight)"
                )
                "zh" -> Notification(
                    "${courseName}第一次发布了分数",
                    "您在“${assignment.getDisplayName(language)}”中获得了平均\" + \"${assignmentAvg?.toRoundString(
                        1
                    )}分" +
                            "（无权重）"
                )
                else -> null
            }

            //Assignment finished and have weight
            overallBefore != null && assignment.isFinished && !assignment.isNoWeight && assignmentAvg != null -> when (language) {
                "en" -> {
                    val compareText = when {
                        //same
                        abs(overallBefore!! - overallAfter) < 0.1 -> {
                            "Your course overall remains the same."
                        }

                        //dropped
                        overallBefore!! > overallAfter -> {
                            "Your course overall dropped from ${overallBefore!!.toRoundString(1)}% " +
                                    "to ${overallAfter.toRoundString(1)}%. " +
                                    "(-${(overallBefore!! - overallAfter).toRoundString(1)}%)"
                        }

                        //increased
                        overallBefore!! < overallAfter -> {
                            "Your course overall increased from ${overallBefore!!.toRoundString(1)}% " +
                                    "to ${overallAfter.toRoundString(1)}%. " +
                                    "(+${(overallAfter - overallBefore!!).toRoundString(1)}%)"
                        }

                        //wtf
                        else -> throw Exception("Cannot generate compare text, before: $overallBefore after: $overallAfter")
                    }

                    Notification(
                        "New assessment in $courseName",
                        "You got average ${assignmentAvg?.toRoundString(1)}% in " +
                                "\"${assignment.getDisplayName(language)}\". " +
                                compareText
                    )
                }
                "zh" -> {
                    val compareText = when {
                        //same
                        abs(overallBefore!! - overallAfter) < 0.1 -> {
                            "课程总分保持不变"
                        }

                        //dropped
                        overallBefore!! > overallAfter -> {
                            "课程总分从 ${overallBefore!!.toRoundString(1)}% " +
                                    "下降到了 ${overallAfter.toRoundString(1)}%" +
                                    "（-${(overallBefore!! - overallAfter).toRoundString(1)}%）"
                        }

                        //increased
                        overallBefore!! < overallAfter -> {
                            "课程总分从 ${overallBefore!!.toRoundString(1)}% " +
                                    "升高到了 ${overallAfter.toRoundString(1)}%" +
                                    "（+${(overallAfter - overallBefore!!).toRoundString(1)}%）"
                        }

                        //wtf
                        else -> throw Exception("Cannot generate compare text, before: $overallBefore after: $overallAfter")
                    }

                    Notification(
                        "${courseName}发布了新的分数",
                        "您在“${assignment.getDisplayName(language)}”中获得了平均" +
                                "${assignmentAvg?.toRoundString(1)}分，" +
                                compareText
                    )
                }
                else -> null
            }

            //Assignment not finished
            overallBefore != null && !assignment.isFinished -> when (language) {
                "en" -> Notification(
                    "New unfinished assessment in $courseName",
                    "Teacher didn't finish marking or you didn't hand in " +
                            "\"${assignment.getDisplayName(language)}\". " +
                            "Your course overall remains the same."
                )
                "zh" -> Notification(
                    "${courseName}发布了未完成的分数",
                    "老师未完成批改或您未上交“${assignment.getDisplayName(language)}”，您的课程总分保持不变"
                )
                else -> null
            }

            //Assignment don't have weight
            overallBefore != null && assignment.isNoWeight -> when (language) {
                "en" -> Notification(
                    "New no weight assessment in $courseName",
                    "You got average ${assignmentAvg?.toRoundString(1)}% in " +
                            "\"${assignment.getDisplayName(language)}\". (No weight)" +
                            "Your course overall remains the same."
                )
                "zh" -> Notification(
                    "${courseName}发布了新的分数",
                    "您在“${assignment.getDisplayName(language)}”中获得了平均" +
                            "${assignmentAvg?.toRoundString(1)}分（无权重）"
                )
                else -> null
            }

            else -> null
        }
    }

    private fun getAssignmentUpdatedNoti(language: String, update: AssignmentUpdated) = with(update) {
        when {
            assignmentAvgBefore != null && assignmentAvgAfter != null -> when (language) {
                "en" -> {
                    val compareText = when {
                        //dropped
                        assignmentAvgBefore!! > assignmentAvgAfter!! -> "Your assignment average dropped from ${assignmentAvgBefore?.toRoundString(
                            1
                        )}% to ${assignmentAvgAfter?.toRoundString(1)}%."

                        //increased
                        assignmentAvgBefore!! < assignmentAvgAfter!! -> "Your assignment average increased from ${assignmentAvgBefore?.toRoundString(
                            1
                        )}% to ${assignmentAvgAfter?.toRoundString(1)}%."

                        //same
                        else -> "Your assignment average didn't change."
                    }

                    Notification(
                        "Assignment \"$assignmentName\" has edited by the teacher",
                        compareText
                    )
                }
                "zh" -> {
                    val compareText = when {
                        //dropped
                        assignmentAvgBefore!! > assignmentAvgAfter!! -> "这项作业的平均分从${assignmentAvgBefore?.toRoundString(1)}分下降到了${assignmentAvgAfter?.toRoundString(
                            1
                        )}分。"

                        //increased
                        assignmentAvgBefore!! < assignmentAvgAfter!! -> "这项作业的平均分从${assignmentAvgBefore?.toRoundString(1)}分提高到了${assignmentAvgAfter?.toRoundString(
                            1
                        )}分。"

                        //same
                        else -> "这项作业的平均分没有改变。"
                    }

                    Notification(
                        "老师更改了“${assignmentName}”的评分。",
                        compareText
                    )
                }
                else -> null
            }

            assignmentAvgBefore == null && assignmentAvgAfter != null -> when (language) {
                "en" -> Notification(
                    "Assignment \"$assignmentName\" has edited by the teacher",
                    "Your new average of this assessment is ${assignmentAvgAfter?.toRoundString(1)}%."
                )
                "zh" -> Notification(
                    "老师更改了“${assignmentName}”的评分。",
                    "这项作业的新平均分是${assignmentAvgAfter?.toRoundString(1)}分。"
                )
                else -> null
            }

            else -> null
        }
    }
}