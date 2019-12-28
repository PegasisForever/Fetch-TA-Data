package site.pegasis.ta.fetch.modes.server.route

import com.sun.net.httpserver.HttpExchange
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.*
import java.util.*

object Feedback {
    private class ReqData(req: String) {
        val contactInfo: String
        val feedback: String

        init {
            try {
                val json = jsonParser.parse(req) as JSONObject
                contactInfo = json["contact_info"] as String
                feedback = json["feedback"] as String
            } catch (e: Exception) {
                throw ParseRequestException()
            }
        }
    }

    val route = out@{ exchange: HttpExchange ->
        var statusCode = 200  //200:success  400:bad request  500:internal error

        val hash = exchange.hashCode()
        val reqString = exchange.getReqString()
        val ipAddress = exchange.getIP()
        val reqApiVersion = exchange.getApiVersion()
        log(
            LogLevel.INFO,
            "Request #$hash /feedback <- $ipAddress, api version=$reqApiVersion, data=$reqString"
        )

        if (exchange.returnIfApiVersionInsufficient()) {
            log(
                LogLevel.INFO,
                "Request #$hash /feedback -> $ipAddress, api version insufficient"
            )
            return@out
        }

        try {
            with(ReqData(reqString)) {
                """
                    ${fileDateFormat.format(Date())}
                    Contact Info: $contactInfo
                    Feedback: $feedback
                    ----------------------------------------------
                    
                    
                """.trimIndent()
                    .appendToFile("data/feedback.txt")
            }
        } catch (e: ParseRequestException) {
            log(
                LogLevel.INFO,
                "Request #$hash /feedback :: Can't parse request"
            )
            statusCode = 400
        } catch (e: Exception) {
            log(
                LogLevel.ERROR,
                "Request #$hash /feedback :: Unknown error: ${e.message}",
                e
            )
            statusCode = 500
        }

        log(
            LogLevel.INFO,
            "Request #$hash /feedback -> $ipAddress, api version=$reqApiVersion, status=$statusCode"
        )
        exchange.send(statusCode, "")
    }
}