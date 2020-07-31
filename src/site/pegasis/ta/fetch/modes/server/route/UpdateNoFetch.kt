package site.pegasis.ta.fetch.modes.server.route

import org.json.simple.JSONObject
import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.exceptions.ParseRequestException
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.models.User
import site.pegasis.ta.fetch.modes.server.serializers.serialize
import site.pegasis.ta.fetch.modes.server.storage.CourseListDB
import site.pegasis.ta.fetch.modes.server.storage.UserDB
import site.pegasis.ta.fetch.modes.server.storage.UserUpdateStatusDB
import site.pegasis.ta.fetch.tools.*

object UpdateNoFetch {
    private class ReqData(req: String, version: Int) {
        var user: User

        init {
            try {
                val json = jsonParser.parse(req) as JSONObject
                user = User.fromClient(json)
            } catch (e: Exception) {
                throw ParseRequestException()
            }
        }
    }

    suspend fun route(session: HttpSession){
        val timing = Timing()
        var statusCode = 200  //200:success  400:bad request  401:pwd incorrect  500:internal error
        var res = ""

        val hash = session.hashCode()
        val reqString = session.getReqString()
        val ipAddress = session.getIP()
        val reqApiVersion = session.getApiVersion()
        logInfo("Request #$hash /update_nofetch <- $ipAddress, api version=$reqApiVersion, data=$reqString")

        if (session.isApiVersionInsufficient(7)) {
            session.send(426)
            logInfo("Request #$hash /update_nofetch -> $ipAddress, Api version insufficient")
            return
        }

        timing("init")

        try {
            with(ReqData(reqString, reqApiVersion).user) {
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
                is LoginException ->{
                    logInfo("Request #$hash :: Student number or password incorrect")
                    401
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
