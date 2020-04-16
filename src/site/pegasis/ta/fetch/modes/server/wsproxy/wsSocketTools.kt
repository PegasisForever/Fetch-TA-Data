package site.pegasis.ta.fetch.modes.server.wsproxy

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.tls.tls
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.withContext
import site.pegasis.ta.fetch.modes.server.session.WebSocketSession
import site.pegasis.ta.fetch.modes.server.session.WebsocketMessageType
import site.pegasis.ta.fetch.tools.logError
import site.pegasis.ta.fetch.tools.noThrow
import java.io.Closeable
import java.net.InetSocketAddress
import java.nio.ByteBuffer

fun CharArray.toByteArray(): ByteArray {
    val byteArray = ByteArray(size)
    forEachIndexed { i, char ->
        byteArray[i] = char.toByte()
    }

    return byteArray
}

fun ByteBuffer.toByteArray(size: Int): ByteArray {
    return array().copyOf(size)
}

suspend fun getSSLSocket(host: String, port: Int) = aSocket(ActorSelectorManager(Dispatchers.IO))
        .tcp()
        .connect(InetSocketAddress(host, port))
        .tls(Dispatchers.IO) {
            this.trustManager = TATrustManager()
        }

fun getServerSocket(port: Int) = aSocket(ActorSelectorManager(Dispatchers.IO))
    .tcp()
    .bind(InetSocketAddress("localhost", port))

suspend fun Socket.closeSuspend() = withContext(Dispatchers.IO) {
    noThrow {
        close()
    }
}

suspend fun Closeable.closeSuspend() = withContext(Dispatchers.IO) { close() }

suspend fun ByteReadChannel.readAllLines(): String {
    return try {
        val sb = StringBuffer()
        forEachData { data ->
            val text = String(data, Charsets.UTF_8)
            sb.append(text)
        }
        sb.toString()
    } catch (e: Throwable) {
        logError("error when read all lines, ${e.message}", e)
        ""
    }
}

suspend fun ByteReadChannel.forEachData(action: suspend (data: ByteArray) -> Unit) {
    val buffer = ByteBuffer.allocate(40960)

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

suspend fun ByteWriteChannel.write(byteArray: ByteArray) = writeFully(ByteBuffer.wrap(byteArray))

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
