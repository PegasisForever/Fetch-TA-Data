package site.pegasis.ta.fetch.modes.server.route

import com.sun.net.httpserver.HttpExchange
import site.pegasis.ta.fetch.modes.server.storage.CalendarData
import site.pegasis.ta.fetch.tools.logInfo

object GetCalendar {
    val route = { exchange: HttpExchange ->
        val ipAddress = exchange.getIP()
        val hash = exchange.hashCode()
        logInfo("Request #$hash /getcalendar <-> $ipAddress")
        exchange.send(200, CalendarData)
    }
}