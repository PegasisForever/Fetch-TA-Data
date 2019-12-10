package modes.server.parsers

import models.*
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import toZonedDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object CourseListParserV4 {
    private fun parseSmallMark(json: JSONObject) = SmallMark().apply {
        finished = json["finished"] as Boolean
        total = json["total"] as Double
        get = json["get"] as Double
        weight = json["weight"] as Double
    }

    private fun parseSmallMarkGroup(json: JSONObject, category: String) =
        SmallMarkGroup(CategoryFromInitial(category)).apply {
            available = json["available"] as Boolean
            add(parseSmallMark(json))
        }

    fun parseAssignment(json: JSONObject) = Assignment().apply {
        val smallMarkCategoryAdded = ArrayList<String>()
        json.forEach { key, value ->
            when (key) {
                "name" -> name = value as String
                "time" -> time = (value as String?)?.toZonedDateTime()
                "feedback" -> feedback = value as String?
                else -> {
                    smallMarkCategoryAdded.add(key as String)
                    smallMarkGroups.add(parseSmallMarkGroup(value as JSONObject, key))
                }
            }
        }
        enumValues<Category>().forEach { category ->
            if (!smallMarkCategoryAdded.contains(category.name)) {
                smallMarkGroups.add(SmallMarkGroup(category))
            }
        }
    }

    private fun parseWeight(json: JSONObject, category: String) = Weight(CategoryFromInitial(category)).apply {
        W = json["W"] as Double
        CW = json["CW"] as Double
        SA = OverallMark(json["SA"] as Double)
    }

    private fun parseWeightTable(json: JSONObject) = WeightTable().apply {
        json.forEach { key, value ->
            weightsList.add(parseWeight(value as JSONObject, key as String))
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
