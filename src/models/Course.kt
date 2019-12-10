package models

import LogLevel
import log
import sum
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.math.abs

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

class SmallMark {
    var finished = true
    var total = 0.0
    var get = 0.0
    var weight = 0.0
    val percentage: Double
        get() = get / total

    fun isSame(other: SmallMark): Boolean {
        return finished == other.finished &&
                total == other.total &&
                get == other.get &&
                weight == other.weight
    }

}

class SmallMarkGroup(var category: Category) : ArrayList<SmallMark>() {
    var available = false
    val hasFinished: Boolean
        get() = find { it.finished } != null
    val allFinished: Boolean
        get() = find { !it.finished } == null
    val hasWeight: Boolean
        get() = find { it.weight > 0 } != null
    val allGet: Double
        get() = sum { if (it.finished) it.get else 0.0 }
    val allTotal: Double
        get() = sum { if (it.finished) it.total else 0.0 }
    val allWeight: Double
        get() = sum { if (it.finished) it.weight else 0.0 }
    val percentage: Double
        get() {
            var get = 0.0
            var total = 0.0
            forEach { smallMark ->
                get += smallMark.percentage * smallMark.weight
                total += smallMark.weight
            }
            return get / total
        }

    fun isSame(other: SmallMarkGroup): Boolean {
        if (size != other.size || category != other.category || available != other.available) return false
        forEach { smallMark ->
            if (other.find { it.isSame(smallMark) } == null) return false
        }
        return true
    }
}

class Assignment {
    val smallMarkGroups = ArrayList<SmallMarkGroup>()
    var name = ""
    var time: ZonedDateTime? = null
    var feedback: String? = null

    fun getAverage(weightTable: WeightTable): Double {
        var total = 0.0
        var get = 0.0

        smallMarkGroups.forEach { smallMarkGroup ->
            if (smallMarkGroup.available && smallMarkGroup.hasFinished) {
                val weight = weightTable.getWeight(smallMarkGroup.category).CW
                total += smallMarkGroup.allTotal / smallMarkGroup.allGet * weight
                get += weight
            }
        }

        return get / total * 100
    }

    fun get(category: Category): SmallMarkGroup? {
        return smallMarkGroups.find { it.category == category }
    }

    fun isNoWeight(): Boolean {
        smallMarkGroups.forEach { smallMarkGroup ->
            if (smallMarkGroup.hasWeight) {
                return false
            }
        }
        return true
    }

    fun isFinished(): Boolean {
        smallMarkGroups.forEach { smallMarkGroup ->
            if (!smallMarkGroup.allFinished) {
                return false
            }
        }
        return true
    }

    //everything need to be the same
    fun isSame(other: Assignment): Boolean {
        if (smallMarkGroups.size != other.smallMarkGroups.size) {
            return false
        }
        smallMarkGroups.forEach { as1SmallMarkGroup ->
            if (other.smallMarkGroups.find { it.isSame(as1SmallMarkGroup) } == null) {
                return false
            }
        }
        return true
    }
}

class Weight(var category: Category) {
    var W = 0.0
    var CW = 0.0
    var SA = OverallMark(0.0)
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

class OverallMark {
    var mark: Double? = null
    var level: String? = null

    constructor(m: Double) {
        mark = m
    }

    constructor(l: String) {
        level = l
        log(LogLevel.INFO, "overall level str: \"$l\"")
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
    var overallMark: OverallMark? = null
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
                val smallMarkGroup = assignment.get(category)
                if (smallMarkGroup != null && smallMarkGroup.hasFinished && smallMarkGroup.available && smallMarkGroup.hasWeight) {
                    get += smallMarkGroup.percentage * smallMarkGroup.allWeight
                    total += smallMarkGroup.allWeight
                }
            }

            val avg = get / total
            if (!avg.isNaN()) {
                val weight = weightTable!!.getWeight(category)
                if (weight.SA.mark != null && abs(weight.SA.mark!! - avg * 100) > 0.1) {
                    log(
                        LogLevel.WARN,
                        "Calculated SA value of $category is not same as displayed. Calculated:${avg * 100} Displayed:${weight.SA.mark} course code: $code"
                    )
                    overallGet += weight.SA.mark!! / 100 * weight.CW
                    overallTotal += weight.CW
                } else {
                    weight.SA = OverallMark(avg * 100)
                    overallGet += avg * weight.CW
                    overallTotal += weight.CW
                }
            }
        }

        val overallAvg = overallGet / overallTotal
        if (!overallAvg.isNaN()) {
            if (overallMark?.mark != null && abs(overallMark!!.mark!! - overallAvg * 100) > 0.1) {
                log(
                    LogLevel.WARN,
                    "Calculated overall value is not same as displayed. Calculated:${overallAvg * 100} Displayed:${overallMark?.mark} course code: $code"
                )
            } else {
                overallMark = OverallMark(overallAvg * 100)
            }
        }
    }

    //only things like name need to be the same
    fun isSame(other: Course): Boolean {
        return name == other.name && code == other.code && block == other.block && room == other.room
    }
}

class CourseList : ArrayList<Course>()