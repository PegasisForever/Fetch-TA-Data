package site.pegasis.ta.fetch

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.util.KtorExperimentalAPI
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import site.pegasis.ta.fetch.modes.server.route.WebSocketSession
import site.pegasis.ta.fetch.modes.server.route.WebsocketMessageType
import site.pegasis.ta.fetch.tools.gzip
import site.pegasis.ta.fetch.tools.noThrow
import site.pegasis.ta.fetch.tools.unGzip
import java.net.InetSocketAddress
import java.nio.ByteBuffer

@KtorExperimentalAPI
fun getServerSocket(port: Int) = aSocket(ActorSelectorManager(Dispatchers.IO))
    .tcp()
    .bind(InetSocketAddress("localhost", port))

@KtorExperimentalAPI
fun startSocketProxy(wsSession: WebSocketSession, port: Int, targetHost: String,targetPort: Int): Job {
    return GlobalScope.launch {
        val serverSocket = getServerSocket(port)
        val socket = serverSocket.accept()
        runProxy(socket, wsSession, targetHost, targetPort)
        serverSocket.closeSuspend()
    }
}

suspend fun ByteReadChannel.forEachData(action: suspend (data: ByteArray) -> Unit) {
    val buffer = ByteBuffer.allocate(4096)

    var bytesRead: Int
    try {
        while (readAvailable(buffer).also { bytesRead = it } != -1) {
            action(buffer.toByteArray(bytesRead))
            buffer.position(0)
        }
    } catch (e: ClosedReceiveChannelException) {
    } catch (e: CancellationException) {
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

suspend fun WebSocketSession.forEachData(action: suspend (data: ByteArray, type: WebsocketMessageType) -> Unit) {
    try {
        while (true) {
            val (data, type) = nextMessage()
            action(data, type)
        }
    } catch (e: ClosedReceiveChannelException) {
    } catch (e: CancellationException) {
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

suspend fun Socket.closeSuspend() = withContext(Dispatchers.IO) {
    noThrow {
        close()
    }
}

suspend fun ByteWriteChannel.write(byteArray: ByteArray) = writeFully(ByteBuffer.wrap(byteArray))

fun ByteBuffer.toByteArray(size: Int): ByteArray {
    return array().copyOf(size)
}

suspend fun runProxy(client: Socket, wsSession: WebSocketSession, targetHost: String, targetPort: Int) {
    val fromClient = client.openReadChannel()
    val toClient = client.openWriteChannel(autoFlush = true)

    wsSession.send("${targetHost}:${targetPort}".toByteArray(), WebsocketMessageType.CONNECT)

    val socketToWsJob = GlobalScope.launch {
        fromClient.forEachData { data ->
            wsSession.send(data.gzip(), WebsocketMessageType.DATA)
        }
        println("proxy s2w closed")
    }

    val wsToSocketJob = GlobalScope.launch {
        wsSession.forEachData { data, type ->
            when (type) {
                WebsocketMessageType.DATA -> toClient.write(data.unGzip())
                WebsocketMessageType.DISCONNECT -> throw CancellationException()
            }
        }
        client.closeSuspend()
        println("proxy w2s closed")
    }

    println("proxy launched")
    socketToWsJob.join()
    wsToSocketJob.join()

    println("proxy closed")
}