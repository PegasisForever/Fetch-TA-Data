package site.pegasis.ta.fetch.tools

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

val torontoZoneID = ZoneId.of("America/Toronto")
fun Long.toZonedDateTime(): ZonedDateTime {
    val localDateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(this),
        torontoZoneID
    )
    return localDateTime.atZone(torontoZoneID)
}

fun ZonedDateTime.toJSONString(): String {
    return this.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}

fun String.toZonedDateTime(): ZonedDateTime {
    return ZonedDateTime.parse(this, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}

fun ZonedDateTime.isCloseTo(other: ZonedDateTime): Boolean {
    return abs(this.toEpochSecond() - other.toEpochSecond()) <= 1
}