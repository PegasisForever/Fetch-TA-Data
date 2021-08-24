package site.pegasis.ta.fetch.modes.server.route

import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.modes.server.storage.StaticData

class GetCalendar : BaseRoute() {
    override fun path() = "/getcalendar"

    override suspend fun route(session: HttpSession, timing: Timing): Response {
        val calendar = timing("get calendar") {
            if (session.getApiVersion() >= 13) {
                StaticData.getCalendarV2()
            } else {
                StaticData.getCalendarV1()
            }
        }
        return Response(200, calendar, true)
    }
}
