package site.pegasis.ta.fetch.modes.server.route

import kotlinx.coroutines.withTimeout
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.exceptions.ParseRequestException
import site.pegasis.ta.fetch.models.User
import site.pegasis.ta.fetch.tools.jsonParser

object RemoteFetchV10 {
    private class InitMessage(req: String) {
        val user: User
        val isBackground: Boolean

        init {
            try {
                val json = jsonParser.parse(req) as JSONObject
                user = User.fromClient(json["user"] as JSONObject)
                isBackground = json["is_background"] as Boolean
            } catch (e: Exception) {
                throw ParseRequestException()
            }
        }
    }

    suspend fun route(session: WebSocketSession) {
        val initMessage = InitMessage(session.nextMessage())
        withTimeout(if (initMessage.isBackground) 20_000L else 60_000L) {

        }
    }
}