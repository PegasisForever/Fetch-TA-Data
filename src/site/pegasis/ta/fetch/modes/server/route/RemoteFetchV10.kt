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

//    private class RemoteRequester(private val session: WebSocketSession) : NetworkRequester {
//        override suspend fun post(url: String, data: Map<String, String>): String {
//            val json = JSONObject().apply {
//                this["method"] = "POST"
//                this["url"] = url
//                this["data"] = JSONObject().apply { addAll(data) }
//            }
//            session.send(json.toJSONString())
//            return session.nextMessage()
//        }
//
//        override suspend fun get(url: String): String {
//            val json = JSONObject().apply {
//                this["method"] = "GET"
//                this["url"] = url
//            }
//            session.send(json.toJSONString())
//            return session.nextMessage()
//        }
//
//        override suspend fun close() {
//            session.send(JSONObject()
//                .apply { this["close"] = true }
//                .toJSONString())
//        }
//    }

    suspend fun route(session: WebSocketSession) {
        withTimeout(60_000L) {
//            val proxyServer = launch { WSProxyServer(session, 5002).run() }
//            delay(100)
//
//            val courseList = fetchUserCourseList("349891234","43z955n9", requester = requester)
//
//            session.send(courseList.serialize(10).toJSONString().toByteArray())
//            proxyServer.cancelAndJoin()
//            session.close()
        }
    }
}