package modes.server.route

import LogLevel
import com.sun.net.httpserver.HttpExchange
import getApiVersion
import getIP
import getReqString
import jsonParser
import log
import models.User
import modes.server.PCache
import modes.server.serializers.serialize
import org.json.simple.JSONObject
import returnIfApiVersionInsufficient
import send

object UpdateNoFetch {
    private class ReqData(req: String, version: Int) {
        var user: User

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
        var statusCode = 200  //200:success  400:bad request  401:pwd incorrect  500:internal error
        var res = ""

        val hash = exchange.hashCode()
        val reqString = exchange.getReqString()
        val ipAddress = exchange.getIP()
        val reqApiVersion = exchange.getApiVersion()
        log(
            LogLevel.INFO,
            "Request #$hash /update_nofetch <- $ipAddress, api version=$reqApiVersion, data=$reqString"
        )

        if (exchange.returnIfApiVersionInsufficient(7)) {
            log(LogLevel.INFO, "Request #$hash /update_nofetch -> $ipAddress, api version insufficient")
            return@out
        }

        try {
            with(ReqData(reqString, reqApiVersion).user) {
                if (User.validate(number, password)) {
                    log(LogLevel.INFO, "Request #$hash /update_nofetch :: User validated")
                    User.add(this)
                    res = JSONObject().apply {
                        put("time_line", PCache.readTimeLine(number).serialize(reqApiVersion))
                        put("course_list", PCache.readCourseList(number).serialize(reqApiVersion))
                    }.toJSONString()
                } else {
                    log(LogLevel.INFO, "Request #$hash /update_nofetch :: Wrong user or password")
                    statusCode = 401
                }
            }
        } catch (e: ParseRequestException) {
            log(LogLevel.INFO, "Request #$hash /update_nofetch :: Can't parse request")
            statusCode = 400
        } catch (e: Exception) {
            if (e.message?.indexOf("SocketTimeoutException") != -1) {
                log(LogLevel.WARN, "Request #$hash /update_nofetch :: Connect timeout", e)
                statusCode = 503
            } else {
                log(LogLevel.ERROR, "Request #$hash /update_nofetch :: Unknown error: ${e.message}", e)
                statusCode = 500
            }
        }

        log(
            LogLevel.INFO,
            "Request #$hash /update_nofetch -> $ipAddress, status=$statusCode, data=$res"
        )
        exchange.send(statusCode, res)
    }
}