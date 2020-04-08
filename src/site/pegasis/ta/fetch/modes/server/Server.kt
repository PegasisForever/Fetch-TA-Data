package site.pegasis.ta.fetch.modes.server

import com.sun.net.httpserver.HttpServer
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import site.pegasis.ta.fetch.fetchdata.chromepool.ChromePool
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
import site.pegasis.ta.fetch.tools.getCoreCount
import site.pegasis.ta.fetch.tools.logInfo
import site.pegasis.ta.fetch.tools.logUnhandled
import java.lang.Thread.setDefaultUncaughtExceptionHandler
import java.net.InetSocketAddress
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

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
            ChromePool.close()
            logInfo("Server stopped")
        }
    })

    LastUserUpdateTime.load()
    LastUpdateDoneTime.load()
    LastCleanDoneTime.load()
    User.load()
    CalendarData.load()
    ChromePool.init()
    updateAutoUpdateThread()
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

        HttpServer.create(InetSocketAddress(controlPort), 0).run {
            createContext("/", Controller.route)
            start()
        }
        logInfo("Private server controller started on port $controlPort")

        timing("start private")
    }

    //public server
    HttpServer.create(InetSocketAddress(publicPort), 0).run {
        executor = ThreadPoolExecutor(1, getCoreCount() * 100, 30L, TimeUnit.SECONDS, SynchronousQueue())
        createContext("/getmark", PublicGetMark.route(1))
        createContext("/getmark_v2", PublicGetMark.route(2))
        start()
    }
    logInfo("Public server started on port $publicPort")
    timing("start public")

    logInfo("Server fully started", timing = timing)
}

fun Routing.createContext(path: String, route: suspend (HttpSession) -> Unit) {
    post(path){
        route(this.toHttpSession())
    }
}

fun HttpServer.createContext(path: String, route: (HttpSession) -> Unit) {
    createContext(path) {
        route(it.toHttpSession())
    }
}