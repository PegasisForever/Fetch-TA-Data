package site.pegasis.ta.fetch.tools

import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt


infix fun Double.safeDiv(other: Double) = if (other == 0.0) {
    0.0
} else {
    this / other
}

infix fun Int.pow(b: Int) = toDouble().pow(b.toDouble()).toInt()

infix fun Double?.near(b: Double?) = this to b

infix fun Pair<Double?, Double?>.threshold(t: Double) = when {
    first == null && second == null -> true
    first == null -> false
    second == null -> false
    else -> abs(first!! - second!!) < t
}

operator fun ClosedRange<Int>.contains(value: Double): Boolean {
    return value >= start && value <= endInclusive
}

fun Double.toRoundString(digit: Int): String {
    val df = DecimalFormat("#." + "#" * digit)
    df.roundingMode = RoundingMode.CEILING
    return df.format(this)
}

fun Double.round(digit: Int): Double {
    val factor = 10.0.pow(digit)
    return (this * factor).roundToInt() / factor
}