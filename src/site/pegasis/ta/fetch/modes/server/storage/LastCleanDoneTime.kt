package site.pegasis.ta.fetch.modes.server.storage

import site.pegasis.ta.fetch.tools.*
import java.time.ZonedDateTime

object LastCleanDoneTime {
    private var time = zonedDateTimeEpoch()

    suspend fun load() {
        noThrowSuspend  {
            time = readFile("data/lastCleanDoneTime.time").toZonedDateTime()
        }
    }

    fun get(): ZonedDateTime = time

    fun getMillis() = time.toInstant().toEpochMilli()

    suspend fun set() {
        time = ZonedDateTime.now()
        time.toJSONString().writeToFile("data/lastCleanDoneTime.time")
    }
}
