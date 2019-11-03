package modes.server.route

import LogLevel
import modes.server.serializers.CourseListSerializers
import com.sun.net.httpserver.HttpExchange
import getApiVersion
import getIP
import getReqString
import jsonParser
import log
import models.Course
import models.LoginException
import modes.server.serializers.TimeLineSerializers
import modes.server.timeline.TAUpdate
import modes.server.updater.runFollowUpUpdate
import org.json.simple.JSONObject
import org.json.simple.parser.ParseException
import readFile
import send
import webpage.LoginPage
import java.net.SocketTimeoutException

val getmarkTimelineRoute = { exchange: HttpExchange ->
    var statusCode = 200  //200:success  400:bad request  401:pwd incorrect  500:internal error
    var res = ""

    val hash = exchange.hashCode()
    val reqString = exchange.getReqString()
    val ipAddress = exchange.getIP()
    val reqApiVersion = exchange.getApiVersion()
    log(LogLevel.INFO, "Request #$hash /getmark_timeline <- $ipAddress, api version=$reqApiVersion, data=$reqString")

    try {
        val req = jsonParser.parse(reqString) as JSONObject
        val number = req["number"] as String

        val courses = LoginPage()
            .gotoSummaryPage(number, req["password"] as String)
            .fillDetails()
            .courses

        log(LogLevel.INFO, "Request #$hash /getmark_timeline :: Fetch successfully")
        val out=runFollowUpUpdate(number, courses, hash, "/getmark_timeline")

        res = CourseListSerializers[reqApiVersion]?.invoke(out["courselist"] as ArrayList<Course>)!! +"|||" +
                TimeLineSerializers[reqApiVersion]?.invoke(out["timeline"] as ArrayList<TAUpdate>)!!
    } catch (e: LoginException) {
        log(LogLevel.INFO, "Request #$hash /getmark_timeline :: Login error")
        statusCode = 401
    } catch (e: ParseException) {
        log(LogLevel.INFO, "Request #$hash /getmark_timeline :: Can't parse request")
        statusCode = 400
    } catch (e: SocketTimeoutException) {
        log(LogLevel.WARN, "Request #$hash /getmark_timeline :: connect timeout", e)
        statusCode = 503
    } catch (e: Exception) {
        log(LogLevel.ERROR, "Request #$hash /getmark_timeline :: Unknown error: ${e.message}", e)
        statusCode = 500
    }

    log(
        LogLevel.INFO,
        "Request #$hash /getmark_timeline -> $ipAddress, status=$statusCode, data=$res"
    )
    exchange.send(statusCode, res, reqApiVersion > 1)
}