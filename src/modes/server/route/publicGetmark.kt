package modes.server.route

import LogLevel
import com.sun.net.httpserver.HttpExchange
import getIP
import getReqString
import jsonParser
import log
import models.LoginException
import modes.server.serializers.serializePublic
import modes.server.updater.runFollowUpUpdate
import org.json.simple.JSONObject
import org.json.simple.parser.ParseException
import send
import webpage.LoginPage
import java.net.SocketTimeoutException

val publicGetmarkRoute = out@{ exchange: HttpExchange ->
    exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")

    if (exchange.requestMethod.toUpperCase()=="OPTIONS") {
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

    log(LogLevel.INFO, "Request #$hash /public/getmark <- $ipAddress, data=$reqString")

    try {
        val req = jsonParser.parse(reqString) as JSONObject

        val courses = LoginPage()
            .gotoSummaryPage(req["number"] as String, req["password"] as String)
            .fillDetails()
            .courses
        res = courses.serializePublic().toJSONString()
        log(LogLevel.INFO, "Request #$hash /getmark :: Fetch successfully")

        runFollowUpUpdate(req["number"] as String, courses, hash, "/getmark")
    } catch (e: LoginException) {
        log(LogLevel.INFO, "Request #$hash /getmark :: Login error")
        statusCode = 401
    } catch (e: ParseException) {
        log(LogLevel.INFO, "Request #$hash /getmark :: Can't parse request")
        statusCode = 400
    } catch (e: SocketTimeoutException) {
        log(LogLevel.WARN, "Request #$hash /getmark :: connect timeout", e)
        statusCode = 503
    } catch (e: Exception) {
        log(LogLevel.ERROR, "Request #$hash /getmark :: Unknown error: ${e.message}", e)
        statusCode = 500
    }

    log(
        LogLevel.INFO,
        "Request #$hash /getmark -> $ipAddress, status=$statusCode, data=$res"
    )
    exchange.send(statusCode, res, false)
}