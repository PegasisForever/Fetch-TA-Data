package site.pegasis.ta.fetch.modes.server.route

import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import site.pegasis.ta.fetch.exceptions.ParseRequestException
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.models.User
import site.pegasis.ta.fetch.modes.server.storage.UserDB
import site.pegasis.ta.fetch.tools.logError
import site.pegasis.ta.fetch.tools.logWarn

class Deregi : BaseRoute() {
    private class ReqData(req: String) {
        val user: User

        init {
            try {
                val json = JSONParser().parse(req) as JSONObject
                user = User.fromClient(json)
            } catch (e: Exception) {
                throw ParseRequestException(e)
            }
        }
    }

    override fun path() = "/deregi"

    override suspend fun route(session: HttpSession, timing: Timing):Response{
        var status = 200  //200:success  400:bad request  500:internal error

        val hash = session.hashCode()
        val reqString = session.getReqString()

        timing("init")

        try {
            val user = ReqData(reqString).user
            UserDB.remove(user)
            timing("remove user")
        } catch (e: Throwable) {
            status = when (e) {
                is ParseRequestException -> {
                    logWarn("Request #$hash :: Can't parse request: $reqString")
                    400
                }
                else -> {
                    logError("Request #$hash :: Unknown error: ${e.message}", e)
                    500
                }
            }
        }

        return Response(status)
    }
}
