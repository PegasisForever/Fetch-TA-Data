package site.pegasis.ta.fetch.modes.server.route

interface HttpSession {
    suspend fun getReqString(): String
    fun getIP(): String?
    fun getApiVersion(): Int
    fun isApiVersionInsufficient(minApi: Int = 0): Boolean
    suspend fun send(status: Int, res: String, isGzip: Boolean = true)
    suspend fun send(statusCode: Int, body: ByteArray = ByteArray(0))
    suspend fun makePublic():Boolean
}