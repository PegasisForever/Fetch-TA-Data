package site.pegasis.ta.fetch

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import site.pegasis.ta.fetch.modes.server.route.WebSocketSession
import site.pegasis.ta.fetch.modes.server.route.WebsocketMessageType
import site.pegasis.ta.fetch.tools.*
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

fun startSocketProxy(wsSession: WebSocketSession, port: Int, targetHost: String, targetPort: Int): ServerSocket {
    val server = ServerSocket(port)
    GlobalScope.launch {
        noThrowSuspend {
            while (true) {
                val socket = io { server.accept() }
                runProxy(socket, wsSession, targetHost, targetPort)
            }
        }
    }

    return server
}

suspend fun InputStream.onData(action: suspend (data: ByteArray) -> Unit) {
    withContext(Dispatchers.IO) {
        val buffer = ByteArray(1024)
        var bytesRead: Int
        try {
            while (read(buffer).also { bytesRead = it } != -1) {
                launch(Dispatchers.Default) {
                    action(buffer.copyOf(bytesRead))
                }
            }
        } catch (e: Throwable) {
            println("inputstream")
            e.printStackTrace()
        }
    }
}

suspend fun WebSocketSession.onData(action: suspend (data: ByteArray, type: WebsocketMessageType) -> Unit) {
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

suspend fun OutputStream.writeSuspend(data: ByteArray, flush: Boolean) {
    withContext(Dispatchers.IO) {
        write(data)
        if (flush) flush()
    }
}

suspend fun Socket.closeSuspend() = withContext(Dispatchers.IO) {
    noThrow {
        close()
    }
}

suspend fun runProxy(client: Socket, wsSession: WebSocketSession, targetHost: String, targetPort: Int) {
    val fromClient = io { client.getInputStream() }
    val toClient = io { client.getOutputStream() }

    wsSession.send("${targetHost}:${targetPort}".toByteArray(), WebsocketMessageType.CONNECT)

    val socketToWsJob = GlobalScope.launch {
        fromClient.onData { data ->
            wsSession.send(data.gzip(), WebsocketMessageType.DATA)
        }
        println("s2w closed")
    }

    val wsToSocketJob = GlobalScope.launch {
        wsSession.onData { data, type ->
            withContext(Dispatchers.IO) {
                toClient.write(data.unGzip())
                toClient.flush()
            }
        }
        println("w2s closed")
    }

    println("job launched")
    socketToWsJob.join()
    wsToSocketJob.join()

    println("closing")
    wsSession.send(ByteArray(0), WebsocketMessageType.DISCONNECT)
    client.closeSuspend()

    println("closed")
}