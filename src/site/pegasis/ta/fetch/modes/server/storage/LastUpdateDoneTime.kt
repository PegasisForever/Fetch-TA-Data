package site.pegasis.ta.fetch.modes.server.storage

import site.pegasis.ta.fetch.tools.*
import java.time.ZonedDateTime

object LastUpdateDoneTime {
    private var time = ZonedDateTimeEpoch()

    fun load() {
        noThrow {
            time = readFile("data/lastUpdateDoneTime.time").toZonedDateTime()
        }
    }

    fun get(): ZonedDateTime = time

    fun getMillis() = time.toInstant().toEpochMilli()

    fun set() {
        time = ZonedDateTime.now()
        time.toJSONString().writeToFile("data/lastUpdateDoneTime.time")
    }
}