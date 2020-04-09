package site.pegasis.ta.fetch.modes.server.route

interface WebSocketSession {
    suspend fun nextMessage():ByteArray
    suspend fun send(message:ByteArray)
    fun send2(message:ByteArray)
    suspend fun close()
}