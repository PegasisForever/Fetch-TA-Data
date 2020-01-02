package site.pegasis.ta.fetch.modes.server.route

import com.sun.net.httpserver.HttpExchange
import site.pegasis.ta.fetch.*
import site.pegasis.ta.fetch.modes.server.storage.PCache

object GetAnnouncement {
    val route = { exchange: HttpExchange ->
        val ipAddress = exchange.getIP()
        val hash = exchange.hashCode()
        log(
            LogLevel.INFO,
            "Request #$hash /getannouncement <-> $ipAddress"
        )

        exchange.send(
            200, try {
                PCache.getAnnouncement()
            } catch (e: Throwable) {
                ""
            }
        )
    }
}