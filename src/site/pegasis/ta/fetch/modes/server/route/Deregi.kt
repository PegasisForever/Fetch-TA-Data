package site.pegasis.ta.fetch.modes.server.route

import com.sun.net.httpserver.HttpExchange
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.exceptions.ParseRequestException
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.models.User
import site.pegasis.ta.fetch.tools.jsonParser
import site.pegasis.ta.fetch.tools.logError
import site.pegasis.ta.fetch.tools.logInfo

object Deregi {
    private class ReqData(req: String, version: Int) {
        val user: User

        init {
            try {
                val json = jsonParser.parse(req) as JSONObject
                user = User.fromClient(json)
            } catch (e: Exception) {
                throw ParseRequestException()
            }
        }
    }

    val route = out@{ exchange: HttpExchange ->
        val timing = Timing()
        var statusCode = 200  //200:success  400:bad request  500:internal error

        val hash = exchange.hashCode()
        val reqString = exchange.getReqString()
        val ipAddress = exchange.getIP()
        val reqApiVersion = exchange.getApiVersion()
        logInfo("Request #$hash /deregi <- $ipAddress, api version=$reqApiVersion, data=$reqString")

        if (exchange.returnIfApiVersionInsufficient()) {
            logInfo("Request #$hash /deregi -> Api version insufficient")
            return@out
        }

        timing("init")

        try {
            val user = ReqData(reqString, reqApiVersion).user
            User.remove(user)
            timing("remove user")
        } catch (e: Throwable) {
            statusCode = when (e) {
                is ParseRequestException -> {
                    logInfo("Request #$hash :: Can't parse request")
                    400
                }
                else -> {
                    logError("Request #$hash :: Unknown error: ${e.message}", e)
                    500
                }
            }
        }

        exchange.send(statusCode)
        logInfo("Request #$hash -> status=$statusCode", timing = timing)
    }
}