package modes.server

import LogLevel
import com.sun.net.httpserver.HttpServer
import log
import logUnhandled
import models.User
import modes.server.route.*
import modes.server.updater.autoUpdateThreadRunning
import modes.server.updater.startAutoUpdateThread
import java.lang.Thread.setDefaultUncaughtExceptionHandler
import java.net.InetSocketAddress


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

    autoUpdateThread = startAutoUpdateThread(40)

    //private server
    val server = HttpServer.create(InetSocketAddress(5004), 0)
    server.createContext("/getmark", getmarkRoute)
    server.createContext("/getmark_timeline", getmarkTimelineRoute)
    server.createContext("/regi", regiRoute)
    server.createContext("/deregi", deregiRoute)
    server.start()

    //public server
    val publicServer = HttpServer.create(InetSocketAddress(5005), 0)
    publicServer.createContext("/getmark", publicGetmarkRoute)
    publicServer.start()

    log(LogLevel.INFO, "Server started")
}
