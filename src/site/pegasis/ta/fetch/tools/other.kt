package site.pegasis.ta.fetch.tools

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.simple.parser.JSONParser
import java.io.PrintWriter
import java.io.StringWriter

val jsonParser = JSONParser()

fun getCoreCount() = Runtime.getRuntime().availableProcessors()

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

suspend fun noThrowSuspend(action: suspend () -> Unit) {
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

fun Throwable.isConnectionException() = this is java.net.SocketTimeoutException ||
    this is org.apache.http.conn.ConnectTimeoutException ||
    this is org.apache.http.conn.HttpHostConnectException ||
    (message ?: "").indexOf("SocketTimeoutException") != -1

suspend fun <T> io(block: suspend CoroutineScope.() -> T):T{
    return withContext(Dispatchers.IO){
        block()
    }
}