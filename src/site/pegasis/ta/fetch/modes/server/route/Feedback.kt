package site.pegasis.ta.fetch.modes.server.route

import org.json.simple.JSONObject
import site.pegasis.ta.fetch.exceptions.ParseRequestException
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.tools.*
import java.util.*

object Feedback {
    private class ReqData(req: String) {
        val contactInfo: String
        val feedback: String
        val platform: String
        val version: String

        init {
            try {
                val json = jsonParser.parse(req) as JSONObject
                contactInfo = json["contact_info"] as String
                feedback = json["feedback"] as String
                platform = (json["platform"] ?: "") as String
                version = (json["version"] ?: "") as String
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
        logInfo("Request #$hash /feedback <- $ipAddress, api version=$reqApiVersion, data=$reqString")

        if (session.isApiVersionInsufficient()) {
            session.send(426)
            logInfo("Request #$hash -> Api version insufficient")
            return
        }

        timing("init")

        try {
            with(ReqData(reqString)) {
                """
                    ${fileDateFormat.format(Date())}
                    Contact Info: $contactInfo
                    Feedback: $feedback
                    Platform: $platform
                    Version: $version
                    ----------------------------------------------
                    
                    
                """.trimIndent()
                    .appendToFile("data/feedback.txt")
                timing("write file")
            }
        } catch (e: Throwable) {
            statusCode = when (e) {
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

        session.send(statusCode)
        logInfo("Request #$hash -> status=$statusCode", timing = timing)
    }
}
