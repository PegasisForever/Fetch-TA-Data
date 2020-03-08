package site.pegasis.ta.fetch.models

import site.pegasis.ta.fetch.models.Category.*
import site.pegasis.ta.fetch.tools.*
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

fun categoryFrom(str: String): Category {
    return when {
        str.indexOf("Know") != -1 -> KU
        str.indexOf("Think") != -1 -> T
        str.indexOf("Commu") != -1 -> C
        str.indexOf("Appli") != -1 -> A
        str.indexOf("Other") != -1 -> O
        str.indexOf("Final") != -1 -> F
        else -> throw Exception("Can't prase category. Text: ${str}")
    }
}

fun categoryFromInitial(str: String): Category {
    return when (str) {
        "KU" -> KU
        "T" -> T
        "C" -> C
        "A" -> A
        "O" -> O
        "F" -> F
        else -> throw Exception("Can't prase category. Text: ${str}")
    }
}

class SmallMark {
    var finished = true
    var total = 0.0
    var get = 0.0
    var weight = 0.0
    val percentage: Double
        get() = get safeDiv total

    fun copy() = SmallMark().apply {
        finished = this@SmallMark.finished
        total = this@SmallMark.total
        get = this@SmallMark.get
        weight = this@SmallMark.weight
    }

    override fun equals(other: Any?): Boolean {
        return other is SmallMark &&
            finished == other.finished &&
            total == other.total &&
            get == other.get &&
            weight == other.weight
    }

    override fun hashCode(): Int {
        var result = finished.hashCode()
        result = 31 * result + total.hashCode()
        result = 31 * result + get.hashCode()
        result = 31 * result + weight.hashCode()
        return result
    }

}

class SmallMarkGroup : ArrayList<SmallMark>() {
    val available: Boolean
        get() = size > 0
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
                if (smallMark.finished) {
                    get += smallMark.percentage * smallMark.weight
                    total += smallMark.weight
                }
            }
            return get safeDiv total
        }

    fun isSame(other: SmallMarkGroup): Boolean {
        if (size != other.size || available != other.available) return false
        forEach { smallMark ->
            if (other.find { it == smallMark } == null)
                return false
        }
        return true
    }

    fun copy() = SmallMarkGroup().apply {
        this@SmallMarkGroup.forEach {
            add(it.copy())
        }
    }
}

class Assignment : HashMap<Category, SmallMarkGroup>() {
    var name = ""
    var time: ZonedDateTime? = null
    var feedback: String? = null
    val isNoWeight: Boolean
        get() {
            forEach { entry ->
                if (entry.value.hasWeight) {
                    return false
                }
            }
            return true
        }
    val isFinished: Boolean
        get() {
            forEach { entry ->
                if (!entry.value.allFinished) {
                    return false
                }
            }
            return true
        }

    fun getAverage(weightTable: WeightTable): Double {
        var get = 0.0
        var total = 0.0
        val isNoWeight = this.isNoWeight

        forEach { category, smallMarkGroup ->
            if (smallMarkGroup.available && smallMarkGroup.hasFinished) {
                val weight = weightTable[category]!!.CW
                get += smallMarkGroup.percentage * (if (isNoWeight) 1.0 else smallMarkGroup.allWeight) * weight
                total += (if (isNoWeight) 1.0 else smallMarkGroup.allWeight) * weight
            }
        }

        return get safeDiv total
    }

    fun copy() = Assignment().apply {
        name = this@Assignment.name
        time = this@Assignment.time
        feedback = this@Assignment.feedback
        this@Assignment.forEach { category, smallMarkGroup ->
            put(category, smallMarkGroup.copy())
        }
    }
}

class AssignmentList : ArrayList<Assignment>() {
    fun copy() = AssignmentList().apply {
        this@AssignmentList.forEach { assignment ->
            add(assignment.copy())
        }
    }
}

class Weight {
    var W = 0.0
    var CW = 0.0
    var SA = OverallMark(0.0)

    fun copy() = Weight().apply {
        W = this@Weight.W
        CW = this@Weight.CW
        SA = this@Weight.SA
    }

    override operator fun equals(other: Any?): Boolean {
        return other is Weight &&
            W == other.W &&
            CW == other.CW &&
            SA == other.SA
    }

    override fun hashCode(): Int {
        var result = W.hashCode()
        result = 31 * result + CW.hashCode()
        result = 31 * result + SA.hashCode()
        return result
    }
}

