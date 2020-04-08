package site.pegasis.ta.fetch.modes.server.route

import com.sun.net.httpserver.HttpExchange
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.request.httpMethod
import io.ktor.request.receiveText
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.util.pipeline.PipelineContext
import site.pegasis.ta.fetch.modes.server.latestApiVersion
import site.pegasis.ta.fetch.modes.server.minApiVersion
import site.pegasis.ta.fetch.tools.gzip
import java.nio.charset.StandardCharsets
import kotlin.math.max

fun HttpExchange.toHttpSession() = object : HttpSession {
    override suspend fun getReqString() = String(
        requestBody.readAllBytes(),
        StandardCharsets.UTF_8
    )

    override fun getIP() = if (requestHeaders.containsKey("X-real-ip")) {
        requestHeaders["X-real-ip"]?.get(0)
    } else {
        remoteAddress.address.toString()
    }

    override fun getApiVersion(): Int {
        var apiVersion = 1
        try {
            apiVersion = requestHeaders["api-version"]!![0].toInt()
            if (apiVersion > latestApiVersion) {
                apiVersion = 1
            }
        } catch (e: Exception) {
        }

        return apiVersion
    }

    override fun isApiVersionInsufficient(minApi: Int): Boolean {
        return getApiVersion() < Integer.max(minApi, minApiVersion)
    }

    override suspend fun send(status: Int, res: String, isGzip: Boolean) {
        send(
            status, if (res != "" && isGzip) {
            res.gzip()
        } else {
            res.toByteArray()
        }
        )
    }

    override suspend fun send(statusCode: Int, body: ByteArray) {
        sendResponseHeaders(statusCode, body.size.toLong())
        responseBody.write(body)
        responseBody.close()
    }

    override suspend fun makePublic(): Boolean {
        responseHeaders.add("Access-Control-Allow-Origin", "*")
        if (requestMethod.toUpperCase() == "OPTIONS") {
            responseHeaders.add("Access-Control-Allow-Methods", "GET, OPTIONS")
            responseHeaders.add("Access-Control-Allow-Headers", "Content-Type,Authorization")
            sendResponseHeaders(204, -1)
            return true
        }
        return false
    }
}

fun PipelineContext<Unit, ApplicationCall>.toHttpSession() = object : HttpSession {
    override suspend fun getReqString(): String {
        return call.receiveText()
    }

    override fun getIP(): String? {
        return call.request.header("X-real-ip") ?: call.request.local.remoteHost
    }

    override fun getApiVersion(): Int {
        return (call.request.header("api-version")?.toInt() ?: 1)
            .coerceAtMost(latestApiVersion)
    }

    override fun isApiVersionInsufficient(minApi: Int): Boolean {
        return getApiVersion() < max(minApi, minApiVersion)
    }

    override suspend fun send(status: Int, res: String, isGzip: Boolean) {
        send(
            status,
            if (res != "" && isGzip) {
                res.gzip()
            } else {
                res.toByteArray()
            }
        )
    }

    override suspend fun send(statusCode: Int, body: ByteArray) {
        call.respondBytes(body, status = HttpStatusCode(statusCode, ""))
    }

    override suspend fun makePublic(): Boolean {
        call.response.header("Access-Control-Allow-Origin", "*")
        if (call.request.httpMethod == HttpMethod.Options) {
            call.response.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            call.response.header("Access-Control-Allow-Headers", "Content-Type,Authorization")
            call.respond(HttpStatusCode.NoContent, "")
            return true
        }
        return false
    }

}