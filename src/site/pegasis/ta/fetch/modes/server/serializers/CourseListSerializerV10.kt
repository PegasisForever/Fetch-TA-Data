package site.pegasis.ta.fetch.modes.server.serializers

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.models.*
import site.pegasis.ta.fetch.tools.toJSONString
import java.time.format.DateTimeFormatter

object CourseListSerializerV10 {
    private fun serializeSmallMark(smallMark: SmallMark): JSONObject {
        val obj = JSONObject()
        obj["finished"] = smallMark.finished
        obj["total"] = smallMark.total
        obj["get"] = smallMark.get
        obj["weight"] = smallMark.weight

        return obj
    }

    private fun serializeSmallMarkGroup(smallMarkGroup: SmallMarkGroup): JSONObject {
        val obj = JSONObject()
        val smallMarksArray = JSONArray()
        smallMarkGroup.forEach {
            smallMarksArray.add(serializeSmallMark(it))
        }
        obj["smallmarks"] = smallMarksArray

        return obj
    }

    fun serializeAssignment(assignment: Assignment): JSONObject {
        val obj = JSONObject()
        assignment.forEach { category, smallMarkGroup ->
            if (smallMarkGroup.available) {
                obj[category.name] = serializeSmallMarkGroup(smallMarkGroup)
            }
        }
        obj["name"] = assignment.name
        obj["time"] = assignment.time?.toJSONString()
        obj["feedback"] = assignment.feedback

        return obj
    }

    private fun serializeWeight(weight: Weight): JSONObject {
        val obj = JSONObject()
        obj["W"] = weight.W
        obj["CW"] = weight.CW
        obj["SA"] = weight.SA.getMarkValue()

        return obj
    }

    private fun serializeWeightTable(weightTable: WeightTable): JSONObject {
        val obj = JSONObject()
        weightTable.forEach { category, weight ->
            obj[category.name] = serializeWeight(weight)
        }

        return obj
    }

    private fun serializeCourse(course: Course): JSONObject {
        val obj = JSONObject()
        obj["start_time"] = course.startTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        obj["end_time"] = course.endTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        obj["name"] = course.name
        obj["code"] = course.code
        obj["block"] = course.block
        obj["room"] = course.room
        obj["overall_mark"] = course.overallMark?.getMarkValue()
        obj["cached"] = course.cached
        obj["id"] = course.id

        obj["assignments"] = JSONArray()
        course.assignments?.forEach { assignment ->
            (obj["assignments"] as JSONArray).add(serializeAssignment(assignment))
        }
        obj["weight_table"] = course.weightTable?.let { serializeWeightTable(it) }

        return obj
    }

    fun serializeCourseList(list: CourseList): JSONArray {
        val array = JSONArray()
        list.forEach { course ->
            array.add(serializeCourse(course))
        }

        return array
    }
}