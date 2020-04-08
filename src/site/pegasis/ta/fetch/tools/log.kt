@file:Suppress("DeferredResultUnused")

package site.pegasis.ta.fetch.tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.apache.commons.lang3.exception.ExceptionUtils
import site.pegasis.ta.fetch.models.Timing
import java.text.SimpleDateFormat
import java.util.*

val ANSI_RESET = "\u001B[0m"
val ANSI_BLACK = "\u001B[30m"
val ANSI_RED = "\u001B[31m"
val ANSI_GREEN = "\u001B[32m"
val ANSI_YELLOW = "\u001B[33m"
val ANSI_BLUE = "\u001B[34m"
val ANSI_PURPLE = "\u001B[35m"
val ANSI_CYAN = "\u001B[36m"
val ANSI_WHITE = "\u001B[37m"

enum class LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR,
    FATAL
}

private val logDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
val fileDateFormat = SimpleDateFormat("yyyy-MM-dd")
const val serverBuildNumber = 50
var isQuiet = false
fun log(level: LogLevel, msg: String, throwable: Throwable? = null, timing: Timing? = null) {
    if (isQuiet) {
        return
    }
    val date = Date()
    var logText =
        "${logDateFormat.format(date)}\t|" +
            "\tBN$serverBuildNumber\t|" +
            "\t${level.name}\t|" +
            "\t${Thread.currentThread().name}\t|" +
            "\t${msg.replace("\n", "\\n")}\t"
    if (timing != null) {
        logText += "|\t${timing.getResult()}"
    }
    logText += "\n"
    if (throwable != null) {
        logText += ExceptionUtils.getStackTrace(throwable)
    }

    GlobalScope.async(Dispatchers.IO){
        logText.appendToFile("data/log/${fileDateFormat.format(date)}.log")
    }

    val color = when (level) {
        LogLevel.DEBUG -> ANSI_BLACK
        LogLevel.INFO -> ANSI_BLUE
        LogLevel.WARN -> ANSI_YELLOW
        LogLevel.ERROR -> ANSI_RED
        LogLevel.FATAL -> ANSI_RED
    }
    logText = color + logText + ANSI_RESET
    print(logText)
}

fun logInfo(msg: String, throwable: Throwable? = null, timing: Timing? = null) =
    log(LogLevel.INFO, msg, throwable, timing)

fun logWarn(msg: String, throwable: Throwable? = null, timing: Timing? = null) =
    log(LogLevel.WARN, msg, throwable, timing)

fun logError(msg: String, throwable: Throwable? = null, timing: Timing? = null) =
    log(LogLevel.ERROR, msg, throwable, timing)


fun logUnhandled(thread: Thread?, throwable: Throwable) {
    if (isQuiet) {
        return
    }
    val date = Date()
    var logText =
        "${logDateFormat.format(date)}\t|\tBN$serverBuildNumber\t|\t${LogLevel.FATAL.name}\t|\t${thread?.name}\t|\tUnhandled Error\n"
    logText += ExceptionUtils.getStackTrace(throwable)

    GlobalScope.async(Dispatchers.IO){
        logText.appendToFile("data/log/${fileDateFormat.format(date)}.log")
    }

    logText = ANSI_RED + logText + ANSI_RESET
    print(logText)
}