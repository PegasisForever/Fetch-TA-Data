package site.pegasis.ta.fetch.modes.ctl

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.tools.parseJSON

private const val timeoutSeconds = 100L
const val CONTROL_API_VERSION = 1

fun serverControl(controlUrl: String, args: Array<String>) = runBlocking {
    val client = HttpClient(CIO) {
        engine {
            requestTimeout = timeoutSeconds * 1000
            endpoint {
                connectTimeout = timeoutSeconds * 1000
                socketTimeout = timeoutSeconds * 1000
            }
        }
    }

    val response: HttpResponse = client.request {
        url(controlUrl)
        method = HttpMethod.Post
        body = JSONArray()
            .apply {
                if (args.isEmpty()) {
                    add("-h")
                } else {
                    addAll(args)
                }
            }
            .toJSONString()
    }

    val responseJSON = response.readText().parseJSON<JSONObject>()
    if ((responseJSON["version"] as Long).toInt() != CONTROL_API_VERSION) {
        error("Expect control api version $CONTROL_API_VERSION, got ${responseJSON["version"]}")
    }

    val lines = responseJSON["lines"] as JSONArray
    lines
        .filterIsInstance<JSONObject>()
        .forEach { line ->
            if (line["type"] == "err") {
                System.err.println(line["text"])
            } else {
                println(line["text"])
            }
        }

    (responseJSON["exit_code"] as Long).toInt()
}
