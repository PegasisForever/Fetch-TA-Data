package site.pegasis.ta.fetch.fetchdata.jsoup

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import site.pegasis.ta.fetch.exceptions.RateLimitedException
import site.pegasis.ta.fetch.modes.server.storage.Config
import site.pegasis.ta.fetch.tools.noThrow


class JsoupSession(forceUseProxy: Boolean) {
    var currentPage: Document? = null
    private val cookies = hashMapOf<String, String>()
    private val proxy = Config.getRandomProxy(forceUseProxy)
    private val client = getClient(proxy)

    companion object {
        private var clientsCache = hashMapOf<Config.Proxy, HttpClient>()

        private fun getClient(proxy: Config.Proxy): HttpClient {
            return if (proxy in clientsCache) {
                clientsCache[proxy]!!
            } else {
                val client = HttpClient(CIO) {
                    engine {
                        maxConnectionsCount = 10240
                        requestTimeout = Config.fetchTimeoutSecond * 1000
                        endpoint {
                            connectTimeout = Config.fetchTimeoutSecond * 1000
                            socketTimeout = Config.fetchTimeoutSecond * 1000
                        }
                        this.proxy = proxy.toJavaProxy()
                    }
                }
                clientsCache[proxy] = client
                client
            }
        }
    }

    suspend fun connect(url: String, data: Map<String, String>, method: HttpMethod): Document {
        val response = try {
            client.request {
                this.url(url)
                this.method = method
                if (proxy is Config.HttpProxy && proxy.hasAuth()) {
                    header("Proxy-Authorization", proxy.authText())
                }
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
        } catch (e: RedirectResponseException) {
            e.response
        } catch (e: Throwable) {
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
            HttpStatusCode.Forbidden -> throw RateLimitedException()
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
}
