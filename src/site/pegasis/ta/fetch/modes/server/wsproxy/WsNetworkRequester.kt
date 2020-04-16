package site.pegasis.ta.fetch.modes.server.wsproxy

import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.util.cio.write
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import site.pegasis.ta.fetch.fetchdata.NetworkRequester
import site.pegasis.ta.fetch.modes.server.session.WebSocketSession
import site.pegasis.ta.fetch.tools.noThrowSuspend
import site.pegasis.ta.fetch.tools.toURL

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

    private suspend fun getRawResponse(wsSession: WebSocketSession, rawRequest: String, server: String, port: Int): String? {
        val proxyPort = PortPool.getPort()
        val socketProxyJob = startWsSocketProxy(wsSession, proxyPort, server, port)
        val sslSocket = getSSLSocket("localhost", proxyPort)
        var response: String? = null

        try {
            sslSocket.openWriteChannel(autoFlush = true).write(rawRequest)
            response = sslSocket.openReadChannel().readAllLines()
        } catch (e: ClosedReceiveChannelException) {
        } catch (e: CancellationException) {
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        noThrowSuspend { sslSocket.closeSuspend() }
        noThrowSuspend { wsSession.sendDisconnect() }
        socketProxyJob.join()
        PortPool.releasePort(proxyPort)
        return response
    }

    private suspend fun httpGet(wsSession: WebSocketSession, urlString: String, cookies: HttpProtocol.Cookies = HttpProtocol.Cookies()): HttpProtocol.HttpResponse {
        val url = urlString.toURL()

        val rawRequest = HttpProtocol.createHttpGetRawRequest(url.host, url.path + (if (url.query != null) "?" + url.query else ""), cookies)
        val rawResponse = getRawResponse(wsSession, rawRequest, url.host, 443) ?: error("http error")

        return HttpProtocol.parseHttpRawResponse(rawResponse)
    }

    private suspend fun httpPost(wsSession: WebSocketSession, urlString: String, data: Map<String, Any> = emptyMap()): HttpProtocol.HttpResponse {
        val url = urlString.toURL()

        val rawRequest = HttpProtocol.createHttpPostRawRequest(url.host, url.path, data)
        val rawResponse = getRawResponse(wsSession, rawRequest, url.host, 443) ?: error("http error")

        return HttpProtocol.parseHttpRawResponse(rawResponse)
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