class WeightTable : HashMap<Category, Weight>() {
    fun fillFakeData() {
        this[KU] = Weight().apply {
            W = 35.7
            CW = 25.0
            SA = OverallMark()
        }
        this[T] = Weight().apply {
            W = 35.7
            CW = 25.0
            SA = OverallMark()
        }
        this[C] = Weight().apply {
            W = 14.3
            CW = 10.0
            SA = OverallMark()
        }
        this[A] = Weight().apply {
            W = 14.3
            CW = 10.0
            SA = OverallMark()
        }
        this[O] = Weight().apply {
            W = 0.0
            CW = 0.0
            SA = OverallMark()
        }
        this[F] = Weight().apply {
            CW = 30.0
            SA = OverallMark()
        }
    }

    fun copy() = WeightTable().apply {
        this@WeightTable.forEach { category, weight ->
            put(category, weight.copy())
        }
    }
}

class OverallMark {
    private var mark: Double? = null //scale: 0-100
    private var level: String? = null
    private var unCertain = false

    constructor(m: Double) {
        mark = m
    }

    constructor(l: String) {
        level = l
    }

    constructor() {
        unCertain = true
    }

    fun getMarkValue(): Double {
        return if (mark != null) {
            mark!!
        } else if (level != null) {
            when (level!!.toLowerCase()) {
                "4+" -> 95.0
                "4" -> 90.0
                "4-" -> 85.0
                "3+" -> 77.5
                "3" -> 75.0
                "3-" -> 72.5
                "2+" -> 67.5
                "2" -> 65.0
                "2-" -> 62.5
                "1+" -> 57.5
                "1" -> 55.0
                "1-" -> 52.5
                "r" -> 50.0
                "" -> 0.0
                else -> 0.0
            }
        } else {
            0.0
        }
    }

    fun isInRange(m: Double) =
        if (mark != null) {
            mark!! near m threshold 0.1
        } else if (level != null) {
            when (level!!.toLowerCase()) {
                "4+" -> m in 90..100
                "4" -> m in 80..100
                "4-" -> m in 80..90
                "3+" -> m in 75..80
                "3" -> m in 70..80
                "3-" -> m in 70..75
                "2+" -> m in 65..70
                "2" -> m in 60..70
                "2-" -> m in 60..65
                "1+" -> m in 55..60
                "1" -> m in 50..60
                "1-" -> m in 50..55
                "r" -> m in 0..50
                "" -> m == 0.0
                else -> false
            }
        } else {
            true
        }

    override fun toString() = "mark: $mark level: $level unCertain: $unCertain"

    fun copy() = when {
        mark != null -> OverallMark(mark!!)
        level != null -> OverallMark(level!!)
        else -> OverallMark()
    }

    override operator fun equals(other: Any?): Boolean {
        return other is OverallMark &&
            level == other.level &&
            mark == other.mark
    }

    override fun hashCode(): Int {
        var result = level.hashCode()
        result = 31 * result + mark.hashCode()
        return result
    }
}

class Course {
    var assignments: AssignmentList? = null
    var weightTable: WeightTable? = null
    var startTime: LocalDate? = null
    var endTime: LocalDate? = null
    var name: String? = null
    var code: String? = null
    var block: String? = null
    var room: String? = null
    var overallMark: OverallMark? = null
    var cached = false
    var id: Int? = null

    val displayName: String
        get() = when {
            name != null -> name!!
            code != null -> code!!
            else -> "Unnamed Course"
        }

