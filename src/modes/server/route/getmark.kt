package modes.server.route

import LogLevel
import Serializers.CourseListSerializers
import com.sun.net.httpserver.HttpExchange
import getReqString
import jsonParser
import log
import models.LoginException
import org.json.simple.JSONObject
import org.json.simple.parser.ParseException
import send
import webpage.LoginPage

val getmarkRoute = { exchange: HttpExchange ->
    var statusCode = 200  //200:success  400:bad request  401:pwd incorrect  500:internal error
    var res = ""

    val hash = exchange.hashCode()
    val reqString = exchange.getReqString()
    val ipAddress = exchange.remoteAddress.address.toString()
    log(LogLevel.INFO, "Request #$hash /getmark <- $ipAddress, data=$reqString")

    try {
        val req = jsonParser.parse(reqString) as JSONObject

        val courses = LoginPage()
            .gotoSummaryPage(req["number"] as String, req["password"] as String)
            .fillDetails()
            .courses
        res=CourseListSerializers[2]?.invoke(courses)!!
        log(LogLevel.INFO, "Request #$hash /getmark :: Fetch successfully")
    } catch (e: LoginException) {
        log(LogLevel.INFO, "Request #$hash /getmark :: Login error")
        statusCode = 401
    } catch (e: ParseException) {
        log(LogLevel.INFO, "Request #$hash /getmark :: Can't parse request")
        statusCode = 400
    } catch (e: Exception) {
        log(LogLevel.ERROR, "Request #$hash /getmark :: Unknown error: ${e.message}", e)
        statusCode = 500
    }

    log(
        LogLevel.INFO,
        "Request #$hash /getmark -> $ipAddress, api version=$apiVersion, status=$statusCode, data=$res"
    )
    exchange.send(statusCode, res, apiVersion = apiVersion)
}