package site.pegasis.ta.fetch.modes.server.route

import io.ktor.routing.*
import site.pegasis.ta.fetch.modes.server.LoadManager

fun Routing.createRoute(path: String, route: suspend (HttpSession) -> Unit) {
    post(path) {
        val session=this.toHttpSession()
        if (LoadManager.isOverLoad()){
            session.send(503)
            return@post
        }

        route(session)
    }
    options(path) {
        route(this.toHttpSession())
    }
}

fun Routing.createRoute(route: BaseRoute) {
    route.createRoute(this)
}
