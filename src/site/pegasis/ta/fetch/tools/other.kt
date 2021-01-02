package site.pegasis.ta.fetch.tools

import kotlinx.coroutines.TimeoutCancellationException
import org.json.simple.parser.JSONParser
import site.pegasis.ta.fetch.exceptions.RateLimitedException
import java.io.PrintWriter
import java.io.StringWriter

val jsonParser = JSONParser()

fun Throwable.toStackTrace(): String {
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    printStackTrace(pw)
    return sw.toString()
}

fun noThrow(action: () -> Unit) {
    try {
        action()
    } catch (ignored: Throwable) {

    }
}

fun getInput(s: String, password: Boolean = false): String {
    print(s)
    return if (password) {
        System.console()?.readPassword()?.joinToString("") ?: readLine()!!
    } else {
        readLine()!!
    }
}

//null or equal
infix fun Any?.noe(b: Any?): Boolean {
    return this == null || b == null || this == b
}

//not null equal
infix fun Any?.nne(b: Any?): Boolean {
    return this != null && b != null && this == b
}

fun Throwable.isConnectionException() = this is TimeoutCancellationException ||
    this is java.net.SocketTimeoutException ||
    this is java.io.InterruptedIOException ||
    this is java.net.ConnectException ||
    this is io.ktor.client.features.HttpRequestTimeoutException ||
    this is org.apache.http.conn.ConnectTimeoutException ||
    this is org.apache.http.conn.HttpHostConnectException ||
    this is RateLimitedException ||
    (message?.toLowerCase() ?: "").indexOf("timeout") != -1
