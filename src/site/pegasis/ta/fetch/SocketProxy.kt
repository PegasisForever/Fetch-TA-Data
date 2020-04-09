package site.pegasis.ta.fetch

import kotlinx.coroutines.runBlocking
import site.pegasis.ta.fetch.modes.server.route.WebSocketSession
import site.pegasis.ta.fetch.tools.ANSI_BLUE
import site.pegasis.ta.fetch.tools.ANSI_CYAN
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.PrintWriter
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
    val server = ServerSocket(5001)
    while (true) {
        ThreadProxy(server.accept(), wsSession)
    }
}

/**
 * Handles a socket connection to the proxy server from the client and uses 2
 * threads to proxy between server and client
 *
 * @author jcgonzalez.com
 */
internal class ThreadProxy(private val sClient: Socket, private val wsSession: WebSocketSession) : Thread() {
    override fun run() {
        try {
            val request = ByteArray(1024)
            val inFromClient = sClient.getInputStream()
            val outToClient = sClient.getOutputStream()
            // connects a socket to the server
            val server = try {
                Socket("ta.yrdsb.ca", 443)
            } catch (e: IOException) {
                val out = PrintWriter(OutputStreamWriter(
                    outToClient))
                out.flush()
                throw RuntimeException(e)
            }
            // a new thread to manage streams from server to client (DOWNLOAD)
            val outToServer = server.getOutputStream()
            // a new thread for uploading to the server
            thread(start = true) {
                var bytes_read = 0
                while (inFromClient.read(request).also { bytes_read = it } != -1 ) {
                    val bytes = request.copyOf(bytes_read)

                    println(ANSI_CYAN + bytes.joinToString(""))
                    runBlocking { wsSession.send(bytes) }
                }

                outToServer.close()
            }

            try {
                while (true) {
                    val msg = runBlocking { wsSession.nextMessage() }
                    println(ANSI_BLUE + msg.joinToString(""))

                    outToClient.write(msg)
                    outToClient.flush()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    server.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            outToClient.close()
            sClient.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    init {
        start()
    }
}