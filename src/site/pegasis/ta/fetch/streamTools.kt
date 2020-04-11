package site.pegasis.ta.fetch

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.tls.tls
import io.ktor.util.KtorExperimentalAPI
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.withContext
import site.pegasis.ta.fetch.modes.server.route.WebSocketSession
import site.pegasis.ta.fetch.modes.server.route.WebsocketMessageType
import site.pegasis.ta.fetch.tools.noThrow
import java.io.Closeable
import java.io.FileInputStream
import java.net.InetSocketAddress
import java.net.URL
import java.nio.ByteBuffer
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager


fun CharArray.toByteArray(): ByteArray {
    val byteArray = ByteArray(size)
    forEachIndexed { i, char ->
        byteArray[i] = char.toByte()
    }

    return byteArray
}

class TATrustManager : X509TrustManager {
    override fun checkClientTrusted(certs: Array<out X509Certificate>?, authType: String?) {}

    override fun checkServerTrusted(certs: Array<out X509Certificate>?, authType: String?) {
        certs ?: throw IllegalArgumentException("null or zero-length certificate chain")

        if (certs[0] != taCertificate) {
            try {
                certs[0].verify(taCertificate.publicKey)
            } catch (e: Throwable) {
                throw  CertificateException("Certificate not trusted", e);
            }
        }
        try {
            certs[0].checkValidity()
        } catch (e: Throwable) {
            throw CertificateException("Certificate not trusted. It has expired", e);
        }
    }

    override fun getAcceptedIssuers() = arrayOf(taCertificate)

    companion object {
        val taCertificate = kotlin.run {
            val cf = CertificateFactory.getInstance("X.509")
            val finStream = FileInputStream("data/ta.cer")
            cf.generateCertificate(finStream) as X509Certificate
        }
    }
}

@KtorExperimentalAPI
suspend fun getSSLSocket(host: String, port: Int) =
    aSocket(ActorSelectorManager(Dispatchers.IO))
        .tcp()
        .connect(InetSocketAddress(host, port))
        .tls(Dispatchers.IO) {
            this.trustManager = TATrustManager()
        }

@KtorExperimentalAPI
fun getServerSocket(port: Int) = aSocket(ActorSelectorManager(Dispatchers.IO))
    .tcp()
    .bind(InetSocketAddress("localhost", port))

suspend fun Closeable.closeSuspend() = withContext(Dispatchers.IO) { close() }

suspend fun ByteReadChannel.readAllLines(): String {
    val sb = StringBuffer()
    var line: String?
    while (readUTF8Line().also { line = it } != null) {
        sb.append(line).append("\n")
    }
    return sb.toString()
}

fun StringBuilder.newLine() = append('\n')

suspend fun String.toURL(): URL = withContext(Dispatchers.IO) {
    URL(this@toURL)
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