package site.pegasis.ta.fetch.modes.server.route

interface HttpSession {
    fun getReqString(): String
    fun getIP(): String?
    fun getApiVersion(): Int
    fun isApiVersionInsufficient(minApi: Int = 0): Boolean
    fun send(status: Int, res: String, isGzip: Boolean = true)
    fun send(statusCode: Int, body: ByteArray = ByteArray(0))
}