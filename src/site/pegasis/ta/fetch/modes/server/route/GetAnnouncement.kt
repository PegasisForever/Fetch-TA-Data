package site.pegasis.ta.fetch.modes.server.route

import com.sun.net.httpserver.HttpExchange
import site.pegasis.ta.fetch.getIP
import site.pegasis.ta.fetch.logInfo
import site.pegasis.ta.fetch.modes.server.storage.PCache
import site.pegasis.ta.fetch.send

object GetAnnouncement {
    val route = { exchange: HttpExchange ->
        val ipAddress = exchange.getIP()
        val hash = exchange.hashCode()
        logInfo("Request #$hash /getannouncement <-> $ipAddress")

        exchange.send(
            200, try {
                PCache.getAnnouncement()
            } catch (e: Throwable) {
                ""
            }
        )
    }
}