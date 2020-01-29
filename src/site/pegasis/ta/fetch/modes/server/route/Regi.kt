package site.pegasis.ta.fetch.modes.server.route

import com.sun.net.httpserver.HttpExchange
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.*
import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.exceptions.ParseRequestException
import site.pegasis.ta.fetch.exceptions.UserParseException
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.models.User
import site.pegasis.ta.fetch.modes.server.serializers.serialize
import site.pegasis.ta.fetch.modes.server.storage.PCache
import site.pegasis.ta.fetch.modes.server.timeline.runFollowUpUpdate
import site.pegasis.ta.fetch.webpage.LoginPage

object Regi {
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
        var statusCode = 200  //200:success  400:bad request  401:pwd incorrect  500:internal error
        var res = ""

        val hash = exchange.hashCode()
        val reqString = exchange.getReqString()
        val ipAddress = exchange.getIP()
        val reqApiVersion = exchange.getApiVersion()
        logInfo("Request #$hash /regi <- $ipAddress, api version=$reqApiVersion, data=$reqString")

        if (exchange.returnIfApiVersionInsufficient()) {
            logInfo("Request #$hash -> api version insufficient")
            return@out
        }

        timing("init")

        try {
            with(ReqData(reqString, reqApiVersion).user) {
                val courses = LoginPage(timing)
                    .gotoSummaryPage(number, password)
                    .fillDetails()
                    .courses

                User.add(this)
                runFollowUpUpdate(number, courses)
                timing("update")

                res = JSONObject().apply {
                    put("time_line", PCache.readTimeLine(number).serialize(reqApiVersion))
                    put("course_list", PCache.readCourseList(number).serialize(reqApiVersion))
                }.toJSONString()
                timing("join")
            }
        } catch (e: Exception) {
            statusCode = when {
                e is LoginException -> {
                    logInfo("Request #$hash :: Login error")
                    401
                }
                e is ParseRequestException -> {
                    logInfo("Request #$hash :: Can't parse request")
                    400
                }
                e is UserParseException -> {
                    logInfo("Request #$hash :: Can't parse given user")
                    400
                }
                e.message?.indexOf("SocketTimeoutException") != -1 -> {
                    logWarn("Request #$hash :: Connect timeout", e)
                    503
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