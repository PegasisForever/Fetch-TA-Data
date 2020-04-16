package site.pegasis.ta.fetch.modes.server.route

import org.json.simple.JSONObject
import site.pegasis.ta.fetch.exceptions.ParseRequestException
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.models.User
import site.pegasis.ta.fetch.modes.server.session.HttpSession
import site.pegasis.ta.fetch.tools.jsonParser
import site.pegasis.ta.fetch.tools.logError
import site.pegasis.ta.fetch.tools.logInfo

object Deregi {
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

    suspend fun route(session: HttpSession){
        val timing = Timing()
        var statusCode = 200  //200:success  400:bad request  500:internal error

        val hash = session.hashCode()
        val reqString = session.getReqString()
        val ipAddress = session.getIP()
        val reqApiVersion = session.getApiVersion()
        logInfo("Request #$hash /deregi <- $ipAddress, api version=$reqApiVersion, data=$reqString")

        if (session.isApiVersionInsufficient()) {
            session.send(426)
            logInfo("Request #$hash /deregi -> Api version insufficient")
            return
        }

        timing("init")

        try {
            val user = ReqData(reqString, reqApiVersion).user
            User.remove(user)
            timing("remove user")
        } catch (e: Throwable) {
            statusCode = when (e) {
                is ParseRequestException -> {
                    logInfo("Request #$hash :: Can't parse request")
                    400
                }
                else -> {
                    logError("Request #$hash :: Unknown error: ${e.message}", e)
                    500
                }
            }
        }

        session.send(statusCode)
        logInfo("Request #$hash -> status=$statusCode", timing = timing)
    }
}