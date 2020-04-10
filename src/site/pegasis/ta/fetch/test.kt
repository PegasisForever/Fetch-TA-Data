package site.pegasis.ta.fetch

import io.ktor.application.install
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.cio.write
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import site.pegasis.ta.fetch.HttpProtocol.createHttpGetRawRequest
import site.pegasis.ta.fetch.HttpProtocol.createHttpPostRawRequest
import site.pegasis.ta.fetch.HttpProtocol.parseHttpRawResponse
import site.pegasis.ta.fetch.fetchdata.NetworkRequester
import site.pegasis.ta.fetch.fetchdata.fetchUserCourseList
import site.pegasis.ta.fetch.modes.server.route.WebSocketSession
import site.pegasis.ta.fetch.modes.server.route.toWebSocketSession
import site.pegasis.ta.fetch.modes.server.serializers.serialize

object HttpProtocol {
    class Cookies : HashMap<String, String>()

    class Headers : ArrayList<Pair<String, String>>() {
        operator fun get(key: String): String {
            return find { it.first == key }?.second ?: ""
        }
    }

    data class HttpResponse(val statusCode: Int, val headers: Headers, val body: String) {
        fun getCookies(): Cookies {
            val cookies = Cookies()
            headers.filter { it.first == "Set-Cookie" }
                .map { it.second.substring(0 until it.second.indexOf(";")) }
                .map {
                    val parts = it.split("=")
                    parts[0] to parts[1]
                }
                .filter { it.second != "deleted" }
                .forEach {
                    cookies += it
                }
            return cookies
        }
    }

    private fun createHttpRawRequest(method: String, host: String, path: String, headers: Headers = Headers(), payload: String = ""): String {
        return with(StringBuilder()) {
            append(method)
            append(' ')
            append(path)
            append(" HTTP/1.1")
            newLine()

            append("Host: ")
            append(host)
            newLine()

            headers.forEach { (key, value) ->
                append("$key: $value")
                newLine()
            }

            newLine()
            append(payload)

            toString()
        }
    }

    fun createHttpGetRawRequest(host: String, path: String, cookies: Cookies = Cookies()): String {
        return if (cookies.isNotEmpty()) {
            val cookieHeaderString = cookies.map { "${it.key}=${it.value}" }.joinToString("; ")
            val headers = Headers()
            headers.add("Cookie" to cookieHeaderString)
            createHttpRawRequest("GET", host, path, headers)
        } else {
            createHttpRawRequest("GET", host, path)
        }
    }

    fun createHttpPostRawRequest(host: String, path: String, data: Map<String, Any> = emptyMap(), cookies: Cookies = Cookies()): String {
        val payload = data.map { "${it.key}=${it.value}" }.joinToString("&")

        val newHeaders = Headers()
        newHeaders.add("Content-Type" to "application/x-www-form-urlencoded")
        newHeaders.add("Content-Length" to payload.length.toString())
        if (cookies.isNotEmpty()) {
            val cookieHeaderString = cookies.map { "${it.key}=${it.value}" }.joinToString("; ")
            newHeaders.add("Cookie" to cookieHeaderString)
        }


        return createHttpRawRequest("POST", host, path, newHeaders, payload)
    }

    fun parseHttpRawResponse(rawResponse: String): HttpResponse {
        var statusCode = -1
        val headers = Headers()
        val bodySb = StringBuilder()

        var readingBody = false
        rawResponse
            .split("\n")
            .forEach { line ->
                if (statusCode == -1) {
                    statusCode = line.split(" ")[1].toInt()
                } else if (readingBody == false) {
                    val colonIndex = line.indexOf(":")
                    if (colonIndex >= 0) {
                        headers.add(line.substring(0 until colonIndex) to line.substring(colonIndex + 2))
                    } else {
                        readingBody = true
                    }
                } else {
                    bodySb.append(line).newLine()
                }
            }

        return HttpResponse(statusCode, headers, bodySb.toString())
    }
}

@KtorExperimentalAPI
class WsNetworkRequester(private val wsSession: WebSocketSession) : NetworkRequester {
    private val cookies = HttpProtocol.Cookies()
    override suspend fun post(url: String, data: Map<String, String>): String {
        val response = httpPost(wsSession, url, data)
        cookies.putAll(response.getCookies())

        return if (response.statusCode == 302) {
            get(response.headers["Location"])
        } else {
            response.body
        }
    }

    override suspend fun get(url: String): String {
        val response = httpGet(wsSession, url, cookies)
        return response.body
    }

    override suspend fun close() {}

    private suspend fun getRawHttpResponse(wsSession: WebSocketSession, rawRequest: String, server: String, port: Int): String? {
        val proxyPort = PortPool.getPort()
        val socketProxyJob = startSocketProxy(wsSession, proxyPort, server, port)
        var response: String? = null
        try {
            with(getSSLSocket("localhost", proxyPort)) {
                openWriteChannel(autoFlush = true).write(rawRequest)
                response = openReadChannel().readAllLines()
                closeSuspend()
            }

            wsSession.sendDisconnect()
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        socketProxyJob.join()
        PortPool.releasePort(proxyPort)
        return response
    }

    private suspend fun httpGet(wsSession: WebSocketSession, urlString: String, cookies: HttpProtocol.Cookies = HttpProtocol.Cookies()): HttpProtocol.HttpResponse {
        val url = urlString.toURL()

        val rawRequest = createHttpGetRawRequest(url.host, url.path + (if (url.query != null) "?" + url.query else ""), cookies)
        println(rawRequest)
        val rawResponse = getRawHttpResponse(wsSession, rawRequest, url.host, 443) ?: error("http error")

        return parseHttpRawResponse(rawResponse)
    }

    private suspend fun httpPost(wsSession: WebSocketSession, urlString: String, data: Map<String, Any> = emptyMap()): HttpProtocol.HttpResponse {
        val url = urlString.toURL()

        val rawRequest = createHttpPostRawRequest(url.host, url.path, data)
        val rawResponse = getRawHttpResponse(wsSession, rawRequest, url.host, 443) ?: error("http error")

        return parseHttpRawResponse(rawResponse)
    }

    companion object {
        private object PortPool {
            private val range = 5100..5200
            private val inUse = mutableSetOf<Int>()

            fun getPort(): Int {
                for (port in range) {
                    if (port !in inUse) {
                        inUse += port
                        return port
                    }
                }
                error("not enough port")
            }

            fun releasePort(port: Int) {
                inUse.remove(port)
            }
        }
    }
}

@KtorExperimentalAPI
fun main() {
    embeddedServer(Netty, 5000) {
        install(WebSockets)
        routing {
            webSocket("/") {
                val session = toWebSocketSession()

                val cl = fetchUserCourseList("", "",
                    requester = WsNetworkRequester(session))
                println(cl.serialize())
            }
        }
    }.start(true)
}