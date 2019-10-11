package modes.server.route

import com.sun.net.httpserver.HttpExchange
import getReqString
import jsonParser
import log
import models.User
import org.json.simple.JSONObject
import org.json.simple.parser.ParseException
import send

var deregiRoute={ exchange:HttpExchange ->
    var statusCode = 200  //200:success  400:bad request  500:internal error

    val hash = exchange.hashCode()
    val reqString = exchange.getReqString()
    val ipAddress = exchange.remoteAddress.address.toString()
    log(LogLevel.INFO, "Request #$hash /deregi <- $ipAddress, data=$reqString")

    try {
        val req = jsonParser.parse(reqString) as JSONObject
        val user = User.fromClient(req)

        User.remove(user)
    } catch (e: ParseException) {
        log(LogLevel.INFO, "Request #$hash /deregi :: Can't parse request")
        statusCode = 400
    } catch (e: Exception) {
        log(LogLevel.ERROR, "Request #$hash /deregi :: Unknown error: ${e.message}", e)
        statusCode = 500
    }

    log(LogLevel.INFO, "Request #$hash /deregi -> $ipAddress, status=$statusCode")
    exchange.send(statusCode, "")
}