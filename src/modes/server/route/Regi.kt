package modes.server.route

import LogLevel
import com.sun.net.httpserver.HttpExchange
import exceptions.LoginException
import exceptions.UserParseException
import getApiVersion
import getIP
import getReqString
import jsonParser
import log
import models.User
import modes.server.serializers.serialize
import modes.server.storage.PCache
import modes.server.timeline.runFollowUpUpdate
import org.json.simple.JSONObject
import returnIfApiVersionInsufficient
import send
import webpage.LoginPage

object Regi {
    private class ReqData(req: String, version: Int) {
        val user: User

        init {
            try {
                val json = jsonParser.parse(req) as JSONObject
                user = User.fromClient(json)
            } catch (e: Exception) {
                throw ParseRequestException()
            }
        }
    }

    val route = out@{ exchange: HttpExchange ->
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
            with(ReqData(reqString, reqApiVersion).user) {
                val courses = LoginPage()
                    .gotoSummaryPage(number, password)
                    .fillDetails()
                    .courses
                User.add(this)
                log(LogLevel.INFO, "Request #$hash /regi :: User verified successfully")
                runFollowUpUpdate(number, courses, hash, "/regi")

                res = JSONObject().apply {
                    put("time_line", PCache.readTimeLine(number).serialize(reqApiVersion))
                    put("course_list", PCache.readCourseList(number).serialize(reqApiVersion))
                }.toJSONString()
            }
        } catch (e: LoginException) {
            log(LogLevel.INFO, "Request #$hash /regi :: Login error")
            statusCode = 401
        } catch (e: ParseRequestException) {
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
}