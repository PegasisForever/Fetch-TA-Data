package site.pegasis.ta.fetch.modes.server.session

abstract class WebSocketSession {
    abstract suspend fun nextMessage(): Pair<ByteArray, WebsocketMessageType>
    abstract suspend fun send(message: ByteArray, type: WebsocketMessageType)
    abstract fun getIP(): String?
    suspend fun sendDisconnect() = send(ByteArray(0), WebsocketMessageType.DISCONNECT)
    abstract suspend fun close()
}