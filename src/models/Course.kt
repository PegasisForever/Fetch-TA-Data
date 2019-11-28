package models

import java.time.LocalDate
import java.time.ZonedDateTime

enum class Category(val displayName: String) {
    KU("Knowledge / Understanding"),
    T("Thinking"),
    C("Communication"),
    A("Application"),
    O("Other/Culminating"),
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

fun CategoryFromInitial(str: String): Category {
    return when (str) {
        "KU" -> Category.KU
        "T" -> Category.T
        "C" -> Category.C
        "A" -> Category.A
        "O" -> Category.O
        "F" -> Category.F
        else -> throw Exception("Can't prase category. Text: ${str}")
    }
}

class SmallMark(var category: Category) {
    var available = false
    var finished = true
    var total = 0.0
    var get = 0.0
    var weight = 0.0

    override fun toString(): String {
        return "${category.name}: finished=$finished"
    }

    companion object {
        fun isSame(sm1: SmallMark, sm2: SmallMark): Boolean {
            return sm1.available == sm2.available &&
                    sm1.finished == sm2.finished &&
                    sm1.total == sm2.total &&
                    sm1.get == sm2.get &&
                    sm1.weight == sm2.weight
        }
    }
}

class Assignment {
    val smallMarks = ArrayList<SmallMark>()
    var name = ""
    var time: ZonedDateTime? = null
    var feedback: String? = null

    fun getAverage(weightTable: WeightTable): Double {
        var total = 0.0
        var get = 0.0

        smallMarks.forEach { smallMark ->
            if (smallMark.available && smallMark.finished) {
                val weight = weightTable.getWeight(smallMark.category).CW
                total += smallMark.total * weight
                get += smallMark.get * weight
            }
        }

        return get / total * 100
    }

    fun isNoWeight(): Boolean {
        smallMarks.forEach { smallMark ->
            if (smallMark.weight != 0.0) {
                return false
            }
        }
        return true
    }

    fun isFinished(): Boolean {
        smallMarks.forEach { smallMark ->
            if (!smallMark.finished) {
                return false
            }
        }
        return true
    }

    //everything need to be the same
    fun isSame(other: Assignment): Boolean {
        if (smallMarks.size != other.smallMarks.size) {
            return false
        }
        smallMarks.forEach { as1SmallMark ->
            other.smallMarks.forEach { as2SmallMark ->
                if (as1SmallMark.category == as2SmallMark.category && !SmallMark.isSame(
                        as1SmallMark,
                        as2SmallMark
                    )
                ) {
                    return false
                }
            }
        }
        return true
    }
}

class Weight(var category: Category) {
    var W = 0.0
    var CW = 0.0
    var SA = 0.0
}

class WeightTable {
    val weightsList = ArrayList<Weight>()

    fun getWeight(category: Category): Weight {
        weightsList.forEach { weight ->
            if (weight.category == category) {
                return weight
            }
        }
        throw java.lang.Exception("Cannot found $category in $weightsList")
    }
}

class Course {
    var assignments: ArrayList<Assignment>? = null
    var weightTable: WeightTable? = null
    var startTime: LocalDate? = null
    var endTime: LocalDate? = null
    var name: String? = null
    var code: String? = null
    var block: String? = null
    var room: String? = null
    var overallMark: Double? = null
    var cached = false

    fun getDisplayName(): String {
        return when {
            name != null -> name!!
            code != null -> code!!
            else -> "Unnamed Course"
        }
    }

    //calculate more accurate course overall and avg marks
    fun calculate() {
        if (weightTable == null) {
            return
        }
        var overallGet = 0.0
        var overallTotal = 0.0
        enumValues<Category>().forEach { category ->
            var get = 0.0
            var total = 0.0
            assignments!!.forEach { assignment ->
                val smallMark = assignment.smallMarks.find { it.category == category }
                if (smallMark != null && smallMark.finished && smallMark.available) {
                    get += smallMark.get / smallMark.total * smallMark.weight
                    total += smallMark.weight
                }
            }

            val avg = get / total
            if (!avg.isNaN()) {
                val weight = weightTable!!.getWeight(category)
                weight.SA = avg * 100

                overallGet += avg * weight.W
                overallTotal += weight.W
            }
        }
        val overallAvg = overallGet / overallTotal
        if (!overallAvg.isNaN()) {
            overallMark = overallAvg * 100
        }
    }

    //only things like name need to be the same
    fun isSame(other: Course): Boolean {
        return name == other.name && code == other.code && block == other.block && room == other.room
    }
}

class CourseList : ArrayList<Course>()