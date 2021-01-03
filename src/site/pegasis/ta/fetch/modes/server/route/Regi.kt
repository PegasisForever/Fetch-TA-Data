package site.pegasis.ta.fetch.modes.server.route

import org.json.simple.JSONObject
import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.exceptions.ParseRequestException
import site.pegasis.ta.fetch.exceptions.UserParseException
import site.pegasis.ta.fetch.fetchdata.fetchUserCourseList
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.models.User
import site.pegasis.ta.fetch.modes.server.serializers.serialize
import site.pegasis.ta.fetch.modes.server.storage.CourseListDB
import site.pegasis.ta.fetch.modes.server.storage.UserDB
import site.pegasis.ta.fetch.modes.server.timeline.runFollowUpUpdate
import site.pegasis.ta.fetch.testaccount.testUserAccount
import site.pegasis.ta.fetch.tools.*

class Regi : BaseRoute() {
    private class ReqData(req: String) {
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

    override fun path() = "/regi"

    override suspend fun route(session: HttpSession, timing: Timing): Response {
        var statusCode = 200  // 200:success  202:password is correct, but ta server is down  400:bad request  401:pwd incorrect  500:internal error
        var res = ""

        val hash = session.hashCode()
        val reqString = session.getReqString()
        val reqApiVersion = session.getApiVersion()

        timing("init")

        try {
            with(ReqData(reqString).user) {
                val courses = fetchUserCourseList(
                    number,
                    password,
                    timing = timing
                )

                UserDB.add(this)
                runFollowUpUpdate(number, courses)
                timing("update")

                res = JSONObject().apply {
                    put("time_line", CourseListDB.readTimeLine(number).serialize(reqApiVersion))
                    put("course_list", CourseListDB.readCourseList(number).serialize(reqApiVersion))
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
                e is UserParseException -> {
                    logInfo("Request #$hash :: Can't parse given user")
                    400
                }
                e.isConnectionException() -> {
                    logWarn("Request #$hash :: Connect timeout", e)
                    if (reqApiVersion >= 13) {
                        testAccount(reqString, hash, timing)
                    } else {
                        503
                    }
                }
                else -> {
                    logError("Request #$hash :: Unknown error: ${e.message}", e)
                    500
                }
            }
        }

        return Response(statusCode, res)
    }

    // return status code
    private suspend fun testAccount(reqString: String, hash: Int, timing: Timing): Int {
        return try {
            val isPasswordCorrect = with(ReqData(reqString).user) {
                UserDB.validate(number, password) || testUserAccount(number, password, timing)
            }
            logInfo("Request #$hash :: User password is ${if (isPasswordCorrect) "correct" else "incorrect"}")
            if (isPasswordCorrect) {
                202
            } else {
                401
            }
        } catch (e: Throwable) {
            when {
                e.isConnectionException() -> {
                    logWarn("Request #$hash :: Test user account timeout", e)
                    503
                }
                else -> {
                    logError("Request #$hash :: Unknown error: ${e.message}", e)
                    500
                }
            }
        }
    }
}
