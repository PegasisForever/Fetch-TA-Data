package site.pegasis.ta.fetch.modes.server.route

import kotlinx.coroutines.withTimeout
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.exceptions.ParseRequestException
import site.pegasis.ta.fetch.fetchdata.NetworkRequester
import site.pegasis.ta.fetch.fetchdata.fetchUserCourseList
import site.pegasis.ta.fetch.models.User
import site.pegasis.ta.fetch.modes.server.serializers.serialize
import site.pegasis.ta.fetch.tools.addAll
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

    private class RemoteRequester(private val session: WebSocketSession) : NetworkRequester {
        override suspend fun post(url: String, data: Map<String, String>): String {
            val json = JSONObject().apply {
                this["method"] = "POST"
                this["url"] = url
                this["data"] = JSONObject().apply { addAll(data) }
            }
            session.send(json.toJSONString())
            return session.nextMessage()
        }

        override suspend fun get(url: String): String {
            val json = JSONObject().apply {
                this["method"] = "GET"
                this["url"] = url
            }
            session.send(json.toJSONString())
            return session.nextMessage()
        }

        override suspend fun close() {
            session.send(JSONObject()
                .apply { this["close"] = true }
                .toJSONString())
        }
    }

    suspend fun route(session: WebSocketSession) {
        val msg = session.nextMessage()
        val initMessage = InitMessage(msg)
        withTimeout(if (initMessage.isBackground) 20_000L else 60_000L) {
            val user = initMessage.user
            val requester = RemoteRequester(session)
            val courseList = fetchUserCourseList(user.number, user.password, requester = requester)

            session.send(courseList.serialize(10).toJSONString())
            session.close()
        }
    }
}