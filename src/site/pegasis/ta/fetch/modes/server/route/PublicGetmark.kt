package site.pegasis.ta.fetch.modes.server.route

import org.json.simple.JSONObject
import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.exceptions.ParseRequestException
import site.pegasis.ta.fetch.fetchdata.fetchUserCourseList
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.modes.server.serializers.serializePublic
import site.pegasis.ta.fetch.modes.server.session.HttpSession
import site.pegasis.ta.fetch.modes.server.timeline.runFollowUpUpdate
import site.pegasis.ta.fetch.tools.*

object PublicGetMark {
    private class ReqData(req: String) {
        val number: String
        val password: String

        init {
            try {
                val json = jsonParser.parse(req) as JSONObject
                number = json["number"] as String
                password = json["password"] as String
            } catch (e: Exception) {
                throw ParseRequestException()
            }
        }
    }

    suspend fun routeV1(session: HttpSession) = route(1, session)
    suspend fun routeV2(session: HttpSession) = route(2, session)

    suspend fun route(publicApiVersion: Int, session: HttpSession) {
        if (session.makePublic()) {
            return
        }

        val timing = Timing()
        var statusCode = 200  //200:success  400:bad request  401:pwd incorrect  500:internal error
        var res = ""

        val hash = session.hashCode()
        val reqString = session.getReqString()
        val ipAddress = session.getIP()
        logInfo("Request #$hash /public/getmark <- $ipAddress, data=$reqString, api version=$publicApiVersion")

        timing("init")

        try {
            with(ReqData(reqString)) {
                val courses = fetchUserCourseList(number, password, timing = timing)

                runFollowUpUpdate(number, courses)
                timing("update")

                res = courses.serializePublic(publicApiVersion).toJSONString()
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

        session.send(statusCode, res, false)
        timing("send")
        logInfo("Request #$hash -> status=$statusCode", timing = timing)
    }
}