package site.pegasis.ta.fetch.modes.server.route

abstract class HttpSession {
    abstract suspend fun getReqString(): String
    abstract fun getIP(): String?
    abstract fun getApiVersion(): Int
    abstract fun isApiVersionInsufficient(minApi: Int = 0): Boolean
    abstract suspend fun send(status: Int, res: String, isGzip: Boolean = true)
    abstract suspend fun send(status: Int, body: ByteArray = ByteArray(0))
    suspend fun send(response: Response) = send(response.status, response.res, response.isGzip)
    abstract suspend fun makePublic(): Boolean
}
