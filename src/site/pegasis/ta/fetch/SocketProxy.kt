package site.pegasis.ta.fetch

import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import site.pegasis.ta.fetch.modes.server.route.WebSocketSession
import site.pegasis.ta.fetch.modes.server.route.WebsocketMessageType
import site.pegasis.ta.fetch.tools.gzip
import site.pegasis.ta.fetch.tools.logDebug
import site.pegasis.ta.fetch.tools.unGzip

@KtorExperimentalAPI
fun startSocketProxy(wsSession: WebSocketSession, port: Int, targetHost: String, targetPort: Int): Job {
    val serverSocket = getServerSocket(port)
    return GlobalScope.launch {
        val socket = serverSocket.accept()
        runProxy(socket, wsSession, targetHost, targetPort)
        serverSocket.closeSuspend()
    }
}

suspend fun runProxy(client: Socket, wsSession: WebSocketSession, targetHost: String, targetPort: Int) {
    val fromClient = client.openReadChannel()
    val toClient = client.openWriteChannel(autoFlush = true)

    wsSession.send("${targetHost}:${targetPort}".toByteArray(), WebsocketMessageType.CONNECT)

    val socketToWsJob = GlobalScope.launch {
        fromClient.forEachData { data ->
            wsSession.send(data.gzip(), WebsocketMessageType.DATA)
        }
        logDebug("proxy s2w closed")
    }

    val wsToSocketJob = GlobalScope.launch {
        wsSession.forEachData { data, type ->
            when (type) {
                WebsocketMessageType.DATA -> toClient.write(data.unGzip())
                WebsocketMessageType.DISCONNECT -> throw CancellationException()
            }
        }
        client.closeSuspend()
        logDebug("proxy w2s closed")
    }

    logDebug("proxy launched")
    socketToWsJob.join()
    wsToSocketJob.join()

    logDebug("proxy closed")
}