import com.gargoylesoftware.htmlunit.WebClient
import com.sun.net.httpserver.HttpExchange
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.json.simple.parser.JSONParser
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.collections.ArrayList


fun find(str: String, regex: String, allowBlank: Boolean = false): ArrayList<String> {
    val result = ArrayList<String>()

    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(str)

    while (matcher.find()) {
        result.add(matcher.group(0))
    }

    if (result.size == 0) {
        result.add("")
        if (!allowBlank) {
            log(LogLevel.WARN, "Regex not found: /${regex}/ in \"${str}\"")
        }
    }

    return result
}

fun String.writeToFile(path: String) {
    FileUtils.writeStringToFile(File(path), this, "UTF-8")
}

fun String.appendToFile(path: String) {
    FileUtils.write(File(path), this, "UTF-8", true);
}

object W {
    val webClient = WebClient()

    init {
        webClient.options.isCssEnabled = false
        webClient.options.isJavaScriptEnabled = false
        webClient.options.isRedirectEnabled = true
        webClient.options.isThrowExceptionOnScriptError = false
        webClient.options.isThrowExceptionOnFailingStatusCode = false
        webClient.options.isDownloadImages = false
        webClient.options.isAppletEnabled = false
    }

}

fun getWebClient() = W.webClient

fun newWebClient(): WebClient {
    val webClient = WebClient()

    webClient.options.isCssEnabled = false
    webClient.options.isJavaScriptEnabled = false
    webClient.options.isRedirectEnabled = true
    webClient.options.isThrowExceptionOnScriptError = false
    webClient.options.isThrowExceptionOnFailingStatusCode = false
    webClient.options.isDownloadImages = false
    webClient.options.isAppletEnabled = false

    return webClient
}

val ANSI_RESET = "\u001B[0m"
val ANSI_BLACK = "\u001B[30m"
val ANSI_RED = "\u001B[31m"
val ANSI_GREEN = "\u001B[32m"
val ANSI_YELLOW = "\u001B[33m"
val ANSI_BLUE = "\u001B[34m"
val ANSI_PURPLE = "\u001B[35m"
val ANSI_CYAN = "\u001B[36m"
val ANSI_WHITE = "\u001B[37m"

val jsonParser = JSONParser()
fun HttpExchange.getReqString() = String(
    requestBody.readAllBytes(),
    UTF_8
)

fun HttpExchange.send(statusCode: Int, body: String, isGzip:Boolean=false,apiVersion:Int?=null) {
    if (apiVersion!=null){
        responseHeaders.add("api-version",apiVersion.toString())
    }

    if (isGzip){
        val zippedBody=body.gzip()
        sendResponseHeaders(statusCode, zippedBody.size.toLong())
        responseBody.write(zippedBody)
        responseBody.close()
    }else{
        sendResponseHeaders(statusCode, body.length.toLong())
        responseBody.write(body.toByteArray())
        responseBody.close()
    }

}

fun HttpExchange.send(statusCode: Int, body: ByteArray) {
    sendResponseHeaders(statusCode, body.size.toLong())
    responseBody.write(body)
    responseBody.close()
}

enum class LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR,
    FATAL
}

var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
fun log(level: LogLevel, msg: String, throwable: Throwable? = null) {
    val time = sdf.format(Date())
    var logText = "$time\t|\t${level.name}\t|\t${Thread.currentThread().name}\t|\t${msg.replace("\n", "\\n")}\n"
    if ((level == LogLevel.FATAL || level == LogLevel.ERROR) && throwable != null) {
        logText += ExceptionUtils.getStackTrace(throwable)
    }

    logText.appendToFile("data/server_log.log")

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

fun logUnhandled(thread: Thread?, throwable: Throwable) {
    val time = sdf.format(Date())
    var logText = "$time\t|\t${LogLevel.FATAL.name}\t|\t${thread?.name}\t|\tUnhandled Error\n"
    logText += ExceptionUtils.getStackTrace(throwable)

    logText.appendToFile("data/server_log.log")

    logText = ANSI_RED + logText + ANSI_RESET
    print(logText)
}

fun fileExists(path: String): Boolean {
    val tmpDir = File(path)
    return tmpDir.exists()
}

fun String.fill(str: String): String {
    return replace("%s", str)
}

fun String.gzip():ByteArray{
    val bos = ByteArrayOutputStream()
    GZIPOutputStream(bos).bufferedWriter(UTF_8).use { it.write(this) }
    return bos.toByteArray()
}


fun ByteArray.ungzip(): String {
    return GZIPInputStream(this.inputStream()).bufferedReader(UTF_8).use { it.readText() }
}
