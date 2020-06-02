package site.pegasis.ta.fetch.fetchdata.jsoup

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.userAgent
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withTimeout
import org.apache.http.HttpHost
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import site.pegasis.ta.fetch.modes.server.storage.Config

class JsoupSession(useProxy:Boolean) {
    var currentPage: Document? = null
    val client = HttpClient(Apache) {
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        expectSuccess = false
        engine {
            followRedirects = true
            socketTimeout = Config.fetchTimeoutSecond * 1000
            connectTimeout = Config.fetchTimeoutSecond * 1000
            connectionRequestTimeout = Config.fetchTimeoutSecond * 1000
            if (Config.proxy.isNotBlank() && useProxy) {
                customizeClient {
                    setProxy(HttpHost(Config.proxy, Config.proxyPort))
                }
            }
        }
    }

    suspend fun connect(url: String, data: Map<String, String>, method: HttpMethod): Document {
        val response = client.request<HttpResponse> {
            url(url)
            this.method = method
            header("Accept-Encoding", "gzip,deflate,sdch")
            header("Connection", "keep-alive")
            userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36")

            if (data.isNotEmpty()) {
                body = FormDataContent(Parameters.build {
                    data.forEach { (key, value) ->
                        append(key, value)
                    }
                })
            }
        }

        return if (response.status == HttpStatusCode.Found) {
            get(response.headers["Location"]!!)
        }else{
            currentPage = Jsoup.parse(response.readText())
            currentPage!!
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
