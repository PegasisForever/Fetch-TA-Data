package site.pegasis.ta.fetch.modes

import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.exceptions.ParseRequestException
import site.pegasis.ta.fetch.jsonParser
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.modes.server.serializers.serialize
import site.pegasis.ta.fetch.readText
import site.pegasis.ta.fetch.webpage.chrome.LoginPage

private class ReqData(req: String) {
    val number: String
    val password: String
    val apiVersion: Int

    init {
        try {
            val json = jsonParser.parse(req) as JSONObject
            number = json["number"] as String
            password = json["password"] as String
            apiVersion = (json["version"] as Long).toInt()
        } catch (e: Exception) {
            throw ParseRequestException()
        }
    }
}

private fun rawPoll(req: String): Pair<Int, String> {
    var res = ""
    var statusCode = 200

    println("req: $req")

    try {
        with(ReqData(req)) {
            val timing = Timing()
            res = LoginPage(timing)
                .gotoSummaryPage(number, password)
                .fillDetails(doCalculation = false)
                .courses
                .serialize(apiVersion)
                .toJSONString()
        }
    } catch (e: Throwable) {
        statusCode = when {
            e is LoginException -> 401
            e is ParseRequestException -> 400
            e.message?.indexOf("SocketTimeoutException") != -1 -> 503
            else -> {
                //log
                500
            }
        }
    }

    return statusCode to res
}

class RawPollCloudFunction : HttpFunction {
    override fun service(httpRequest: HttpRequest, httpResponse: HttpResponse) {
        val reqString = httpRequest.inputStream.readText()
        val (first, second) = rawPoll(reqString)
        httpResponse.setStatusCode(first)
        httpResponse.writer.write(second)
    }
}