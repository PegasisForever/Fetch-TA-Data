package models

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.lang.Exception
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList

enum class Category(val displayName: String) {
    KU("Knowledge / Understanding"),
    T("Thinking"),
    C("Communication"),
    A("Application"),
    O("Other"),
    F("Final / Culminating")
}

fun CategoryFrom(str: String): Category {
    return when {
        str.indexOf("Know") != -1 -> Category.KU
        str.indexOf("Think") != -1 -> Category.T
        str.indexOf("Commu") != -1 -> Category.C
        str.indexOf("Appli") != -1 -> Category.A
        str.indexOf("Other") != -1 -> Category.O
        str.indexOf("Final") != -1 -> Category.F
        else -> throw Exception("Can't prase category. Text: ${str}")
    }
}

class SmallMark(var category: Category) {
    var available = false
    var finished = true
    var total = 0.0
    var get = 0.0
    var weight = 0.0

    fun toJSONObject(): JSONObject {
        val obj=JSONObject()
        obj["available"]=available
        obj["finished"]=finished
        obj["total"]=total
        obj["get"]=get
        obj["weight"]=weight

        return obj
    }
}

class Assignment {
    val smallMarks = ArrayList<SmallMark>()
    var name = ""
    var time = ZonedDateTime.now()

    fun toJSONObject(): JSONObject {
        val obj = JSONObject()
        smallMarks.forEach {
            obj[it.category.name]=it.toJSONObject()
        }
        obj["name"]=name
        obj["time"]=time.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        return obj
    }
}

class Weight(var category: Category) {
    var W = 0.0
    var CW = 0.0
    var SA = 0.0

    fun toJSONObject(): JSONObject {
        val obj = JSONObject()
        obj["W"]=W
        obj["CW"]=CW
        obj["SA"]=SA

        return obj
    }
}

class WeightTable {
    val weightsList = ArrayList<Weight>()

    fun toJSONObject(): JSONObject {
        val obj = JSONObject()
        weightsList.forEach {
            obj[it.category.name]=it.toJSONObject()
        }

        return obj
    }
}

class Course {
    var assignments: ArrayList<Assignment>? = null
    var weightTable: WeightTable? = null
    var startTime = LocalDate.now()
    var endTime = LocalDate.now()
    var name = ""
    var code = ""
    var block = ""
    var room = ""
    var overallMark: Double? = null

    fun toJSONObject(): JSONObject {
        val obj=JSONObject()
        obj["start_time"]=startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        obj["end_time"]=endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        obj["name"]=name
        obj["code"]=code
        obj["block"]=block
        obj["room"]=room
        obj["overall_mark"]=overallMark

        val markDetail=JSONObject()
        markDetail["assignments"]=JSONArray()
        assignments?.forEach {
            (markDetail["assignments"] as JSONArray).add(it.toJSONObject())
        }
        markDetail["weights"]=weightTable?.toJSONObject()

        obj["mark_detail"]=markDetail

        return obj
    }
}

fun ArrayList<Course>.toJSONArray():JSONArray{
    val array=JSONArray()
    forEach {
        array.add(it.toJSONObject())
    }

    return array
}