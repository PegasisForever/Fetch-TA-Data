package site.pegasis.ta.fetch.modes.server.route

import com.sun.net.httpserver.HttpExchange
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.*
import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.exceptions.ParseRequestException
import site.pegasis.ta.fetch.modes.server.serializers.serializePublic
import site.pegasis.ta.fetch.modes.server.timeline.runFollowUpUpdate
import site.pegasis.ta.fetch.webpage.LoginPage

object PublicGetMark {
    private class ReqData(req: String) {
        val number: String
        val password: String

        init {
            try {
                val json = jsonParser.parse(req) as JSONObject
                number = json["number"] as String
                password = json["password"] as String
            } catch (e: Exception) {
                throw ParseRequestException()
            }
        }
    }

    val route = out@{ exchange: HttpExchange ->
        exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")

        if (exchange.requestMethod.toUpperCase() == "OPTIONS") {
            exchange.responseHeaders.add("Access-Control-Allow-Methods", "GET, OPTIONS")
            exchange.responseHeaders.add("Access-Control-Allow-Headers", "Content-Type,Authorization")
            exchange.sendResponseHeaders(204, -1)

            return@out
        }

        var statusCode = 200  //200:success  400:bad request  401:pwd incorrect  500:internal error
        var res = ""

        val hash = exchange.hashCode()
        val reqString = exchange.getReqString()
        val ipAddress = exchange.getIP()

        log(
            LogLevel.INFO,
            "Request #$hash /public/getmark <- $ipAddress, data=$reqString"
        )

        try {
            with(ReqData(reqString)) {
                val courses = LoginPage()
                    .gotoSummaryPage(number, password)
                    .fillDetails()
                    .courses
                res = courses.serializePublic().toJSONString()
                log(
                    LogLevel.INFO,
                    "Request #$hash /getmark :: Fetch successfully"
                )

                runFollowUpUpdate(number, courses)
            }
        } catch (e: LoginException) {
            log(
                LogLevel.INFO,
                "Request #$hash /getmark :: Login error"
            )
            statusCode = 401
        } catch (e: ParseRequestException) {
            log(
                LogLevel.INFO,
                "Request #$hash /getmark :: Can't parse request"
            )
            statusCode = 400
        } catch (e: Exception) {
            if (e.message?.indexOf("SocketTimeoutException") != -1) {
                log(
                    LogLevel.WARN,
                    "Request #$hash /getmark :: connect timeout",
                    e
                )
                statusCode = 503
            } else {
                log(
                    LogLevel.ERROR,
                    "Request #$hash /getmark :: Unknown error: ${e.message}",
                    e
                )
                statusCode = 500
            }
        }

        log(
            LogLevel.INFO,
            "Request #$hash /getmark -> $ipAddress, status=$statusCode, data=$res"
        )
        exchange.send(statusCode, res, false)
    }
}