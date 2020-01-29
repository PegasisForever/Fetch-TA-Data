package site.pegasis.ta.fetch.modes.server.route

import com.sun.net.httpserver.HttpExchange
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.*
import site.pegasis.ta.fetch.exceptions.ParseRequestException
import site.pegasis.ta.fetch.models.Timing
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

    val route = out@{ exchange: HttpExchange ->
        val timing = Timing()
        var statusCode = 200  //200:success  400:bad request  500:internal error

        val hash = exchange.hashCode()
        val reqString = exchange.getReqString()
        val ipAddress = exchange.getIP()
        val reqApiVersion = exchange.getApiVersion()
        logInfo("Request #$hash /feedback <- $ipAddress, api version=$reqApiVersion, data=$reqString")

        if (exchange.returnIfApiVersionInsufficient()) {
            logInfo("Request #$hash -> api version insufficient")
            return@out
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
        } catch (e: Exception) {
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

        exchange.send(statusCode)
        logInfo("Request #$hash -> status=$statusCode", timing = timing)
    }
}