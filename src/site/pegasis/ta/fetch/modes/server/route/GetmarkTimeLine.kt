package site.pegasis.ta.fetch.modes.server.route

import com.sun.net.httpserver.HttpExchange
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.*
import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.models.User
import site.pegasis.ta.fetch.modes.server.serializers.serialize
import site.pegasis.ta.fetch.modes.server.storage.PCache
import site.pegasis.ta.fetch.modes.server.timeline.runFollowUpUpdate
import site.pegasis.ta.fetch.webpage.LoginPage

object GetmarkTimeLine {
    private class ReqData(req: String, version: Int) {
        val number: String
        val password: String
        var user: User? = null

        init {
            try {
                val json = jsonParser.parse(req) as JSONObject
                if (version < 7) {
                    number = json["number"] as String
                    password = json["password"] as String
                } else {
                    user = User.fromClient(json)
                    number = user!!.number
                    password = user!!.password
                }
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

        logInfo("Request #$hash /getmark_timeline <- $ipAddress, api version=$reqApiVersion, data=${reqString.removeBlank()}")

        if (exchange.returnIfApiVersionInsufficient()) {
            logInfo("Request #$hash -> api version insufficient")
            return@out
        }

        timing("init")

        try {
            with(ReqData(reqString, reqApiVersion)) {
                val courses = LoginPage()
                    .gotoSummaryPage(number, password)
                    .fillDetails()
                    .courses
                timing("fetch")

                user?.let { User.add(it) }
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
        logInfo("Request #$hash /getmark_timeline -> $ipAddress, status=$statusCode", timing = timing)
    }
}