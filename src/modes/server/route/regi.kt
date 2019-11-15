package modes.server.route

import LogLevel
import com.sun.net.httpserver.HttpExchange
import exceptions.UserParseException
import getApiVersion
import getIP
import getReqString
import jsonParser
import log
import models.LoginException
import models.User
import modes.server.PCache
import modes.server.serializers.serialize
import modes.server.updater.runFollowUpUpdate
import org.json.simple.JSONObject
import org.json.simple.parser.ParseException
import returnIfApiVersionInsufficient
import send
import webpage.LoginPage

var regiRoute = out@{ exchange: HttpExchange ->
    var statusCode = 200  //200:success  400:bad request  401:pwd incorrect  500:internal error
    var res = ""

    val hash = exchange.hashCode()
    val reqString = exchange.getReqString()
    val ipAddress = exchange.getIP()
    val reqApiVersion = exchange.getApiVersion()
    log(LogLevel.INFO, "Request #$hash /regi <- $ipAddress, api version=$reqApiVersion, data=$reqString")

    if (exchange.returnIfApiVersionInsufficient()) {
        log(LogLevel.INFO, "Request #$hash /regi -> $ipAddress, api version insufficient")
        return@out
    }

    try {
        val req = jsonParser.parse(reqString) as JSONObject
        val user = User.fromClient(req)

        val courses = LoginPage()
            .gotoSummaryPage(user.number, user.password)
            .fillDetails()
            .courses
        User.add(user)
        log(LogLevel.INFO, "Request #$hash /regi :: User verified successfully")
        runFollowUpUpdate(user.number, courses, hash, "/regi")

        res = JSONObject().apply {
            put("time_line", PCache.readTimeLine(user.number).serialize(reqApiVersion))
            put("course_list", PCache.readCourseList(user.number).serialize(reqApiVersion))
        }.toJSONString()
    } catch (e: LoginException) {
        log(LogLevel.INFO, "Request #$hash /regi :: Login error")
        statusCode = 401
    } catch (e: ParseException) {
        log(LogLevel.INFO, "Request #$hash /regi :: Can't parse request")
        statusCode = 400
    } catch (e: UserParseException) {
        log(LogLevel.INFO, "Request #$hash /regi :: Can't parse given user")
        statusCode = 400
    } catch (e: Exception) {
        log(LogLevel.ERROR, "Request #$hash /regi :: Unknown error: ${e.message}", e)
        statusCode = 500
    }

    log(LogLevel.INFO, "Request #$hash /regi -> $ipAddress, status=$statusCode, data=$res")
    exchange.send(statusCode, res)
}