package site.pegasis.ta.fetch.modes.server.route

import com.sun.net.httpserver.HttpExchange
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.exceptions.ParseRequestException
import site.pegasis.ta.fetch.exceptions.UserParseException
import site.pegasis.ta.fetch.fetchdata.fetchUserCourseList
import site.pegasis.ta.fetch.fetchdata.isConnectionException
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.models.User
import site.pegasis.ta.fetch.modes.server.serializers.serialize
import site.pegasis.ta.fetch.modes.server.storage.PCache
import site.pegasis.ta.fetch.modes.server.timeline.runFollowUpUpdate
import site.pegasis.ta.fetch.tools.jsonParser
import site.pegasis.ta.fetch.tools.logError
import site.pegasis.ta.fetch.tools.logInfo
import site.pegasis.ta.fetch.tools.logWarn

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

    val route = out@{ session: HttpSession ->
        val timing = Timing()
        var statusCode = 200  //200:success  400:bad request  401:pwd incorrect  500:internal error
        var res = ""

        val hash = session.hashCode()
        val reqString = session.getReqString()
        val ipAddress = session.getIP()
        val reqApiVersion = session.getApiVersion()
        logInfo("Request #$hash /regi <- $ipAddress, api version=$reqApiVersion, data=$reqString")

        if (session.isApiVersionInsufficient()) {
            session.send(426)
            logInfo("Request #$hash -> Api version insufficient")
            return@out
        }

        timing("init")

        try {
            with(ReqData(reqString, reqApiVersion).user) {
                val courses = fetchUserCourseList(number, password,
                    timing = timing,
                    parallel = User.get(number) == null)

                User.add(this)
                runFollowUpUpdate(number, courses)
                timing("update")

                res = JSONObject().apply {
                    put("time_line", PCache.readTimeLine(number).serialize(reqApiVersion))
                    put("course_list", PCache.readCourseList(number).serialize(reqApiVersion))
                }.toJSONString()
                timing("join")
            }
        } catch (e: Throwable) {
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
                e.isConnectionException() -> {
                    logWarn("Request #$hash :: Connect timeout", e)
                    503
                }
                else -> {
                    logError("Request #$hash :: Unknown error: ${e.message}", e)
                    500
                }
            }
        }

        session.send(statusCode, res)
        timing("send")
        logInfo("Request #$hash -> status=$statusCode", timing = timing)
    }
}