package modes.server.route

import LogLevel
import com.sun.net.httpserver.HttpExchange
import getIP
import log
import modes.server.storage.CalendarData
import send

object GetCalendar {
    val route = { exchange: HttpExchange ->
        val ipAddress = exchange.getIP()
        val hash = exchange.hashCode()
        log(LogLevel.INFO, "Request #$hash /getcalendar <-> $ipAddress")
        exchange.send(200, CalendarData)
    }
}