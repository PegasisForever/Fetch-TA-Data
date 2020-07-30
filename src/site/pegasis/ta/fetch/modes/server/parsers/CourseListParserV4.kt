package site.pegasis.ta.fetch.modes.server.parsers

import site.pegasis.ta.fetch.models.*
import site.pegasis.ta.fetch.tools.toZonedDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object CourseListParserV4 {
    private fun parseSmallMark(json: Map<*,*>) = SmallMark().apply {
        finished = json["finished"] as Boolean
        total = json["total"] as Double
        get = json["get"] as Double
        weight = json["weight"] as Double
    }

    private fun parseSmallMarkGroup(json: Map<*,*>) = SmallMarkGroup().apply {
        add(parseSmallMark(json))
    }

    fun parseAssignment(json: Map<*,*>) = Assignment().apply {
        json.forEach { key, value ->
            when (key) {
                "name" -> name = value as String
                "time" -> time = (value as String?)?.toZonedDateTime()
                "feedback" -> feedback = value as String?
                else -> {
                    this[categoryFromInitial(key as String)] = parseSmallMarkGroup(value as Map<*,*>)
                }
            }
        }
        enumValues<Category>().forEach { category ->
            putIfAbsent(category, SmallMarkGroup())
        }
    }

    private fun parseWeight(json: Map<*,*>) = Weight().apply {
        W = json["W"] as Double
        CW = json["CW"] as Double
        SA = OverallMark((json["SA"] as Double?) ?: 0.0)
    }

    private fun parseWeightTable(json: Map<*,*>) = WeightTable().apply {
        json.forEach { key, value ->
            this[categoryFromInitial(key as String)] = parseWeight(value as Map<*,*>)
        }
    }

    private fun parseCourse(json: Map<*,*>) = Course().apply {
        startTime = (json["start_time"] as String?)?.let {
            LocalDate.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }
        endTime = (json["end_time"] as String?)?.let {
            LocalDate.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }
        name = json["name"] as String?
        code = json["code"] as String?
        block = json["block"] as String?
        room = json["room"] as String?
        overallMark = (json["overall_mark"] as Double?)?.let { OverallMark(it) }
        cached = json["cached"] as Boolean

        if (overallMark != null) {
            assignments = AssignmentList()
            (json["assignments"] as List<*>).forEach { assignmentJSON ->
                assignments!!.add(parseAssignment(assignmentJSON as Map<*,*>))
            }
            weightTable = parseWeightTable(json["weight_table"] as Map<*,*>)
        }
    }

    fun parseCourseList(json: List<*>) = CourseList().apply {
        json.forEach { courseJSON ->
            add(parseCourse(courseJSON as Map<*,*>))
        }
    }
}
