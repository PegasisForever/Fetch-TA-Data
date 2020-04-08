package site.pegasis.ta.fetch.modes.server.route

interface WebSocketSession {
    suspend fun nextMessage():String
    suspend fun send(message:String)
    suspend fun close()
}