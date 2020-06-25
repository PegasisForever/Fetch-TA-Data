package site.pegasis.ta.fetch.tools

import site.pegasis.ta.fetch.models.WeightedDouble


inline fun <T> Collection<T>.sum(action: (T) -> Double): Double {
    var sum = 0.0
    this.forEach {
        sum += action(it)
    }
    return sum
}

inline fun <T> Collection<T>.avg(action: (T) -> Double): Double {
    var sum = 0.0
    forEach {
        sum += action(it)
    }
    return sum safeDiv size.toDouble()
}

inline fun <T> Collection<T>.weightedAvg(action: (T) -> WeightedDouble): Double {
    var get = 0.0
    var total = 0.0
    forEach {
        val weightedDouble = action(it)
        get += weightedDouble.value * weightedDouble.weight
        total += weightedDouble.weight
    }
    return get safeDiv total
}

inline fun <T, U> forEach(list1: Iterable<T>, list2: Iterable<U>, action: (T, U) -> Unit) {
    list1.forEach { list1Item ->
        list2.forEach { list2Item ->
            action(list1Item, list2Item)
        }
    }
}

inline fun <T> ArrayList<T>.findAndRemove(predicate: (T) -> Boolean): T? {
    val item = this.find(predicate)
    item?.let { remove(it) }
    return item
}

fun listsEqual(list1: List<Any>?, list2: List<Any>?): Boolean {
    if ((list1 == null || list1.isEmpty()) && (list2 == null || list2.isEmpty())) return true
    if (list1 == null || list2 == null) return false

    if (list1.size != list2.size)
        return false

    val pairList = list1.zip(list2)

    return pairList.all { (a, b) ->
        a == b
    }
}
