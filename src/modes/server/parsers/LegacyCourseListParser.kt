package modes.server.parsers

import jsonParser
import models.*
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LegacyCourseListParser {
    companion object {
        private fun parseSmallMark(json: JSONObject, category: String): SmallMark {
            val smallMark = SmallMark(CategoryFromInitial(category))
            smallMark.available = json["available"] as Boolean
            smallMark.finished = json["finished"] as Boolean
            smallMark.total = json["total"] as Double
            smallMark.get = json["get"] as Double
            smallMark.weight = json["weight"] as Double

            return smallMark
        }

        fun parseAssignment(json: JSONObject): Assignment {
            val assignment = Assignment()
            val smallMarkCategoryAdded = ArrayList<String>()
            json.forEach { key, value ->
                when (key) {
                    "name" -> assignment.name = value as String
                    "time" -> assignment.time = null
                    "feedback" -> assignment.feedback = (value as String).takeIf { it != "" }
                    else -> {
                        smallMarkCategoryAdded.add(key as String)
                        assignment.smallMarks.add(parseSmallMark(value as JSONObject, key))
                    }
                }
            }
            enumValues<Category>().forEach { category ->
                if (!smallMarkCategoryAdded.contains(category.name)) {
                    assignment.smallMarks.add(SmallMark(category))
                }
            }

            return assignment
        }

        private fun parseWeight(json: JSONObject, category: String): Weight {
            val weight = Weight(CategoryFromInitial(category))
            weight.W = json["W"] as Double
            weight.CW = json["CW"] as Double
            weight.SA = json["SA"] as Double

            return weight
        }

        private fun parseWeightTable(json: JSONObject): WeightTable {
            val weightTable = WeightTable()
            json.forEach { key, value ->
                weightTable.weightsList.add(parseWeight(value as JSONObject, key as String))
            }

            return weightTable
        }

        private fun parseCourse(json: JSONObject): Course {
            val course = Course()
            course.startTime = LocalDate.parse(json["start_time"] as String, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            course.endTime = LocalDate.parse(json["end_time"] as String, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            course.name = (json["name"] as String).takeIf { it != "" }
            course.code = json["code"] as String
            course.block = json["block"] as String
            course.room = json["room"] as String
            course.overallMark = json["overall_mark"] as Double?
            if (json.containsKey("cached")) {
                course.cached = json["cached"] as Boolean //delete after first run
            }

            if (course.overallMark != null) {
                course.assignments = ArrayList()
                (json["assignments"] as JSONArray).forEach { assignmentJSON ->
                    course.assignments!!.add(parseAssignment(assignmentJSON as JSONObject))
                }
                course.weightTable = parseWeightTable(json["weight_table"] as JSONObject)
            }

            return course
        }

        fun parseCourseList(str: String): CourseList {
            val json = jsonParser.parse(str) as JSONArray
            val list = CourseList()
            json.forEach { courseJSON ->
                list.add(parseCourse(courseJSON as JSONObject))
            }

            return list
        }
    }
}