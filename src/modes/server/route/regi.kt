package modes.server.route

import LogLevel
import Serializers.CourseListSerializers
import com.sun.net.httpserver.HttpExchange
import exceptions.UserParseException
import getReqString
import jsonParser
import log
import models.LoginException
import models.User
import org.json.simple.JSONObject
import org.json.simple.parser.ParseException
import send
import webpage.LoginPage

var regiRoute={ exchange:HttpExchange ->
    var statusCode = 200  //200:success  400:bad request  401:pwd incorrect  500:internal error
    var res = ""

    val hash = exchange.hashCode()
    val reqString = exchange.getReqString()
    val ipAddress = exchange.remoteAddress.address.toString()
    log(LogLevel.INFO, "Request #$hash /regi <- $ipAddress, data=$reqString")

    try {
        val req = jsonParser.parse(reqString) as JSONObject
        val user = User.fromClient(req)

        val courses = LoginPage()
            .gotoSummaryPage(user.number, user.password)
            .fillDetails()
            .courses
        res= CourseListSerializers[2]?.invoke(courses)!!

        log(LogLevel.INFO, "Request #$hash /regi :: User verified successfully")

        User.add(user)

    } catch (e: LoginException) {
        log(LogLevel.INFO, "Request #$hash /regi :: Login error")
        statusCode = 401
    } catch (e: ParseException) {
        log(LogLevel.INFO, "Request #$hash /regi :: Can't parse request")
        statusCode = 400
    } catch (e: UserParseException){
        log(LogLevel.INFO, "Request #$hash /regi :: Can't parse given user")
        statusCode = 400
    } catch (e: Exception) {
        log(LogLevel.ERROR, "Request #$hash /regi :: Unknown error: ${e.message}", e)
        statusCode = 500
    }

    log(LogLevel.INFO, "Request #$hash /regi -> $ipAddress, api version=$apiVersion, status=$statusCode, data=$res")
    exchange.send(statusCode, res, apiVersion = apiVersion)
}