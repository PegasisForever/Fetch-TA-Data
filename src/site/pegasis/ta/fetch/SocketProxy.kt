package site.pegasis.ta.fetch

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import site.pegasis.ta.fetch.modes.server.route.WebSocketSession
import site.pegasis.ta.fetch.tools.*
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

fun startSocketProxy(wsSession: WebSocketSession, port: Int): ServerSocket {
    val server = ServerSocket(port)
    GlobalScope.launch {
        noThrowSuspend {
            while (true) {
                val socket = io { server.accept() }
                runProxy(socket, wsSession)
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

suspend fun WebSocketSession.onData(action: suspend (data: ByteArray) -> Unit) {
    try {
        while (true) {
            val data = nextMessage()
            action(data)
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

suspend fun InputStream.closeSuspend() = withContext(Dispatchers.IO) {
    noThrow {
        close()
    }
}

suspend fun OutputStream.closeSuspend() = withContext(Dispatchers.IO) {
    noThrow {
        close()
    }
}

suspend fun Socket.closeSuspend() = withContext(Dispatchers.IO) {
    noThrow {
        close()
    }
}

suspend fun runProxy(client: Socket, wsSession: WebSocketSession) {
    val fromClient = io { client.getInputStream() }
    val toClient = io { client.getOutputStream() }

    val socketToWsJob = GlobalScope.launch {
        fromClient.onData { data: ByteArray ->
            wsSession.send(data.gzip())
        }
    }

    val wsToSocketJob = GlobalScope.launch {
        wsSession.onData { data: ByteArray ->
            withContext(Dispatchers.IO) {
                toClient.write(data.unGzip())
                toClient.flush()
            }
        }
    }

    println("job launched")
    socketToWsJob.join()
    wsToSocketJob.join()


    println("closing")
    client.closeSuspend()

    println("closed")
}