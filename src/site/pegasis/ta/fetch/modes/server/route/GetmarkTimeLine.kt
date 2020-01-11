package site.pegasis.ta.fetch.modes.server.route

import com.sun.net.httpserver.HttpExchange
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.*
import site.pegasis.ta.fetch.exceptions.LoginException
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
        var statusCode = 200  //200:success  400:bad request  401:pwd incorrect  500:internal error
        var res = ""

        val hash = exchange.hashCode()
        val reqString = exchange.getReqString()
        val ipAddress = exchange.getIP()
        val reqApiVersion = exchange.getApiVersion()
        log(
            LogLevel.INFO,
            "Request #$hash /getmark_timeline <- $ipAddress, api version=$reqApiVersion, data=$reqString"
        )

        if (exchange.returnIfApiVersionInsufficient()) {
            log(
                LogLevel.INFO,
                "Request #$hash /getmark_timeline -> $ipAddress, api version insufficient"
            )
            return@out
        }

        try {
            with(ReqData(reqString, reqApiVersion)) {
                val courses = LoginPage()
                    .gotoSummaryPage(number, password)
                    .fillDetails()
                    .courses

                log(
                    LogLevel.INFO,
                    "Request #$hash /getmark_timeline :: Fetched successfully"
                )

                user?.let { User.add(it) }
                runFollowUpUpdate(number, courses, hash, "/getmark_timeline")
                res = JSONObject().apply {
                    put("time_line", PCache.readTimeLine(number).serialize(reqApiVersion))
                    put("course_list", PCache.readCourseList(number).serialize(reqApiVersion))
                }.toJSONString()
            }
        } catch (e: LoginException) {
            log(
                LogLevel.INFO,
                "Request #$hash /getmark_timeline :: Login error"
            )
            statusCode = 401
        } catch (e: ParseRequestException) {
            log(
                LogLevel.INFO,
                "Request #$hash /getmark_timeline :: Can't parse request"
            )
            statusCode = 400
        } catch (e: Exception) {
            if (e.message?.indexOf("SocketTimeoutException") != -1) {
                log(
                    LogLevel.WARN,
                    "Request #$hash /getmark_timeline :: Connect timeout",
                    e
                )
                statusCode = 503
            } else {
                log(
                    LogLevel.ERROR,
                    "Request #$hash /getmark_timeline :: Unknown error: ${e.message}",
                    e
                )
                statusCode = 500
            }
        }

        log(
            LogLevel.INFO,
            "Request #$hash /getmark_timeline -> $ipAddress, status=$statusCode, data=$res"
        )
        exchange.send(statusCode, res)
    }
}