package site.pegasis.ta.fetch.modes.server.storage

import site.pegasis.ta.fetch.tools.*
import java.time.Instant
import java.time.ZonedDateTime

object LastCleanDoneTime {
    private var time = ZonedDateTime.ofInstant(Instant.EPOCH, torontoZoneID)

    fun load() {
        noThrow {
            time = readFile("data/lastCleanDoneTime.time").toZonedDateTime()
        }
    }

    fun get(): ZonedDateTime = time

    fun getMillis() = time.toInstant().toEpochMilli()

    fun set() {
        time = ZonedDateTime.now()
        time.toJSONString().writeToFile("data/lastCleanDoneTime.time")
    }
}