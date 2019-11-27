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

    companion object {
        fun isSame(as1: Assignment, as2: Assignment): Boolean {
            if (as1.smallMarks.size != as2.smallMarks.size) {
                return false
            }
            as1.smallMarks.forEach { as1SmallMark ->
                as2.smallMarks.forEach { as2SmallMark ->
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
        if (weightTable != null) {
            var K = 0.0
            var Kn = 0.0
            var T = 0.0
            var Tn = 0.0
            var C = 0.0
            var Cn = 0.0
            var A = 0.0
            var An = 0.0
            var O = 0.0
            var On = 0.0

            assignments!!.forEach { assignment ->
                assignment.smallMarks.forEach { smallMark ->
                    if (smallMark.finished && smallMark.available)
                        when (smallMark.category) {
                            Category.KU -> {
                                K += smallMark.get / smallMark.total * smallMark.weight
                                Kn += smallMark.weight
                            }
                            Category.T -> {
                                T += smallMark.get / smallMark.total * smallMark.weight
                                Tn += smallMark.weight
                            }
                            Category.C -> {
                                C += smallMark.get / smallMark.total * smallMark.weight
                                Cn += smallMark.weight
                            }
                            Category.A -> {
                                A += smallMark.get / smallMark.total * smallMark.weight
                                An += smallMark.weight
                            }
                            Category.O -> {
                                O += smallMark.get / smallMark.total * smallMark.weight
                                On += smallMark.weight
                            }
                        }
                }
            }

            val Ka = K / Kn
            val Ta = T / Tn
            val Ca = C / Cn
            val Aa = A / An
            val Oa = O / On
            var avg = 0.0
            var avgn = 0.0
            if (Ka >= 0.0) {
                val weight = weightTable!!.getWeight(Category.KU)
                weight.SA = Ka * 100
                avg += Ka * weight.W
                avgn += weight.W
            }
            if (Ta >= 0.0) {
                val weight = weightTable!!.getWeight(Category.T)
                weight.SA = Ta * 100
                avg += Ta * weight.W
                avgn += weight.W
            }
            if (Ca >= 0.0) {
                val weight = weightTable!!.getWeight(Category.C)
                weight.SA = Ca * 100
                avg += Ca * weight.W
                avgn += weight.W
            }
            if (Aa >= 0.0) {
                val weight = weightTable!!.getWeight(Category.A)
                weight.SA = Aa * 100
                avg += Aa * weight.W
                avgn += weight.W
            }
            if (Oa >= 0.0) {
                val weight = weightTable!!.getWeight(Category.O)
                weight.SA = Oa * 100
                avg += Oa * weight.W
                avgn += weight.W
            }

            if (avg / avgn >= 0) {
                overallMark = avg / avgn * 100
            }
        }
    }

    fun isSameCourse(other: Course): Boolean {
        return name == other.name && code == other.code && block == other.block && room == other.room
    }
}

class CourseList : ArrayList<Course>()