package site.pegasis.ta.fetch.modes.server.route

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import site.pegasis.ta.fetch.modes.server.LATEST_API_VERSION
import site.pegasis.ta.fetch.modes.server.MIN_API_VERSION
import site.pegasis.ta.fetch.tools.gzip
import kotlin.math.max

fun PipelineContext<Unit, ApplicationCall>.toHttpSession() = object : HttpSession() {
    var receivedText: String? = null

    override suspend fun getReqString(): String {
        if (receivedText == null) {
            receivedText = call.receiveText()
        }
        return receivedText!!
    }

    override fun getIP(): String {
        return call.request.header("X-real-ip") ?: call.request.local.remoteHost
    }

    override fun getApiVersion(): Int {
        return (call.request.header("api-version")?.toInt() ?: 1)
            .coerceAtMost(LATEST_API_VERSION)
    }

    override fun isApiVersionInsufficient(minApi: Int): Boolean {
        return getApiVersion() < max(minApi, MIN_API_VERSION)
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

    override suspend fun send(status: Int, body: ByteArray) {
        call.respondBytes(body, status = HttpStatusCode(status, ""))
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
