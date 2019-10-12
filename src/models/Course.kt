package models

import java.time.LocalDate
import java.time.ZonedDateTime

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
}

class Assignment {
    val smallMarks = ArrayList<SmallMark>()
    var name = ""
    var time = ZonedDateTime.now()
}

class Weight(var category: Category) {
    var W = 0.0
    var CW = 0.0
    var SA = 0.0
}

class WeightTable {
    val weightsList = ArrayList<Weight>()
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
}