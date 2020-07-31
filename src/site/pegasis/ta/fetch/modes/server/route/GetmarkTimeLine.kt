package site.pegasis.ta.fetch.modes.server.route

import org.json.simple.JSONObject
import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.exceptions.ParseRequestException
import site.pegasis.ta.fetch.fetchdata.fetchUserCourseList
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.models.User
import site.pegasis.ta.fetch.modes.server.serializers.serialize
import site.pegasis.ta.fetch.modes.server.storage.PCache
import site.pegasis.ta.fetch.modes.server.storage.UserDB
import site.pegasis.ta.fetch.modes.server.timeline.runFollowUpUpdate
import site.pegasis.ta.fetch.tools.*

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

    suspend fun route(session: HttpSession) {
        val timing = Timing()
        var statusCode = 200  //200:success  400:bad request  401:pwd incorrect  500:internal error
        var res = ""

        val hash = session.hashCode()
        val reqString = session.getReqString()
        val ipAddress = session.getIP()
        val reqApiVersion = session.getApiVersion()

        logInfo("Request #$hash /getmark_timeline <- $ipAddress, api version=$reqApiVersion, data=${reqString.removeBlank()}")

        if (session.isApiVersionInsufficient()) {
            session.send(426)
            logInfo("Request #$hash -> Api version insufficient")
            return
        }

        timing("init")

        try {
            with(ReqData(reqString, reqApiVersion)) {
                val courses = fetchUserCourseList(number, password, timing = timing)

                user?.let { UserDB.add(it) }
                timing("add user")
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
                    logWarn("Request #$hash :: Can't parse request: $reqString")
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
