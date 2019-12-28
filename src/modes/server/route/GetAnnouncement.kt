package modes.server.route

import LogLevel
import com.sun.net.httpserver.HttpExchange
import getIP
import log
import readFile
import send

object GetAnnouncement {
    val route = { exchange: HttpExchange ->
        val ipAddress = exchange.getIP()
        val hash = exchange.hashCode()
        log(LogLevel.INFO, "Request #$hash /getannouncement <-> $ipAddress")

        exchange.send(
            200, try {
                readFile("data/announcement.txt")
            } catch (e: Throwable) {
                ""
            }
        )
    }
}