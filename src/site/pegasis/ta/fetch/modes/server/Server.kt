package site.pegasis.ta.fetch.modes.server

import com.sun.net.httpserver.HttpServer
import site.pegasis.ta.fetch.getCoreCount
import site.pegasis.ta.fetch.logInfo
import site.pegasis.ta.fetch.logUnhandled
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.models.User
import site.pegasis.ta.fetch.modes.server.controller.Controller
import site.pegasis.ta.fetch.modes.server.route.*
import site.pegasis.ta.fetch.modes.server.storage.Config
import site.pegasis.ta.fetch.modes.server.storage.LastUpdateTime
import site.pegasis.ta.fetch.modes.server.storage.initFiles
import site.pegasis.ta.fetch.modes.server.timeline.stopAutoUpdateThread
import site.pegasis.ta.fetch.modes.server.timeline.updateAutoUpdateThread
import java.lang.Thread.setDefaultUncaughtExceptionHandler
import java.net.InetSocketAddress
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

const val minApiVersion = 4
const val latestApiVersion = 10

fun startServer(enablePrivate: Boolean, privatePort: Int, controlPort: Int, publicPort: Int) {
    val timing = Timing()

    setDefaultUncaughtExceptionHandler { thread: Thread?, e: Throwable ->
        logUnhandled(thread, e)
    }
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            stopAutoUpdateThread()
            logInfo("Server stopped")
        }
    })

    initFiles()
    Config.load()
    LastUpdateTime.load()
    User.load()
    updateAutoUpdateThread()
    timing("load data")

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
        createContext("/getmark", PublicGetMark.route)
        start()
    }
    logInfo("Public server started on port $publicPort")
    timing("start public")

    logInfo("Server fully started", timing = timing)
}
