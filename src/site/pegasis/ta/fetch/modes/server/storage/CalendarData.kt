package site.pegasis.ta.fetch.modes.server.storage

import site.pegasis.ta.fetch.tools.readFile

object CalendarData {
    private var calendar = "[]"

    suspend fun load() {
        calendar = readFile("data/calendar.json")
    }

    fun get() = calendar
}
