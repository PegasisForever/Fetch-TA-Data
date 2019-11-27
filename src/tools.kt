import com.gargoylesoftware.htmlunit.WebClient
import com.sun.net.httpserver.HttpExchange
import modes.server.latestApiVersion
import modes.server.minApiVersion
import org.apache.commons.lang3.exception.ExceptionUtils
import org.json.simple.parser.JSONParser
import java.io.ByteArrayOutputStream
import java.io.File
import java.math.RoundingMode
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Paths
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.collections.ArrayList


fun find(str: String, regex: String): ArrayList<String>? {
    var result: ArrayList<String>? = ArrayList()

    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(str)

    while (matcher.find()) {
        result!!.add(matcher.group(0))
    }

    if (result!!.size == 0) {
        result = null
    }

    return result
}

fun findFirst(str: String, regex: String): String? {
    return find(str, regex)?.get(0)
}

fun String.writeToFile(path: String) {
    File(path).writeText(this)
}

fun String.appendToFile(path: String) {
    File(path).appendText(this)
}

fun readFile(path: String): String {
    return String(Files.readAllBytes(Paths.get(path)))
}

fun readFile(file: File): String {
    return file.readText()
}

val webClient = WebClient().apply {
    options.isCssEnabled = false
    options.isJavaScriptEnabled = false
    options.isRedirectEnabled = true
    options.isThrowExceptionOnScriptError = false
    options.isThrowExceptionOnFailingStatusCode = false
    options.isDownloadImages = false
    options.isAppletEnabled = false
    options.timeout = 10000
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

fun HttpExchange.getIP(): String? {
    return if (requestHeaders.containsKey("X-real-ip")) {
        requestHeaders["X-real-ip"]?.get(0)
    } else {
        remoteAddress.address.toString()
    }
}

fun HttpExchange.send(statusCode: Int, body: String, isGzip: Boolean = true) {
    send(
        statusCode, if (body != "" && isGzip) {
            body.gzip()
        } else {
            body.toByteArray()
        }
    )
}

fun HttpExchange.send(statusCode: Int, body: ByteArray = ByteArray(0)) {
    sendResponseHeaders(statusCode, body.size.toLong())
    responseBody.write(body)
    responseBody.close()
}

fun HttpExchange.returnIfApiVersionInsufficient(): Boolean {
    if (getApiVersion() < minApiVersion) {
        send(426)
        return true
    }
    return false
}

fun HttpExchange.getApiVersion(): Int {
    var apiVersion = 1
    try {
        apiVersion = requestHeaders["api-version"]!![0].toInt()
        if (apiVersion > latestApiVersion) {
            apiVersion = 1
        }
    } catch (e: Exception) {
    }

    return apiVersion
}


enum class LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR,
    FATAL
}

private val logDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
val fileDateFormat = SimpleDateFormat("yyyy-MM-dd")
const val serverBuildNumber = 14
fun log(level: LogLevel, msg: String, throwable: Throwable? = null) {
    val date = Date()
    var logText =
        "${logDateFormat.format(date)}\t|\tBN${serverBuildNumber}\t|\t${level.name}\t|\t${Thread.currentThread().name}\t|\t${msg.replace(
            "\n",
            "\\n"
        )}\n"
    if (throwable != null) {
        logText += ExceptionUtils.getStackTrace(throwable)
    }

    logText.appendToFile("data/log/${fileDateFormat.format(date)}.log")

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
    val time = logDateFormat.format(Date())
    var logText =
        "$time\t|\tBN${serverBuildNumber}\t|\t${LogLevel.FATAL.name}\t|\t${thread?.name}\t|\tUnhandled Error\n"
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

fun String.gzip(): ByteArray {
    val bos = ByteArrayOutputStream()
    GZIPOutputStream(bos).bufferedWriter(UTF_8).use { it.write(this) }
    return bos.toByteArray()
}

fun ByteArray.unGzip(): String {
    return GZIPInputStream(inputStream()).bufferedReader(UTF_8).use { it.readText() }
}

private val torontoZoneID = ZoneId.of("America/Toronto")
fun Long.toZonedDateTime(): ZonedDateTime {
    val localDateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(this),
        torontoZoneID
    )
    return localDateTime.atZone(torontoZoneID)
}

fun ZonedDateTime.toJSONString(): String {
    return this.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}

fun String.toZonedDateTime(): ZonedDateTime {
    return ZonedDateTime.parse(this, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}

operator fun String.times(time: Int): String {
    val builder = StringBuilder()
    repeat(time) {
        builder.append(this)
    }

    return builder.toString()
}

fun Double.toRoundString(digit: Int): String {
    val df = DecimalFormat("#." + "#" * digit)
    df.roundingMode = RoundingMode.CEILING
    return df.format(this)
}
