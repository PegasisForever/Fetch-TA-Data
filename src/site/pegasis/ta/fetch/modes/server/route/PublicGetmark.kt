package site.pegasis.ta.fetch.modes.server.route

import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.exceptions.ParseRequestException
import site.pegasis.ta.fetch.fetchdata.fetchUserCourseList
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.modes.server.serializers.serializePublic
import site.pegasis.ta.fetch.modes.server.timeline.runFollowUpUpdate
import site.pegasis.ta.fetch.tools.*

class PublicGetMark(private val publicApiVersion: Int) : BaseRoute() {
    init {
        if (publicApiVersion != 1 && publicApiVersion != 2) {
            error("Unknown public api version: $publicApiVersion")
        }
    }

    private class ReqData(req: String) {
        val number: String
        val password: String

        init {
            try {
                val json = JSONParser().parse(req) as JSONObject
                number = json["number"] as String
                password = json["password"] as String
            } catch (e: Exception) {
                throw ParseRequestException(e)
            }
        }
    }

    override fun path() = if (publicApiVersion == 1) {
        "/getmark"
    } else {
        "/getmark_v2"
    }

    override fun isPublic() = true

    override suspend fun route(session: HttpSession, timing: Timing): Response {
        var statusCode = 200  //200:success  400:bad request  401:pwd incorrect  500:internal error
        var res = ""

        val hash = session.hashCode()
        val reqString = session.getReqString()

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

        return Response(statusCode, res, false)
    }
}
