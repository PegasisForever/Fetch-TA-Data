package site.pegasis.ta.fetch.tools

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

fun getInput(s: String, password: Boolean = false): String {
    print(s)
    return if (password) {
        System.console()?.readPassword()?.joinToString("") ?: readLine()!!
    } else {
        readLine()!!
    }
}

