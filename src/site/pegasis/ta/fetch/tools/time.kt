package site.pegasis.ta.fetch.tools

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs

val defaultZoneID = ZoneId.of("America/Toronto")
fun Long.toZonedDateTime(): ZonedDateTime {
    val localDateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(this),
        defaultZoneID
    )
    return localDateTime.atZone(defaultZoneID)
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

fun zonedDateTimeEpoch(): ZonedDateTime = ZonedDateTime.ofInstant(Instant.EPOCH, defaultZoneID)

fun Date.toZonedDateTime(): ZonedDateTime = ZonedDateTime.ofInstant(toInstant(), defaultZoneID)

fun ZonedDateTime.toDate(): Date = Date(toEpochSecond() * 1000)
