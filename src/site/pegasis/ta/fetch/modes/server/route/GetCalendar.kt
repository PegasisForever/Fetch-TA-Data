package site.pegasis.ta.fetch.modes.server.route

import site.pegasis.ta.fetch.modes.server.storage.StaticData
import site.pegasis.ta.fetch.tools.logInfo

object GetCalendar {
    suspend fun route(session: HttpSession){
        val ipAddress = session.getIP()
        val hash = session.hashCode()
        logInfo("Request #$hash /getcalendar <-> $ipAddress")
        session.send(200, StaticData.getCalendar())
    }
}
