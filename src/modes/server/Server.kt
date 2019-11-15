package modes.server

import LogLevel
import com.sun.net.httpserver.HttpServer
import log
import logUnhandled
import models.User
import modes.server.route.deregiRoute
import modes.server.route.getmarkTimelineRoute
import modes.server.route.publicGetmarkRoute
import modes.server.route.regiRoute
import modes.server.updater.autoUpdateThreadRunning
import modes.server.updater.startAutoUpdateThread
import java.lang.Thread.setDefaultUncaughtExceptionHandler
import java.net.InetSocketAddress

val minApiVersion = 4
val latestApiVersion = 4

fun startServer() {
    var autoUpdateThread: Thread? = null

    log(LogLevel.INFO, "Starting server")

    setDefaultUncaughtExceptionHandler { thread: Thread?, e: Throwable ->
        logUnhandled(thread, e)
    }
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            autoUpdateThreadRunning.set(false)
            autoUpdateThread?.interrupt()
            log(LogLevel.INFO, "Server stopped")
        }
    })

    User.init()

    if (Config.autoUpdateEnabled) {
        autoUpdateThread = startAutoUpdateThread(Config.autoUpdateIntervalMinute)
    }

    //private server
    HttpServer.create(InetSocketAddress(5004), 0).run {
        createContext("/getmark_timeline", getmarkTimelineRoute)
        createContext("/regi", regiRoute)
        createContext("/deregi", deregiRoute)
        start()
    }

    //public server
    HttpServer.create(InetSocketAddress(5005), 0).run {
        createContext("/getmark", publicGetmarkRoute)
        start()
    }

    log(LogLevel.INFO, "Server started")
}