    //calculate more accurate course overall and avg marks
    fun calculate() {
        if (weightTable == null) return

        val otherAssignments = assignments!!.filter { it[O]!!.available }
        val otherSize = otherAssignments.size
        var foundSolution = false
        if (otherSize > 0) for (state in 0 until (2 pow otherSize)) {
            for (i in 0 until otherSize) {
                val placement = state and (1 shl i) != 0
                val assignment = otherAssignments[i]
                if (placement) {
                    assignment[O] = assignment[F].takeIf { it!!.available } ?: assignment[O]!!
                    assignment[F] = SmallMarkGroup()
                } else {
                    assignment[F] = assignment[O].takeIf { it!!.available } ?: assignment[F]!!
                    assignment[O] = SmallMarkGroup()
                }
            }
            val Oavg = otherAssignments.weightedAvg {
                it[O]!!.percentage weighted it[O]!!.allWeight
            }
            val Favg = otherAssignments.weightedAvg {
                it[F]!!.percentage weighted it[F]!!.allWeight
            }
            val expectedO = weightTable!![O]!!.SA
            val expectedF = weightTable!![F]!!.SA
            if (expectedO.isInRange(Oavg * 100) && expectedF.isInRange(Favg * 100)) {
                foundSolution = true
                break
            } else if (expectedO.isInRange(Favg * 100) && expectedF.isInRange(Oavg * 100)) {
                otherAssignments.forEach { assignment ->
                    val temp = assignment[O]
                    assignment[O] = assignment[F]!!
                    assignment[F] = temp!!
                }
                foundSolution = true
                break
            }
        }
        //If not found solution, all assignment that name contains "final" will be final mark
        if (!foundSolution) {
            for (i in 0 until otherSize) {
                val assignment = otherAssignments[i]
                if (assignment.name.contains("final", true)) {
                    assignment[F] = assignment[O].takeIf { it!!.available } ?: assignment[F]!!
                    assignment[O] = SmallMarkGroup()
                }
            }
        }

        var overallGet = 0.0
        var overallTotal = 0.0
        enumValues<Category>().forEach { category ->
            var get = 0.0
            var total = 0.0
            assignments!!.forEach { assignment ->
                val smallMarkGroup = assignment[category]
                if (smallMarkGroup != null && smallMarkGroup.hasFinished && smallMarkGroup.available && smallMarkGroup.hasWeight) {
                    get += smallMarkGroup.percentage * smallMarkGroup.allWeight
                    total += smallMarkGroup.allWeight
                }
            }

            val avg = get / total
            val weight = weightTable!![category]!!
            if (total > 0.0) {
                if (!weight.SA.isInRange(avg * 100)) {
                    logWarn("Calculated SA value of $category is not same as displayed. Calculated:${avg * 100} Displayed:${weight.SA} course code: $code")
                    overallGet += weight.SA.getMarkValue() / 100 * weight.CW
                    overallTotal += weight.CW
                } else {
                    weight.SA = OverallMark(avg * 100)
                    overallGet += avg * weight.CW
                    overallTotal += weight.CW
                }
            } else if (weight.SA.getMarkValue() > 0) {
                overallGet += weight.SA.getMarkValue() / 100 * weight.CW
                overallTotal += weight.CW
            }
        }

        val overallAvg = overallGet / overallTotal
        if (overallTotal > 0.0) {
            if (!overallMark!!.isInRange(overallAvg * 100)) {
                logWarn("Calculated overall value is not same as displayed. Calculated:${overallAvg * 100} Displayed:${overallMark} course code: $code")
            } else {
                overallMark = OverallMark(overallAvg * 100)
            }
        }
    }

    //only things like name need to be the same
    fun isSameName(other: Course): Boolean {
        if (this.id nne other.id) return true
        return (name noe other.name) && (code noe other.code) && (block noe other.block) && (room noe other.room)
    }

    fun copy() = Course().apply {
        assignments = this@Course.assignments?.copy()
        weightTable = this@Course.weightTable?.copy()
        startTime = this@Course.startTime
        endTime = this@Course.endTime
        name = this@Course.name
        code = this@Course.code
        block = this@Course.block
        room = this@Course.room
        overallMark = this@Course.overallMark?.copy()
        cached = this@Course.cached
        id = this@Course.id
    }

    override operator fun equals(other: Any?): Boolean {
        return other is Course &&
            assignments == other.assignments &&
            weightTable == other.weightTable &&
            startTime == other.startTime &&
            endTime == other.endTime &&
            name == other.name &&
            code == other.code &&
            block == other.block &&
            room == other.room &&
            overallMark == other.overallMark &&
            cached == other.cached
    }

    override fun hashCode(): Int {
        var result = assignments?.hashCode() ?: 0
        result = 31 * result + (weightTable?.hashCode() ?: 0)
        result = 31 * result + (startTime?.hashCode() ?: 0)
        result = 31 * result + (endTime?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (code?.hashCode() ?: 0)
        result = 31 * result + (block?.hashCode() ?: 0)
        result = 31 * result + (room?.hashCode() ?: 0)
        result = 31 * result + (overallMark?.hashCode() ?: 0)
        result = 31 * result + cached.hashCode()
        return result
    }
}

class CourseList() : ArrayList<Course>() {
    constructor(list: List<Course>) : this() {
        addAll(list)
    }

    fun copy() = CourseList().apply {
        this@CourseList.forEach { course ->
            add(course.copy())
        }
    }

    fun shadowCopy() = CourseList().apply {
        this@CourseList.forEach { course ->
            add(course)
        }
    }
}