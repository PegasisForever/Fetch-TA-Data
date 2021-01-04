import io.ktor.client.*
import io.ktor.client.engine.curl.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.system.exitProcess

@Serializable
data class Line(val type: String, val text: String)

@Serializable
data class ControlResponse(val version: Int, @SerialName("exit_code") val exitCode: Int, val lines: List<Line>)

const val CONTROL_API_VERSION = 1

fun main(args: Array<String>) {
    val controlUrl = "http://localhost:5006/"
    val response = runBlocking {
        val httpResponse: HttpResponse = try {
            HttpClient(Curl) {
                expectSuccess = true
            }.request {
                url(controlUrl)
                method = HttpMethod.Post
                body = Json.encodeToString(args)
            }
        } catch (e: Throwable) {
            printlnErr("Error sending request to $controlUrl.")
            printlnErr("You can change control url using -t <url> before all the arguments.")
            printlnErr(e.stackTraceToString())
            exitProcess(1)
        }

        Json.decodeFromString<ControlResponse>(httpResponse.readText())
    }


    if (response.version != CONTROL_API_VERSION) {
        error("Expect control api version $CONTROL_API_VERSION, got ${response.version}")
    }
    response.lines.forEach { (type, text) ->
        when (type) {
            "std" -> println(text)
            "err" -> printlnErr(text)
            else -> error("Unknown line type: $type")
        }
    }

    exitProcess(response.exitCode)
}
