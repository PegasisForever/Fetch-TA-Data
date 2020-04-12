package site.pegasis.ta.fetch.modes.server.route

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import site.pegasis.ta.fetch.WsNetworkRequester
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.modes.server.timeline.AutoUpdateUserQueue
import site.pegasis.ta.fetch.modes.server.timeline.performUpdate
import site.pegasis.ta.fetch.tools.logInfo
import site.pegasis.ta.fetch.tools.logWarn

object RemoteFetchV10 {
    suspend fun route(session: WebSocketSession) = coroutineScope {
        val timing = Timing()
        val hash = session.hashCode()
        val ipAddress = session.getIP()
        val reqApiVersion = 10

        logInfo("WS connection #$hash <- $ipAddress, api version=$reqApiVersion")

        val forceCloseJob = launch {
            delay(55 * 1000)
            logWarn("WS connection #$hash timeout, force closing", timing = timing)
            session.close()
        }

        timing("init")

        val requester = WsNetworkRequester(session)
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < 40 * 1000) {
            val user = AutoUpdateUserQueue.poll() ?: break
            val updates = performUpdate(user, requester = requester, timing = timing)
            logInfo("WS connection #$hash performed update for user ${user.number}, ${updates.size} updates")
        }
        forceCloseJob.cancelAndJoin()

        logInfo("WS connection #$hash disconnected", timing = timing)
    }
}