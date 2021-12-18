package site.pegasis.ta.fetch.modes.server.storage

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import site.pegasis.ta.fetch.tools.logInfo
import site.pegasis.ta.fetch.tools.toBase64
import java.net.InetSocketAddress
import java.net.Proxy as JavaProxy

object ProxyManager {
    data class Proxy(
        val id: String,
        val ip: String,
        val password: String,
    ) {
        fun authText() = "Basic " + "pegasis:$password".toBase64()

        fun toJavaProxy(): JavaProxy {
            return JavaProxy(JavaProxy.Type.HTTP, InetSocketAddress(ip, 81))
        }
    }

    private val client = HttpClient(CIO)
    private var proxies = emptyList<Proxy>()
    private val proxyReportMap = hashMapOf<Proxy, Int>()
    private val bannedProxies = hashSetOf<Proxy>()
    private var job: Job? = null

    private fun HttpRequestBuilder.auth() {
        val token = "pegasis:${Config.proxyManagerPassword}".toBase64()
        header("Authorization", "Basic $token")
    }

    private fun start() {
        if (job != null) return
        job = GlobalScope.launch {
            while (isActive) {
                val res: HttpResponse = client.post("${Config.proxyManagerBaseUrl}/get_proxies") {
                    auth()
                    header(HttpHeaders.ContentType, "application/json")
                    body = """{"expectedMinimum":3}"""
                }
                val resJSON = JSONParser().parse(res.readText()) as JSONArray

                proxies = resJSON.map {
                    val proxyJSON = it as JSONObject
                    Proxy(
                        proxyJSON["id"] as String,
                        (proxyJSON["vm"] as JSONObject)["main_ip"] as String,
                        proxyJSON["proxyPassword"] as String,
                    )
                }
                logInfo("available proxies: ${proxies.filter { !bannedProxies.contains(it) }.size}")

                delay(10000)
            }
        }
    }

    private suspend fun stop() {
        if (job == null) return
        job?.cancelAndJoin()
        proxies = emptyList()
        proxyReportMap.clear()
        bannedProxies.clear()
        job = null
    }

    suspend fun updateConfig() {
        if (Config.useProxy) {
            start()
        } else {
            stop()
        }
    }

    fun getRandomProxy(): Proxy? {
        return proxies.filter { !bannedProxies.contains(it) }.randomOrNull()
    }

    fun reportProxy(proxy: Proxy) {
        logInfo("proxy ${proxy.id} reported")
        proxyReportMap[proxy] = (proxyReportMap[proxy] ?: 0) + 1
        if ((proxyReportMap[proxy] ?: 0) > 10) {
            bannedProxies.add(proxy)
            logInfo("proxy ${proxy.id} banned")
            proxyReportMap[proxy] = 0
            GlobalScope.launch {
                client.post("${Config.proxyManagerBaseUrl}/replace_proxy") {
                    auth()
                    header(HttpHeaders.ContentType, "application/json")
                    body = """{"proxyID":"${proxy.id}"}"""
                }
            }
        }
    }
}