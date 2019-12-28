package site.pegasis.ta.fetch.models

import site.pegasis.ta.fetch.safeDiv

data class WeightedDouble(val value: Double = 0.0, val weight: Double = 0.0) {
    val percent: Double
        get() = value safeDiv weight

    operator fun plus(other: WeightedDouble) =
        WeightedDouble(value * weight + other.value * other.weight, weight + other.weight)

    operator fun minus(other: WeightedDouble) =
        WeightedDouble(value - other.value, weight - other.weight)

    operator fun times(factor: Double) =
        WeightedDouble(value * factor, weight * factor)

    operator fun div(factor: Double) =
        WeightedDouble(value / factor, weight / factor)
}

infix fun Double.weighted(weight: Double) = WeightedDouble(this, weight)