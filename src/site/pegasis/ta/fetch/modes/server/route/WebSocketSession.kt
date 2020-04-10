package site.pegasis.ta.fetch.modes.server.route

interface WebSocketSession {
    suspend fun nextMessage(): Pair<ByteArray, WebsocketMessageType>
    suspend fun send(message: ByteArray, type: WebsocketMessageType)
    suspend fun close()
}