package site.pegasis.ta.fetch

import io.ktor.application.install
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.*
import site.pegasis.ta.fetch.modes.server.route.WebSocketSession
import site.pegasis.ta.fetch.modes.server.route.toWebSocketSession
import site.pegasis.ta.fetch.tools.ANSI_BLUE
import site.pegasis.ta.fetch.tools.ANSI_CYAN
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

val charSet = Charset.forName("UTF-8")

suspend fun connect(wsSession2: WebSocketSession, port: Int) = withContext(Dispatchers.IO) {
    val serverSocket = ServerSocket(port)
    while (!serverSocket.isClosed) {
        val proxySocket = serverSocket.accept()
        val toClient = OutputStreamWriter(proxySocket.getOutputStream())
        val fromClient = BufferedReader(InputStreamReader(proxySocket.getInputStream()))

        val remoteSocket = Socket("ta.yrdsb.ca", 443)
        val toRemote = OutputStreamWriter(remoteSocket.getOutputStream())
        val fromRemote = BufferedReader(InputStreamReader(remoteSocket.getInputStream()))


        val remoteToClient = launch(Dispatchers.IO) {
            var readBytes: Int
            val buffer = CharArray(4096)
            while (fromRemote.read(buffer).also { readBytes = it } >= 0) {
                println(ANSI_BLUE + buffer.copyOf(readBytes).joinToString(", "))
                toClient.write(buffer.copyOf(readBytes))
                toClient.flush()
            }
        }

        val clientToRemote = launch(Dispatchers.IO) {
            var readBytes: Int
            val buffer = CharArray(4096)
            while (fromClient.read(buffer).also { readBytes = it } >= 0) {
                println(ANSI_CYAN + buffer.copyOf(readBytes).joinToString(", "))
                toRemote.write(buffer.copyOf(readBytes))
                toRemote.flush()
            }
        }
        clientToRemote.join()
        remoteToClient.cancelAndJoin()
    }
}

fun CharArray.toByteArray(): ByteArray {
    val byteArray = ByteArray(size)
    forEachIndexed { i, char ->
        byteArray[i] = char.toByte()
    }

    return byteArray
}

fun main() {
    System.setProperty("kotlinx.coroutines.io.parallelism","16")
    embeddedServer(Netty, 5000) {
        install(WebSockets)
        routing {
            webSocket("/") {
                val session = this.toWebSocketSession()
                GlobalScope.launch {
                    while (isActive) {
                        session.send(ByteArray(0))
                        delay(500)
                    }
                }

                startSocketProxy(session)

                delay(100)
//                val connectJob = GlobalScope.launch { connect(session, 5001) }

                val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
                val socket = factory.createSocket("localhost", 5001) as SSLSocket
//                val socket = factory.createSocket("ta.yrdsb.ca", 443) as SSLSocket

                socket.startHandshake()

                val socketWriter = PrintWriter(BufferedWriter(OutputStreamWriter(socket.outputStream)))
                socketWriter.println("GET /yrdsb/ HTTP/1.0")
                socketWriter.println()
                socketWriter.flush()

                if (socketWriter.checkError()) println("SSLSocketClient:  java.io.PrintWriter error")

                val socketReader = BufferedReader(InputStreamReader(socket.inputStream))
                var inputLine: String?
                while (socketReader.readLine().also { inputLine = it } != null) println(inputLine)

                socketWriter.close()
                socketReader.close()
                socket.close()
//                connectJob.cancelAndJoin()
            }
        }
    }.start(true)
}