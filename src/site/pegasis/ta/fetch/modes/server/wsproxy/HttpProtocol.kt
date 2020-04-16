package site.pegasis.ta.fetch.modes.server.wsproxy

import site.pegasis.ta.fetch.tools.newLine

object HttpProtocol {
    class Cookies : HashMap<String, String>()

    class Headers : ArrayList<Pair<String, String>>() {
        operator fun get(key: String): String {
            return find { it.first == key }?.second ?: ""
        }
    }

    data class HttpResponse(val statusCode: Int, val headers: Headers, val body: String) {
        fun getCookies(): Cookies {
            val cookies = Cookies()
            headers.filter { it.first == "Set-Cookie" }
                .map { it.second.substring(0 until it.second.indexOf(";")) }
                .map {
                    val parts = it.split("=")
                    parts[0] to parts[1]
                }
                .filter { it.second != "deleted" }
                .forEach {
                    cookies += it
                }
            return cookies
        }
    }

    private fun createHttpRawRequest(method: String, host: String, path: String, headers: Headers = Headers(), payload: String = ""): String {
        return with(StringBuilder()) {
            append(method)
            append(' ')
            append(path)
            append(" HTTP/1.1")
            newLine()

            append("Host: ")
            append(host)
            newLine()

            headers.forEach { (key, value) ->
                append("$key: $value")
                newLine()
            }

            newLine()
            append(payload)

            toString()
        }
    }

    fun createHttpGetRawRequest(host: String, path: String, cookies: Cookies = Cookies()): String {
        return if (cookies.isNotEmpty()) {
            val cookieHeaderString = cookies.map { "${it.key}=${it.value}" }.joinToString("; ")
            val headers = Headers()
            headers.add("Cookie" to cookieHeaderString)
            createHttpRawRequest("GET", host, path, headers)
        } else {
            createHttpRawRequest("GET", host, path)
        }
    }

    fun createHttpPostRawRequest(host: String, path: String, data: Map<String, Any> = emptyMap(), cookies: Cookies = Cookies()): String {
        val payload = data.map { "${it.key}=${it.value}" }.joinToString("&")

        val newHeaders = Headers()
        newHeaders.add("Content-Type" to "application/x-www-form-urlencoded")
        newHeaders.add("Content-Length" to payload.length.toString())
        if (cookies.isNotEmpty()) {
            val cookieHeaderString = cookies.map { "${it.key}=${it.value}" }.joinToString("; ")
            newHeaders.add("Cookie" to cookieHeaderString)
        }


        return createHttpRawRequest("POST", host, path, newHeaders, payload)
    }

    fun parseHttpRawResponse(rawResponse: String): HttpResponse {
        var statusCode = -1
        val headers = Headers()
        val bodySb = StringBuilder()

        var readingBody = false
        rawResponse
            .split("\n")
            .forEach { line ->
                if (statusCode == -1) {
                    statusCode = line.split(" ")[1].toInt()
                } else if (!readingBody) {
                    val colonIndex = line.indexOf(":")
                    if (colonIndex >= 0) {
                        headers.add(line.substring(0 until colonIndex) to line.substring(colonIndex + 2))
                    } else {
                        readingBody = true
                    }
                } else {
                    bodySb.append(line).newLine()
                }
            }

        return HttpResponse(statusCode, headers, bodySb.toString())
    }
}