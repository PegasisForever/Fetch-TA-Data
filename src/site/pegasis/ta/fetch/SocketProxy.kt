package site.pegasis.ta.fetch

import kotlinx.coroutines.*
import site.pegasis.ta.fetch.modes.server.route.WebSocketSession
import site.pegasis.ta.fetch.tools.ANSI_BLUE
import site.pegasis.ta.fetch.tools.ANSI_CYAN
import site.pegasis.ta.fetch.tools.noThrow
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

fun main() {
    val server = ServerSocket(5002)
    while (true) {
        val socket = server.accept()
        val request = ByteArray(1024)
        val inputStream = socket.getInputStream()
        var bytes_read: Int
        try {
            while (inputStream.read(request).also { bytes_read = it } != -1) {
                println(ANSI_CYAN + request.copyOf(bytes_read).joinToString(""))
            }
        } catch (e: IOException) {
        }
    }

}

fun startSocketProxy(wsSession: WebSocketSession) {
    thread(start = true) {
        val server = ServerSocket(5001)
        while (true) {
            val socket = server.accept()
            runBlocking {
                ThreadProxy(socket, wsSession).run()
            }
        }
    }

}

/**
 * Handles a socket connection to the proxy server from the client and uses 2
 * threads to proxy between server and client
 *
 * @author jcgonzalez.com
 */

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

internal class ThreadProxy(private val client: Socket, private val wsSession: WebSocketSession) {
    suspend fun run() {
        try {
            val fromClient = client.getInputStream()
            val toClient = client.getOutputStream()

            val passDataJob = GlobalScope.launch {
                fromClient.onData { data: ByteArray ->
                    println(ANSI_CYAN + "send " + data.joinToString(""))
                    wsSession.send(data)
                }
            }

            val receiveDataJob = GlobalScope.launch {
                wsSession.onData { data: ByteArray ->
                    println(ANSI_BLUE + "receive " + data.joinToString(""))

                    toClient.writeSuspend(data, true)
                }
            }
            println("job launched")

            passDataJob.join()
            receiveDataJob.join()

            println("closing")
            fromClient.close()
            toClient.close()
            client.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}