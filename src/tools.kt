import com.gargoylesoftware.htmlunit.WebClient
import com.sun.net.httpserver.HttpExchange
import org.apache.commons.io.FileUtils
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

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
            println("${ANSI_YELLOW}Regex not found: /${regex}/ in \"${str}\"${ANSI_RESET}")
        }
    }

    return result
}

fun String.writeToFile(path: String) {
    FileUtils.writeStringToFile(File(path), this, "UTF-8")
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

fun log(msg:String){

}

val jsonParser = JSONParser()
fun HttpExchange.getReqJSONObject()=jsonParser.parse(
    String(
        requestBody.readAllBytes(),
        StandardCharsets.UTF_8
    )
) as JSONObject

fun HttpExchange.getReqJSONArray()=jsonParser.parse(
    String(
        requestBody.readAllBytes(),
        StandardCharsets.UTF_8
    )
) as JSONArray

fun HttpExchange.send(statusCode:Int,body:String){
    sendResponseHeaders(statusCode, body.length.toLong())
    responseBody.write(body.toByteArray())
    responseBody.close()
}