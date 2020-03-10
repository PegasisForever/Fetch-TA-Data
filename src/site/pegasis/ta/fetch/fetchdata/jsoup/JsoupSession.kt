package site.pegasis.ta.fetch.fetchdata.jsoup

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class JsoupSession {
    val cookies: HashMap<String, String> = hashMapOf()
    var currentPage: Document? = null

    fun connect(url: String, data: Map<String, String>, method: Connection.Method, saveCookies: Boolean): Document {
        val connection = Jsoup.connect(url)
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