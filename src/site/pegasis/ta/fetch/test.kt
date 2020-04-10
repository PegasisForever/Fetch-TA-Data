package site.pegasis.ta.fetch

import io.ktor.application.install
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.network.tls.tls
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.cio.write
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import site.pegasis.ta.fetch.modes.server.route.WebSocketSession
import site.pegasis.ta.fetch.modes.server.route.toWebSocketSession
import java.io.Closeable
import java.net.InetSocketAddress

fun CharArray.toByteArray(): ByteArray {
    val byteArray = ByteArray(size)
    forEachIndexed { i, char ->
        byteArray[i] = char.toByte()
    }

    return byteArray
}

@KtorExperimentalAPI
suspend fun getSSLSocket(host: String, port: Int) =
    aSocket(ActorSelectorManager(Dispatchers.IO))
        .tcp()
        .connect(InetSocketAddress(host, port))
        .tls(Dispatchers.IO)

suspend fun Closeable.closeSuspend() = withContext(Dispatchers.IO) { close() }

suspend fun ByteReadChannel.readAllLines(): String {
    val sb = StringBuffer()
    var line: String?
    while (readUTF8Line().also { line = it } != null) {
        sb.append(line).append("\n")
    }
    return sb.toString()
}

object PortPool {
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

val taServerAddress = InetSocketAddress("ta.yrdsb.ca", 443)

@KtorExperimentalAPI
suspend fun getRawHttpResponse(wsSession: WebSocketSession, rawRequest: String, server: InetSocketAddress): String? {
    val proxyPort = PortPool.getPort()
    val socketProxyJob = startSocketProxy(wsSession, proxyPort, server)
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

//TODO http parser https://stackoverflow.com/a/31600846/10874380
@KtorExperimentalAPI
fun main() {
    embeddedServer(Netty, 5000) {
        install(WebSockets)
        routing {
            webSocket("/") {
                val session = toWebSocketSession()
                getRawHttpResponse(session, "GET /yrdsb/ HTTP/1.0\n\n", taServerAddress)
            }
        }
    }.start(true)
}