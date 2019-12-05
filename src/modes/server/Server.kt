package modes.server

import LogLevel
import com.sun.net.httpserver.HttpServer
import log
import logUnhandled
import models.User
import modes.server.route.*
import modes.server.timeline.autoUpdateThreadRunning
import modes.server.timeline.startAutoUpdateThread
import java.lang.Thread.setDefaultUncaughtExceptionHandler
import java.net.InetSocketAddress

const val minApiVersion = 4
const val latestApiVersion = 7

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
        createContext("/getmark_timeline", GetmarkTimeLine.route)
        createContext("/getarchived", GetArchived.route)
        createContext("/feedback", Feedback.route)
        createContext("/regi", Regi.route)
        createContext("/deregi", Deregi.route)
        start()
    }

    //public server
    HttpServer.create(InetSocketAddress(5005), 0).run {
        createContext("/getmark", PublicGetMark.route)
        start()
    }

    log(LogLevel.INFO, "Server started")
}
