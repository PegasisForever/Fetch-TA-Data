package Serializers

import models.*
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.time.format.DateTimeFormatter

class CourseListSerializerV1 {
    companion object {
        private fun serializeSmallMark(smallMark: SmallMark): JSONObject {
            val obj = JSONObject()
            obj["available"] = smallMark.available
            obj["finished"] = smallMark.finished
            obj["total"] = smallMark.total
            obj["get"] = smallMark.get
            obj["weight"] = smallMark.weight

            return obj
        }

        private fun serializeAssignment(assignment: Assignment): JSONObject {
            val obj = JSONObject()
            enumValues<Category>().forEach { category ->
                obj[category.name] = serializeSmallMark(SmallMark(category))
            }
            assignment.smallMarks.forEach { smallMark ->
                obj[smallMark.category.name] = serializeSmallMark(smallMark)
            }
            obj["name"] = assignment.name
            obj["time"] = assignment.time.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

            return obj
        }

        private fun serializeWeight(weight: Weight): JSONObject {
            val obj = JSONObject()
            obj["W"] = weight.W
            obj["CW"] = weight.CW
            obj["SA"] = weight.SA

            return obj
        }

        private fun serializeWeightTable(weightTable: WeightTable): JSONObject {
            val obj = JSONObject()
            weightTable.weightsList.forEach { weight ->
                obj[weight.category.name] = serializeWeight(weight)
            }

            return obj
        }

        private fun serializeCourse(course: Course): JSONObject {
            val obj = JSONObject()
            obj["start_time"] = course.startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            obj["end_time"] = course.endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            obj["name"] = course.name
            obj["code"] = course.code
            obj["block"] = course.block
            obj["room"] = course.room
            obj["overall_mark"] = course.overallMark

            val markDetail = JSONObject()
            markDetail["assignments"] = JSONArray()
            course.assignments?.forEach { assignment ->
                (markDetail["assignments"] as JSONArray).add(serializeAssignment(assignment))
            }
            markDetail["weights"] = course.weightTable?.let { serializeWeightTable(it) }

            obj["mark_detail"] = markDetail

            return obj
        }

        fun serializeCourseList(list: ArrayList<Course>): String {
            val array = JSONArray()
            list.forEach { course ->
                array.add(serializeCourse(course))
            }

            return array.toJSONString()
        }
    }
}