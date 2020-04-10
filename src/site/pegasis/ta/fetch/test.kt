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
import site.pegasis.ta.fetch.modes.server.route.toWebSocketSession
import site.pegasis.ta.fetch.tools.io
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

suspend fun Closeable.closeSuspend() = io {
    close()
}

suspend fun ByteReadChannel.readAllLines(): String {
    val sb = StringBuffer()
    var line: String?
    while (readUTF8Line().also { line = it } != null) {
        sb.append(line).append("\n")
    }
    return sb.toString()
}

//TODO http parser https://stackoverflow.com/a/31600846/10874380
@KtorExperimentalAPI
fun main() {
    System.setProperty("kotlinx.coroutines.io.parallelism", "16")
    embeddedServer(Netty, 5000) {
        install(WebSockets)
        routing {
            webSocket("/") {
                val session = toWebSocketSession()
                val socketProxyJob = startSocketProxy(session, 5001, "ta.yrdsb.ca", 443)
                val socket = getSSLSocket("localhost", 5001)

                val toSocketChannel = socket.openWriteChannel(autoFlush = true)
                val fromSocketChannel = socket.openReadChannel()

                toSocketChannel.write("GET /yrdsb/ HTTP/1.0\n\n")
                println(fromSocketChannel.readAllLines())
                session.sendDisconnect()

                socketProxyJob.join()
                socket.closeSuspend()
                println("ws end")
            }
        }
    }.start(true)
}