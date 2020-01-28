package site.pegasis.ta.fetch.modes.server.route

import com.sun.net.httpserver.HttpExchange
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.*
import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.exceptions.ParseRequestException
import site.pegasis.ta.fetch.models.User
import site.pegasis.ta.fetch.modes.server.serializers.serialize
import site.pegasis.ta.fetch.modes.server.storage.PCache

object GetArchived {
    private class ReqData(req: String, version: Int) {
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
        var statusCode = 200  //200:success  400:bad request  401:pwd incorrect  500:internal error
        var res = ""

        val hash = exchange.hashCode()
        val reqString = exchange.getReqString()
        val ipAddress = exchange.getIP()
        val reqApiVersion = exchange.getApiVersion()
        log(
            LogLevel.INFO,
            "Request #$hash /getarchived <- $ipAddress, api version=$reqApiVersion, data=$reqString"
        )

        if (exchange.returnIfApiVersionInsufficient()) {
            log(
                LogLevel.INFO,
                "Request #$hash /getarchived -> $ipAddress, api version insufficient"
            )
            return@out
        }

        try {
            with(ReqData(reqString, reqApiVersion)) {
                val validated = User.validate(number, password)
                if (validated) {
                    res = PCache.readArchivedCourseList(number).serialize(reqApiVersion).toJSONString()
                } else {
                    throw LoginException(null)
                }
            }
        } catch (e: LoginException) {
            log(
                LogLevel.INFO,
                "Request #$hash /getarchived :: student number or password incorrect"
            )
            statusCode = 401
        } catch (e: Exception) {
            log(
                LogLevel.ERROR,
                "Request #$hash /getarchived :: Unknown error: ${e.message}",
                e
            )
            statusCode = 500
        }

        log(
            LogLevel.INFO,
            "Request #$hash /getarchived -> $ipAddress, status=$statusCode, data=$res"
        )
        exchange.send(statusCode, res)
    }
}