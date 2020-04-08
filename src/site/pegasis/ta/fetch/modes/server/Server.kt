package site.pegasis.ta.fetch.modes.server

import io.ktor.routing.Routing
import io.ktor.routing.options
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import kotlinx.coroutines.runBlocking
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.models.User
import site.pegasis.ta.fetch.modes.server.controller.Controller
import site.pegasis.ta.fetch.modes.server.route.*
import site.pegasis.ta.fetch.modes.server.storage.CalendarData
import site.pegasis.ta.fetch.modes.server.storage.LastCleanDoneTime
import site.pegasis.ta.fetch.modes.server.storage.LastUpdateDoneTime
import site.pegasis.ta.fetch.modes.server.storage.LastUserUpdateTime
import site.pegasis.ta.fetch.modes.server.timeline.stopAutoUpdateThread
import site.pegasis.ta.fetch.modes.server.timeline.updateAutoUpdateThread
import site.pegasis.ta.fetch.tools.logInfo
import site.pegasis.ta.fetch.tools.logUnhandled
import java.lang.Thread.setDefaultUncaughtExceptionHandler

const val minApiVersion = 4
const val latestApiVersion = 10
const val latestPublicApiVersion = 2

fun startServer(enablePrivate: Boolean, privatePort: Int, controlPort: Int, publicPort: Int) {
    val timing = Timing()
    logInfo("Starting server")

    var privateServer: NettyApplicationEngine? = null
    var controlServer: NettyApplicationEngine? = null
    var publicServer: NettyApplicationEngine? = null

    setDefaultUncaughtExceptionHandler { thread: Thread?, e: Throwable ->
        logUnhandled(thread, e)
    }
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            privateServer?.stop(1_000L, 2_000L)
            controlServer?.stop(1_000L, 2_000L)
            publicServer?.stop(1_000L, 2_000L)
            stopAutoUpdateThread()
            logInfo("Server stopped")
        }
    })

    runBlocking {
        LastUserUpdateTime.load()
        LastUpdateDoneTime.load()
        LastCleanDoneTime.load()
        User.load()
        CalendarData.load()
        updateAutoUpdateThread()
    }
    timing("load data")

    //private server
    if (enablePrivate) {
        privateServer = embeddedServer(Netty, privatePort) {
            routing {
                createContext("/getmark_timeline", GetmarkTimeLine::route)
                createContext("/getcalendar", GetCalendar::route)
                createContext("/getannouncement", GetAnnouncement::route)
                createContext("/update_nofetch", UpdateNoFetch::route)
                createContext("/getarchived", GetArchived::route)
                createContext("/feedback", Feedback::route)
                createContext("/regi", Regi::route)
                createContext("/deregi", Deregi::route)
            }
        }
        privateServer.start()
        logInfo("Private server started on port $privatePort")

        controlServer = embeddedServer(Netty, controlPort) {
            routing {
                createContext("/", Controller::route)
            }
        }
        controlServer.start()
        logInfo("Private server controller started on port $controlPort")

        timing("start private")
    }

    //public server
    publicServer = embeddedServer(Netty, publicPort) {
        routing {
            createContext("/getmark", PublicGetMark::routeV1)
            createContext("/getmark_v2", PublicGetMark::routeV2)
        }
    }
    publicServer.start()
    logInfo("Public server started on port $publicPort")
    timing("start public")

    logInfo("Server fully started", timing = timing)
}

fun Routing.createContext(path: String, route: suspend (HttpSession) -> Unit) {
    post(path) {
        route(this.toHttpSession())
    }
    options(path){
        route(this.toHttpSession())
    }
}