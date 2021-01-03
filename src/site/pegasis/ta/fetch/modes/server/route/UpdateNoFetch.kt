package site.pegasis.ta.fetch.modes.server.route

import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.exceptions.ParseRequestException
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.models.User
import site.pegasis.ta.fetch.modes.server.serializers.serialize
import site.pegasis.ta.fetch.modes.server.storage.CourseListDB
import site.pegasis.ta.fetch.modes.server.storage.UserDB
import site.pegasis.ta.fetch.modes.server.storage.UserUpdateStatusDB
import site.pegasis.ta.fetch.tools.*

class UpdateNoFetch : BaseRoute() {
    private class ReqData(req: String) {
        var user: User

        init {
            try {
                val json = JSONParser().parse(req) as JSONObject
                user = User.fromClient(json)
            } catch (e: Exception) {
                throw ParseRequestException(e)
            }
        }
    }

    override fun path() = "/update_nofetch"

    override fun minApiVersion() = 7

    override suspend fun route(session: HttpSession, timing: Timing): Response {
        var statusCode = 200  //200:success  400:bad request  401:pwd incorrect  500:internal error
        var res = ""

        val hash = session.hashCode()
        val reqString = session.getReqString()
        val reqApiVersion = session.getApiVersion()

        timing("init")

        try {
            with(ReqData(reqString).user) {
                if (UserDB.validate(number, password)) {
                    res = JSONObject().apply {
                        this["time_line"] = CourseListDB.readTimeLine(number).serialize(reqApiVersion)
                        this["course_list"] = CourseListDB.readCourseList(number).serialize(reqApiVersion)
                        this["update_time"] = UserUpdateStatusDB.get(number).lastUpdateTime?.toJSONString()
                    }.toJSONString()
                    timing("join")
                } else {
                    throw LoginException(null)
                }
            }
        } catch (e: Throwable) {
            statusCode = when (e) {
                is ParseRequestException -> {
                    logWarn("Request #$hash :: Can't parse request: $reqString")
                    400
                }
                is LoginException -> {
                    logInfo("Request #$hash :: Student number or password incorrect")
                    401
                }
                else -> {
                    logError("Request #$hash :: Unknown error: ${e.message}", e)
                    500
                }
            }
        }

        return Response(statusCode, res)
    }
}
