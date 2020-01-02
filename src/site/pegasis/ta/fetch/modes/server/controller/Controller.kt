package site.pegasis.ta.fetch.modes.server.controller

import com.sun.net.httpserver.HttpExchange
import org.json.simple.JSONArray
import site.pegasis.ta.fetch.*

object Controller {

    val route = { exchange: HttpExchange ->
        var statusCode = 200
        var res = ""

        val hash = exchange.hashCode()
        val reqString = exchange.getReqString()
        val ipAddress = exchange.getIP()

        log(
            LogLevel.INFO,
            "Control #$hash <- $ipAddress, data=$reqString"
        )

        try {
            val args = (jsonParser.parse(reqString) as JSONArray)
                .map { it as String }

        } catch (e: Throwable) {
            log(
                LogLevel.ERROR,
                "Control #$hash Unknown error: ${e.message}",
                e
            )
            statusCode = 500
            res =  ANSI_RED + e.toStackTrace() + ANSI_RESET
        }

        exchange.send(statusCode, res, false)
    }
}