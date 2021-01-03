package site.pegasis.ta.fetch.modes.server.route

import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.exceptions.ParseRequestException
import site.pegasis.ta.fetch.models.CourseList
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.modes.server.serializers.serialize
import site.pegasis.ta.fetch.modes.server.storage.CourseListDB
import site.pegasis.ta.fetch.modes.server.storage.UserDB
import site.pegasis.ta.fetch.tools.logError
import site.pegasis.ta.fetch.tools.logInfo

class GetArchived : BaseRoute() {
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

    override fun path() = "/getarchived"

    override suspend fun route(session: HttpSession, timing: Timing): Response {
        var statusCode = 200  //200:success  400:bad request  401:pwd incorrect  500:internal error
        var res = ""

        val hash = session.hashCode()
        val reqString = session.getReqString()
        val reqApiVersion = session.getApiVersion()

        timing("init")

        try {
            with(ReqData(reqString)) {
                if (UserDB.validate(number, password)) {
                    res = if (reqApiVersion == 6) {
                        CourseList(CourseListDB.readArchivedCourseList(number)
                            .filter { it.overallMark != null })
                            .serialize(reqApiVersion)
                            .toJSONString()
                    } else {
                        CourseListDB.readArchivedCourseList(number)
                            .serialize(reqApiVersion)
                            .toJSONString()
                    }
                    timing("join")
                } else {
                    throw LoginException(null)
                }
            }
        } catch (e: Throwable) {
            statusCode = when (e) {
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
