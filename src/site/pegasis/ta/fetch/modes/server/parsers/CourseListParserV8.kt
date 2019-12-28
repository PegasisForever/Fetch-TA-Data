package site.pegasis.ta.fetch.modes.server.parsers

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.models.*
import site.pegasis.ta.fetch.toZonedDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object CourseListParserV8 {
    private fun parseSmallMark(json: JSONObject) = SmallMark().apply {
        finished = json["finished"] as Boolean
        total = json["total"] as Double
        get = json["get"] as Double
        weight = json["weight"] as Double
    }

    private fun parseSmallMarkGroup(json: JSONObject) = SmallMarkGroup().apply {
        (json["smallmarks"] as JSONArray).forEach { smallMarkJSON ->
            add(parseSmallMark(smallMarkJSON as JSONObject))
        }
    }

    fun parseAssignment(json: JSONObject) = Assignment().apply {
        json.forEach { key, value ->
            when (key) {
                "name" -> name = value as String
                "time" -> time = (value as String?)?.toZonedDateTime()
                "feedback" -> feedback = value as String?
                else -> {
                    this[categoryFromInitial(key as String)] = parseSmallMarkGroup(value as JSONObject)
                }
            }
        }
        enumValues<Category>().forEach { category ->
            putIfAbsent(category, SmallMarkGroup())
        }
    }

    private fun parseWeight(json: JSONObject) = Weight().apply {
        W = json["W"] as Double
        CW = json["CW"] as Double
        SA = OverallMark(json["SA"] as Double)
    }

    private fun parseWeightTable(json: JSONObject) = WeightTable().apply {
        json.forEach { key, value ->
            this[categoryFromInitial(key as String)] = parseWeight(value as JSONObject)
        }
    }

    private fun parseCourse(json: JSONObject) = Course().apply {
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
            assignments = ArrayList()
            (json["assignments"] as JSONArray).forEach { assignmentJSON ->
                assignments!!.add(parseAssignment(assignmentJSON as JSONObject))
            }
            weightTable = parseWeightTable(json["weight_table"] as JSONObject)
        }
    }

    fun parseCourseList(json: JSONArray) = CourseList().apply {
        json.forEach { courseJSON ->
            add(parseCourse(courseJSON as JSONObject))
        }
    }
}
