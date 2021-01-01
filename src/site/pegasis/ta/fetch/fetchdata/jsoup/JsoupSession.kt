package site.pegasis.ta.fetch.fetchdata.jsoup

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withTimeout
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import site.pegasis.ta.fetch.exceptions.RateLimitedException
import site.pegasis.ta.fetch.modes.server.storage.Config
import site.pegasis.ta.fetch.tools.noThrow

class JsoupSession(forceUseProxy: Boolean) {
    var currentPage: Document? = null
    val cookies = hashMapOf<String, String>()

    val client = HttpClient(CIO) {
        install(HttpTimeout) {
            connectTimeoutMillis = Config.fetchTimeoutSecond * 1000
            requestTimeoutMillis = Config.fetchTimeoutSecond * 1000
            socketTimeoutMillis = Config.fetchTimeoutSecond * 1000
        }
        engine {
            if (forceUseProxy || (Config.useProxy && !Config.useLocalIP)) {
                // only use remote proxy
                val proxy = Config.getRandomRemoteProxy()!!
                this.proxy = ProxyBuilder.socks(proxy.host, proxy.port)
            } else if (Config.useProxy && Config.useLocalIP) {
                // use remote proxy and local ip
                val proxy = Config.getRandomProxy()
                if (proxy is Config.RemoteProxy) {
                    this.proxy = ProxyBuilder.socks(proxy.host, proxy.port)
                }
            }
        }
    }

    suspend fun connect(url: String, data: Map<String, String>, method: HttpMethod): Document {
        val response = try {
            client.request<HttpResponse> {
                this.url(url)
                this.method = method
                header("Accept-Encoding", "gzip,deflate,sdch")
                header("Connection", "keep-alive")
                header("Cookie",
                    cookies
                        .map { (key, value) -> "$key=$value" }
                        .joinToString("; ")
                )
                userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36")

                if (data.isNotEmpty()) {
                    body = FormDataContent(Parameters.build {
                        data.forEach { (key, value) ->
                            append(key, value)
                        }
                    })
                }
            }
        } catch (e: Throwable) {
            close()
            throw e
        }

        response.headers.getAll("set-cookie")?.forEach { cookie ->
            noThrow {
                val firstArg = cookie.split(";").first()
                val (key, value) = firstArg.split("=")
                cookies[key] = value
            }
        }
        return when (response.status) {
            HttpStatusCode.Found -> get(response.headers["Location"]!!)
            HttpStatusCode.Unauthorized -> throw RateLimitedException()
            else -> {
                currentPage = Jsoup.parse(response.readText())
                currentPage!!
            }
        }
    }

    suspend fun post(url: String, data: Map<String, String>): Document {
        return connect(url, data, HttpMethod.Post)
    }

    suspend fun get(url: String): Document {
        return connect(url, hashMapOf(), HttpMethod.Get)
    }

    suspend fun close() {
        try {
            withTimeout(3000) {
                client.close()
            }
        } catch (timeout: TimeoutCancellationException) {
            client.cancel()
        }
    }
}
