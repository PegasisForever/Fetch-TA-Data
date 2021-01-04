package site.pegasis.ta.fetch.modes.server

import io.fluidsonic.mongo.MongoDatabase
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.modes.server.controller.Controller
import site.pegasis.ta.fetch.modes.server.route.*
import site.pegasis.ta.fetch.modes.server.storage.*
import site.pegasis.ta.fetch.modes.server.timeline.stopAutoUpdateThread
import site.pegasis.ta.fetch.modes.server.timeline.updateAutoUpdateThread
import site.pegasis.ta.fetch.tools.getMongoClient
import site.pegasis.ta.fetch.tools.logInfo
import site.pegasis.ta.fetch.tools.logUnhandled
import site.pegasis.ta.fetch.tools.toUrlEncoded
import java.lang.Thread.setDefaultUncaughtExceptionHandler

// inclusive
const val MIN_API_VERSION = 4
const val LATEST_API_VERSION = 13
const val LATEST_PUBLIC_API_VERSION = 2
const val DB_NAME = "ta"

lateinit var database: MongoDatabase

fun startServer(enablePrivate: Boolean, privatePort: Int, controlPort: Int, publicPort: Int, dbHost: String, dbPort: Int, dbUSer: String, dbPassword: String) {
    val timing = Timing()

    val serverJob = Job()
    var privateServer: ApplicationEngine? = null
    var controlServer: ApplicationEngine? = null
    var publicServer: ApplicationEngine? = null

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

            serverJob.complete()
            logInfo("Server stopped")
        }
    })

    logInfo("Java info: ${System.getProperty("java.runtime.version")} ${System.getProperty("java.vm.name")}")

    logInfo("Connecting to mongodb.....")
    val mongoClient = getMongoClient("mongodb://${dbUSer.toUrlEncoded()}:${dbPassword.toUrlEncoded()}@$dbHost:$dbPort")
    val mongoDB = mongoClient.getDatabase(DB_NAME)
    runBlocking { initMongoDB(mongoDB) }
    timing("connect to mongodb")

    logInfo("Initiating.....")
    runBlocking {
        CourseListDB.init(mongoDB)
        UserUpdateStatusDB.init(mongoDB)
        UserDB.init(mongoDB)
        FeedbackDB.init(mongoDB)
        StaticData.init(mongoDB)
        updateAutoUpdateThread()
        LoadManager.init()
    }
    timing("initiate")

    logInfo("Starting server.....")
    //private server
    if (enablePrivate) {
        privateServer = embeddedServer(CIO, privatePort) {
            routing {
                createRoute(GetmarkTimeLine())
                createRoute(GetCalendar())
                createRoute(GetAnnouncement())
                createRoute(UpdateNoFetch())
                createRoute(GetArchived())
                createRoute(Feedback())
                createRoute(Regi())
                createRoute(Deregi())
            }
        }
        privateServer.start()
        logInfo("Private server started on port $privatePort")

        controlServer = embeddedServer(CIO, controlPort) {
            routing {
                createRoute(Controller())
            }
        }
        controlServer.start()
        logInfo("Private server controller started on port $controlPort")

        timing("start private")
    }

    //public server
    publicServer = embeddedServer(CIO, publicPort) {
        routing {
            createRoute(PublicGetMark(1))
            createRoute(PublicGetMark(2))
        }
    }
    publicServer.start()
    logInfo("Public server started on port $publicPort")
    timing("start public")

    database = mongoDB
    logInfo("Server fully started", timing = timing)

    runBlocking {
        serverJob.join()
    }
}

fun Routing.createRoute(route: BaseRoute) {
    route.createRoute(this)
}
