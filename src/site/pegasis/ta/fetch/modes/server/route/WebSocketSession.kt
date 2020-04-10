package site.pegasis.ta.fetch.modes.server.route

abstract class WebSocketSession {
    abstract suspend fun nextMessage(): Pair<ByteArray, WebsocketMessageType>
    abstract suspend fun send(message: ByteArray, type: WebsocketMessageType)
    suspend fun sendDisconnect() = send(ByteArray(0), WebsocketMessageType.DISCONNECT)
    abstract suspend fun close()
}