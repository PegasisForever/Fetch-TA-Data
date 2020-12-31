package site.pegasis.ta.fetch.modes.server.route

import io.ktor.routing.*
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.modes.server.LoadManager
import site.pegasis.ta.fetch.modes.server.MIN_API_VERSION
import site.pegasis.ta.fetch.tools.logInfo
import site.pegasis.ta.fetch.tools.logWarn
import site.pegasis.ta.fetch.tools.removeBlank

data class Response(val status: Int, val res: String = "", val isGzip: Boolean = true)

abstract class BaseRoute {
    open fun isPublic() = false
    open fun isController() = false
    open fun minApiVersion() = MIN_API_VERSION
    abstract fun path(): String

    abstract suspend fun route(session: HttpSession, timing: Timing): Response

    private fun getLogHeader() = if (isController()) "Control" else "Request"

    fun createRoute(routing: Routing) {
        routing.post(path()) {
            val timing = Timing()
            val session = this.toHttpSession()
            val hash = session.hashCode()

            logInfo("${getLogHeader()} #$hash ${path()} <- ${session.getIP()}, api version=${session.getApiVersion()}, data=${session.getReqString().removeBlank()}")

            if (LoadManager.isOverLoad() && !isController()) {
                session.send(503)
                logWarn("Request #$hash -> Server overload, ignoring request")
                return@post
            }
            if (session.isApiVersionInsufficient(minApiVersion())) {
                session.send(426)
                logInfo("Request #$hash -> Api version insufficient")
                return@post
            }

            val response = route(session, timing)
            session.send(response)
            timing("send")
            logInfo("${getLogHeader()} #$hash -> status=${response.status}", timing = timing)
        }

        if (isPublic()) {
            routing.options(path()) {
                this.toHttpSession().makePublic()
            }
        }
    }
}
