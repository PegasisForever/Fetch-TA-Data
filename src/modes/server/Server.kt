package modes.server

import LogLevel
import com.sun.net.httpserver.HttpServer
import log
import logUnhandled
import models.User
import modes.server.route.deregiRoute
import modes.server.route.getmarkRoute
import modes.server.route.regiRoute
import modes.server.updater.autoUpdateThreadRunning
import modes.server.updater.startAutoUpdateThread
import java.lang.Thread.setDefaultUncaughtExceptionHandler
import java.net.InetSocketAddress


fun startServer() {
    var autoUpdateThread:Thread?=null

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

    val server = HttpServer.create(InetSocketAddress(5004), 0)
    server.createContext("/getmark", getmarkRoute)
    server.createContext("/regi", regiRoute)
    server.createContext("/deregi", deregiRoute)
    server.start()

    log(LogLevel.INFO, "Server started")
}
