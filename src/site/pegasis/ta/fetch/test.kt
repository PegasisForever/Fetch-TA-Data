package site.pegasis.ta.fetch

import io.ktor.application.install
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import site.pegasis.ta.fetch.modes.server.route.toWebSocketSession
import site.pegasis.ta.fetch.tools.io
import java.io.*
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

fun CharArray.toByteArray(): ByteArray {
    val byteArray = ByteArray(size)
    forEachIndexed { i, char ->
        byteArray[i] = char.toByte()
    }

    return byteArray
}

val sslFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
fun getSSLSocket(host: String, port: Int) = sslFactory.createSocket(host, port) as SSLSocket

suspend fun SSLSocket.handshakeSuspend() = io {
    startHandshake()
}

suspend fun Flushable.flushSuspend() = io {
    flush()
}

suspend fun BufferedReader.readTextSuspend() = io {
    readText()
}

suspend fun Closeable.closeSuspend() = io {
    close()
}

val SSLSocket.printWriter: PrintWriter
    get() = PrintWriter(BufferedWriter(OutputStreamWriter(outputStream)))

val SSLSocket.bufferedReader: BufferedReader
    get() = BufferedReader(InputStreamReader(inputStream))

fun main() {
    System.setProperty("kotlinx.coroutines.io.parallelism", "16")
    embeddedServer(Netty, 5000) {
        install(WebSockets)
        routing {
            webSocket("/") {
                val session = toWebSocketSession()
                val socketProxy = startSocketProxy(session, 5001)
                val socket = getSSLSocket("localhost", 5001)

                socket.handshakeSuspend()

                with(socket.printWriter) {
                    println("GET /yrdsb/ HTTP/1.0")
                    println()
                    flushSuspend()

                    if (checkError()) error("SSLSocketClient:  java.io.PrintWriter error")
                }

                with(socket.bufferedReader) {
                    val response = readTextSuspend()
                    println(response)
                }

                socketProxy.closeSuspend()
                socket.closeSuspend()
                println("ws closed")
            }
        }
    }.start(true)
}