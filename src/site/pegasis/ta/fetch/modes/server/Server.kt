package site.pegasis.ta.fetch.modes.server

import FeedbackDB
import io.fluidsonic.mongo.MongoDatabase
import io.ktor.routing.Routing
import io.ktor.routing.options
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.modes.server.controller.Controller
import site.pegasis.ta.fetch.modes.server.route.*
import site.pegasis.ta.fetch.modes.server.storage.CourseListDB
import site.pegasis.ta.fetch.modes.server.storage.StaticData
import site.pegasis.ta.fetch.modes.server.storage.UserDB
import site.pegasis.ta.fetch.modes.server.storage.UserUpdateStatusDB
import site.pegasis.ta.fetch.modes.server.timeline.stopAutoUpdateThread
import site.pegasis.ta.fetch.modes.server.timeline.updateAutoUpdateThread
import site.pegasis.ta.fetch.tools.getMongoClient
import site.pegasis.ta.fetch.tools.logInfo
import site.pegasis.ta.fetch.tools.logUnhandled
import site.pegasis.ta.fetch.tools.toUrlEncoded
import java.lang.Thread.setDefaultUncaughtExceptionHandler

const val MIN_API_VERSION = 4
const val LATEST_API_VERSION = 12
const val LATEST_PUBLIC_API_VERSION = 2
const val DB_NAME = "ta"

lateinit var database: MongoDatabase

fun startServer(enablePrivate: Boolean, privatePort: Int, controlPort: Int, publicPort: Int, dbHost: String, dbPort: Int, dbUSer: String, dbPassword: String) {
    val timing = Timing()

    var privateServer: NettyApplicationEngine? = null
    var controlServer: NettyApplicationEngine? = null
    var publicServer: NettyApplicationEngine? = null

    setDefaultUncaughtExceptionHandler { thread: Thread?, e: Throwable ->
        logUnhandled(thread, e)
    }
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            val job1 = GlobalScope.launch {
                privateServer?.stop(1_000L, 2_000L)
            }
            val job2 = GlobalScope.launch {
                controlServer?.stop(1_000L, 2_000L)
            }
            val job3 = GlobalScope.launch {
                publicServer?.stop(1_000L, 2_000L)
            }
            val job4 = GlobalScope.launch {
                stopAutoUpdateThread()
            }
            runBlocking {
                job1.join();job2.join();job3.join();job4.join()
            }

            logInfo("Server stopped")
        }
    })

    logInfo("Java info: ${System.getProperty("java.runtime.version")} ${System.getProperty("java.vm.name")}")

    logInfo("Connecting to mongodb.....")
    val mongoClient = getMongoClient("mongodb://${dbUSer.toUrlEncoded()}:${dbPassword.toUrlEncoded()}@$dbHost:$dbPort")
    val mongoDB = mongoClient.getDatabase(DB_NAME)
    timing("connect to mongodb")

    logInfo("Initiating.....")
    runBlocking {
        CourseListDB.init(mongoDB)
        UserUpdateStatusDB.init(mongoDB)
        UserDB.init(mongoDB)
        FeedbackDB.init(mongoDB)
        StaticData.init(mongoDB)
        updateAutoUpdateThread()
    }
    timing("initiate")

    logInfo("Starting server.....")
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

    database = mongoDB
    logInfo("Server fully started", timing = timing)
}

fun Routing.createContext(path: String, route: suspend (HttpSession) -> Unit) {
    post(path) {
        route(this.toHttpSession())
    }
    options(path) {
        route(this.toHttpSession())
    }
}
