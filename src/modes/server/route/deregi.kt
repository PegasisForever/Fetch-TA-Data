package modes.server.route

import LogLevel
import com.sun.net.httpserver.HttpExchange
import exceptions.UserParseException
import getApiVersion
import getIP
import getReqString
import jsonParser
import log
import models.User
import org.json.simple.JSONObject
import org.json.simple.parser.ParseException
import returnIfApiVersionInsufficient
import send

var deregiRoute = out@{ exchange: HttpExchange ->
    var statusCode = 200  //200:success  400:bad request  500:internal error

    val hash = exchange.hashCode()
    val reqString = exchange.getReqString()
    val ipAddress = exchange.getIP()
    val reqApiVersion = exchange.getApiVersion()
    log(LogLevel.INFO, "Request #$hash /deregi <- $ipAddress, api version=$reqApiVersion, data=$reqString")

    if (exchange.returnIfApiVersionInsufficient()) {
        log(LogLevel.INFO, "Request #$hash /deregi -> $ipAddress, api version insufficient")
        return@out
    }

    try {
        val req = jsonParser.parse(reqString) as JSONObject
        val user = User.fromClient(req)

        User.remove(user)
    } catch (e: ParseException) {
        log(LogLevel.INFO, "Request #$hash /deregi :: Can't parse request")
        statusCode = 400
    } catch (e: UserParseException){
        log(LogLevel.INFO, "Request #$hash /regi :: Can't parse given user")
        statusCode = 400
    } catch (e: Exception) {
        log(LogLevel.ERROR, "Request #$hash /deregi :: Unknown error: ${e.message}", e)
        statusCode = 500
    }

    log(LogLevel.INFO, "Request #$hash /deregi -> $ipAddress, api version=$reqApiVersion, status=$statusCode")
    exchange.send(statusCode, "")
}