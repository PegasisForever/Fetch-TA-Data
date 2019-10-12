package modes.server

import LogLevel
import com.sun.net.httpserver.HttpServer
import log
import logUnhandled
import models.User
import modes.server.route.deregiRoute
import modes.server.route.getmarkRoute
import modes.server.route.regiRoute
import java.lang.Thread.setDefaultUncaughtExceptionHandler
import java.net.InetSocketAddress


fun startServer() {
    log(LogLevel.INFO, "Starting server")

    setDefaultUncaughtExceptionHandler { thread: Thread?, e: Throwable ->
        logUnhandled(thread, e)
    }
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            log(LogLevel.INFO, "Server stopped")
        }
    })

    User.init()

    val server = HttpServer.create(InetSocketAddress(5004), 0)
    server.createContext("/getmark", getmarkRoute)
    server.createContext("/regi", regiRoute)
    server.createContext("/deregi", deregiRoute)
    server.start()

    log(LogLevel.INFO, "Server started")
}
