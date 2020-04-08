package site.pegasis.ta.fetch.fetchdata.jsoup

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import site.pegasis.ta.fetch.fetchdata.NetworkRequester

class JsoupSession(private val requester: NetworkRequester) {
    var currentPage: Document? = null

    suspend fun post(url: String, data: Map<String, String>): Document {
        currentPage = Jsoup.parse(requester.post(url, data))
        return currentPage!!
    }

    suspend fun get(url: String): Document {
        currentPage = Jsoup.parse(requester.get(url))
        return currentPage!!
    }

    suspend fun close() {
        requester.close()
    }
}