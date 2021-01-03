package site.pegasis.ta.fetch.modes.server.route

import FeedbackDB
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import site.pegasis.ta.fetch.exceptions.ParseRequestException
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.tools.logError
import site.pegasis.ta.fetch.tools.logWarn

class Feedback : BaseRoute() {
    private class ReqData(req: String) {
        val contactInfo: String
        val feedback: String
        val platform: String
        val version: String

        init {
            try {
                val json = JSONParser().parse(req) as JSONObject
                contactInfo = json["contact_info"] as String
                feedback = json["feedback"] as String
                platform = (json["platform"] ?: "") as String
                version = (json["version"] ?: "") as String
            } catch (e: Exception) {
                throw ParseRequestException(e)
            }
        }
    }

    override fun path() = "/feedback"

    override suspend fun route(session: HttpSession, timing: Timing): Response {
        var status = 200  //200:success  400:bad request  500:internal error

        val hash = session.hashCode()
        val reqString = session.getReqString()

        timing("init")

        try {
            with(ReqData(reqString)) {
                FeedbackDB.add(contactInfo, feedback, platform, version)
            }
            timing("save feedback")
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
