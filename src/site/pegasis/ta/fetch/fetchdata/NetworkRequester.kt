package site.pegasis.ta.fetch.fetchdata

interface NetworkRequester {
    suspend fun post(url: String, data: Map<String, String>):String
    suspend fun get(url: String): String
    suspend fun close()
}