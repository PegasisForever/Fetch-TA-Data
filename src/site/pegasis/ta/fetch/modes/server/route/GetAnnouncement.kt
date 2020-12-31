package site.pegasis.ta.fetch.modes.server.route

import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.modes.server.storage.StaticData

class GetAnnouncement : BaseRoute() {
    override fun path() = "/getannouncement"

    override suspend fun route(session: HttpSession, timing: Timing): Response {
        val announcement = timing("get announcement") {
            StaticData.getAnnouncement()
        }

        return Response(200, announcement)
    }
}
