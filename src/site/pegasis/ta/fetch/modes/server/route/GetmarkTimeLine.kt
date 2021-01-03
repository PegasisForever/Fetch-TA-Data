package site.pegasis.ta.fetch.modes.server.route

import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.exceptions.ParseRequestException
import site.pegasis.ta.fetch.fetchdata.fetchUserCourseList
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.models.User
import site.pegasis.ta.fetch.modes.server.serializers.serialize
import site.pegasis.ta.fetch.modes.server.storage.CourseListDB
import site.pegasis.ta.fetch.modes.server.storage.UserDB
import site.pegasis.ta.fetch.modes.server.timeline.runFollowUpUpdate
import site.pegasis.ta.fetch.tools.*

class GetmarkTimeLine : BaseRoute() {
    private class ReqData(req: String, version: Int) {
        val number: String
        val password: String
        var user: User? = null

        init {
            try {
                val json = JSONParser().parse(req) as JSONObject
                if (version < 7) {
                    number = json["number"] as String
                    password = json["password"] as String
                } else {
                    user = User.fromClient(json)
                    number = user!!.number
                    password = user!!.password
                }
            } catch (e: Exception) {
                throw ParseRequestException(e)
            }
        }
    }

    override fun path() = "/getmark_timeline"

    override suspend fun route(session: HttpSession, timing: Timing): Response {
        var status = 200  //200:success  400:bad request  401:pwd incorrect  500:internal error
        var res = ""

        val hash = session.hashCode()
        val reqString = session.getReqString()
        val reqApiVersion = session.getApiVersion()

        timing("init")

        try {
            with(ReqData(reqString, reqApiVersion)) {
                val courses = fetchUserCourseList(number, password, timing = timing)

                user?.let { UserDB.add(it) }
                timing("add user")
                runFollowUpUpdate(number, courses)
                timing("update")

                res = JSONObject().apply {
                    put("time_line", CourseListDB.readTimeLine(number).serialize(reqApiVersion))
                    put("course_list", CourseListDB.readCourseList(number).serialize(reqApiVersion))
                }.toJSONString()
                timing("join")
            }
        } catch (e: Throwable) {
            status = when {
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

        return Response(status, res)
    }
}
