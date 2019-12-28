package modes.server

import LogLevel
import com.sun.net.httpserver.HttpServer
import getCoreCount
import log
import logUnhandled
import models.User
import modes.server.route.*
import modes.server.storage.Config
import modes.server.storage.initFiles
import modes.server.timeline.autoUpdateThreadRunning
import modes.server.timeline.startAutoUpdateThread
import java.lang.Thread.setDefaultUncaughtExceptionHandler
import java.net.InetSocketAddress
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

const val minApiVersion = 4
const val latestApiVersion = 9

fun startServer(enablePrivate: Boolean, privatePort: Int, publicPort: Int) {
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

    initFiles()

    User.init()

    if (Config.autoUpdateEnabled) {
        autoUpdateThread = startAutoUpdateThread(Config.autoUpdateIntervalMinute)
    }

    //private server
    if (enablePrivate) {
        HttpServer.create(InetSocketAddress(privatePort), 0).run {
            executor = ThreadPoolExecutor(1, getCoreCount() * 100, 30L, TimeUnit.SECONDS, SynchronousQueue())
            createContext("/getmark_timeline", GetmarkTimeLine.route)
            createContext("/getcalendar", GetCalendar.route)
            createContext("/getannouncement", GetAnnouncement.route)
            createContext("/update_nofetch", UpdateNoFetch.route)
            createContext("/getarchived", GetArchived.route)
            createContext("/feedback", Feedback.route)
            createContext("/regi", Regi.route)
            createContext("/deregi", Deregi.route)
            start()
        }
        log(LogLevel.INFO, "Private server started on port $privatePort")
    }

    //public server
    HttpServer.create(InetSocketAddress(publicPort), 0).run {
        executor = ThreadPoolExecutor(1, getCoreCount() * 100, 30L, TimeUnit.SECONDS, SynchronousQueue())
        createContext("/getmark", PublicGetMark.route)
        start()
    }
    log(LogLevel.INFO, "Public server started on port $publicPort")
}
