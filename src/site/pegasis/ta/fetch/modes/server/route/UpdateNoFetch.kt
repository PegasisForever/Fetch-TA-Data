package site.pegasis.ta.fetch.modes.server.route

import com.sun.net.httpserver.HttpExchange
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.exceptions.ParseRequestException
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.models.User
import site.pegasis.ta.fetch.modes.server.serializers.serialize
import site.pegasis.ta.fetch.modes.server.storage.LastUpdateTime
import site.pegasis.ta.fetch.modes.server.storage.PCache
import site.pegasis.ta.fetch.tools.jsonParser
import site.pegasis.ta.fetch.tools.logError
import site.pegasis.ta.fetch.tools.logInfo
import site.pegasis.ta.fetch.tools.toJSONString

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
        val timing = Timing()
        var statusCode = 200  //200:success  400:bad request  401:pwd incorrect  500:internal error
        var res = ""

        val hash = exchange.hashCode()
        val reqString = exchange.getReqString()
        val ipAddress = exchange.getIP()
        val reqApiVersion = exchange.getApiVersion()
        logInfo("Request #$hash /update_nofetch <- $ipAddress, api version=$reqApiVersion, data=$reqString")

        if (exchange.returnIfApiVersionInsufficient(7)) {
            logInfo("Request #$hash /update_nofetch -> $ipAddress, Api version insufficient")
            return@out
        }

        timing("init")

        try {
            with(ReqData(reqString, reqApiVersion).user) {
                if (User.validate(number, password)) {
                    res = JSONObject().apply {
                        this["time_line"] = PCache.readTimeLine(number).serialize(reqApiVersion)
                        this["course_list"] = PCache.readCourseList(number).serialize(reqApiVersion)
                        this["update_time"] = LastUpdateTime[number]?.toJSONString()
                    }.toJSONString()
                    timing("join")
                } else {
                    throw LoginException(null)
                }
            }
        } catch (e: Throwable) {
            statusCode = when (e) {
                is ParseRequestException -> {
                    logInfo("Request #$hash :: Can't parse request")
                    400
                }
                is LoginException ->{
                    logInfo("Request #$hash :: Student number or password incorrect")
                    401
                }
                else -> {
                    logError("Request #$hash :: Unknown error: ${e.message}", e)
                    500
                }
            }
        }

        exchange.send(statusCode, res)
        timing("send")
        logInfo("Request #$hash -> status=$statusCode", timing = timing)
    }
}