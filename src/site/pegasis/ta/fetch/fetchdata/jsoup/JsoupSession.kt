package site.pegasis.ta.fetch.fetchdata.jsoup

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import site.pegasis.ta.fetch.modes.server.storage.Config

class JsoupSession {
    val cookies: HashMap<String, String> = hashMapOf()
    var currentPage: Document? = null

    fun connect(url: String, data: Map<String, String>, method: Connection.Method, saveCookies: Boolean): Document {
        val connection = Jsoup.connect(url)
            .timeout(Config.fetchTimeoutSecond * 1000)
            .header("Accept-Encoding", "gzip,deflate,sdch")
            .header("Connection", "keep-alive")
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36")

        if (Config.proxy.isNotBlank()) connection.proxy(Config.proxy, Config.proxyPort)
        data.forEach { (name, value) ->
            connection.data(name, value)
        }
        cookies.forEach { (name, value) ->
            connection.cookie(name, value)
        }
        connection.method(method)
        val res = connection.execute()
        if (saveCookies) res.cookies().forEach { (name, value) ->
            if (name != null && value != null && value != "deleted") {
                cookies[name] = value
            }
        }
        currentPage = res.parse()
        return currentPage!!
    }

    fun post(url: String, data: Map<String, String>): Document {
        return connect(url, data, Connection.Method.POST, true)
    }

    fun get(url: String): Document {
        return connect(url, hashMapOf(), Connection.Method.GET, false)
    }
}