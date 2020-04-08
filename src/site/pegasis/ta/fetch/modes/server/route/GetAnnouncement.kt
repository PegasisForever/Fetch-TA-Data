package site.pegasis.ta.fetch.modes.server.route

import site.pegasis.ta.fetch.modes.server.storage.PCache
import site.pegasis.ta.fetch.tools.logInfo

object GetAnnouncement {
    suspend fun route(session: HttpSession){
        val ipAddress = session.getIP()
        val hash = session.hashCode()
        logInfo("Request #$hash /getannouncement <-> $ipAddress")

        session.send(
            200, try {
                PCache.getAnnouncement()
            } catch (e: Throwable) {
                ""
            }
        )
    }
}