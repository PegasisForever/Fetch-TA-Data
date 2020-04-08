package site.pegasis.ta.fetch.modes.server.route

import com.sun.net.httpserver.HttpExchange
import site.pegasis.ta.fetch.modes.server.latestApiVersion
import site.pegasis.ta.fetch.modes.server.minApiVersion
import site.pegasis.ta.fetch.tools.gzip
import java.nio.charset.StandardCharsets

fun HttpExchange.toHttpSession() = object : HttpSession {
    override fun getReqString() = String(
        requestBody.readAllBytes(),
        StandardCharsets.UTF_8
    )

    override fun getIP() = if (requestHeaders.containsKey("X-real-ip")) {
        requestHeaders["X-real-ip"]?.get(0)
    } else {
        remoteAddress.address.toString()
    }

    override fun getApiVersion(): Int {
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

    override fun isApiVersionInsufficient(minApi: Int): Boolean {
        return getApiVersion() < Integer.max(minApi, minApiVersion)
    }

    override fun send(status: Int, res: String, isGzip: Boolean) {
        send(
            status, if (res != "" && isGzip) {
            res.gzip()
        } else {
            res.toByteArray()
        }
        )
    }

    override fun send(statusCode: Int, body: ByteArray) {
        sendResponseHeaders(statusCode, body.size.toLong())
        responseBody.write(body)
        responseBody.close()
    }
}

fun HttpExchange.getReqString() = String(
    requestBody.readAllBytes(),
    StandardCharsets.UTF_8
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

fun HttpExchange.returnIfApiVersionInsufficient(minApi: Int = 0): Boolean {
    if (getApiVersion() < Integer.max(minApi, minApiVersion)) {
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

fun HttpExchange.makePublic(): Boolean {
    responseHeaders.add("Access-Control-Allow-Origin", "*")
    if (requestMethod.toUpperCase() == "OPTIONS") {
        responseHeaders.add("Access-Control-Allow-Methods", "GET, OPTIONS")
        responseHeaders.add("Access-Control-Allow-Headers", "Content-Type,Authorization")
        sendResponseHeaders(204, -1)
        return true
    }
    return false
}