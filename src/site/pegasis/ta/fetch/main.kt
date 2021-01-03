package site.pegasis.ta.fetch

import picocli.CommandLine
import picocli.CommandLine.*
import site.pegasis.ta.fetch.modes.ctl.serverControl
import site.pegasis.ta.fetch.modes.getMark
import site.pegasis.ta.fetch.modes.server.LATEST_API_VERSION
import site.pegasis.ta.fetch.modes.server.MIN_API_VERSION
import site.pegasis.ta.fetch.modes.server.startServer
import site.pegasis.ta.fetch.modes.server.storage.Config
import site.pegasis.ta.fetch.modes.server.storage.initFiles
import site.pegasis.ta.fetch.tools.getInput
import site.pegasis.ta.fetch.tools.serverBuildNumber
import java.util.concurrent.Callable
import kotlin.system.exitProcess

suspend fun main(args: Array<String>) {
    initFiles()
    Config.load()
    CommandLine(FetchTa())
        .addSubcommand(GetMark())
        .addSubcommand(Server())
        .addSubcommand(ServerControl())
        .execute(*args)
}

@Command(
    name = "fetchta",
    mixinStandardHelpOptions = true,
    version = ["BN$serverBuildNumber"]
)
class FetchTa : Callable<Unit> {
    override fun call() {}
}

class ApiVersionConverter : ITypeConverter<Int> {
    override fun convert(p0: String): Int {
        val apiVersion = p0.toInt()
        if (apiVersion < MIN_API_VERSION || apiVersion > LATEST_API_VERSION) {
            error("Api version must between $MIN_API_VERSION and $LATEST_API_VERSION.")
        }
        return apiVersion
    }

}

@Command(
    description = ["Fetch a student's mark from YRDSB Teach Assist."],
    name = "getmark",
    mixinStandardHelpOptions = true,
    version = ["BN$serverBuildNumber"]
)
class GetMark : Callable<Unit> {
    @Parameters(index = "0", defaultValue = "")
    private var studentNumber = ""

    @Parameters(index = "1", defaultValue = "")
    private var password = ""

    @Option(
        names = ["--api", "-a"],
        description = ["API Level of the output JSON, default to $LATEST_API_VERSION."],
        converter = [ApiVersionConverter::class]
    )
    private var apiLevel = LATEST_API_VERSION

    @CommandLine.Option(
        names = ["--quiet", "-q"],
        description = ["Don't output logs."]
    )
    private var quiet = false

    @CommandLine.Option(
        names = ["--raw", "-r"],
        description = ["Don't do calculations."]
    )
    private var raw = false

    @CommandLine.Option(
        names = ["--interactive", "-i"],
        description = ["Ask for student number and password"]
    )
    private var interactive = false

    override fun call() {
        if (interactive) {
            studentNumber = getInput("Student number: ")
            password = getInput("Password: ", password = true)
        } else if (studentNumber.isBlank() || password.isBlank()) {
            System.err.println("Please specify student number and password." + "")
            return
        }

        getMark(studentNumber, password, apiLevel, quiet, raw)
    }
}

@Command(
    description = ["Run as a server of unofficial YRDSB Teach Assist."],
    name = "server",
    mixinStandardHelpOptions = true,
    version = ["BN$serverBuildNumber"]
)
class Server : Callable<Unit> {
    @Option(
        names = ["--enable-private", "-p"],
        description = ["Enable private server."]
    )
    private var enablePrivate = false

    @Option(
        names = ["--private-port"],
        description = ["Port of private server, default to 5004."]
    )
    private var privatePort = 5004

    @Option(
        names = ["--control-port"],
        description = ["Control port of private server, default to 5006."]
    )
    private var controlPort = 5006

    @Option(
        names = ["--public-port"],
        description = ["Port of public server, default to 5005."]
    )
    private var publicPort = 5005

    @Option(
        names = ["--host", "--db-host"],
        description = ["Host of the database, default to localhost."]
    )
    private var dbHost = "localhost"

    @Option(
        names = ["--port", "--db-port"],
        description = ["Host of the database, default to 27017."]
    )
    private var dbPort = 27017

    @Option(
        names = ["-u", "--db-user"],
        description = ["User of the database, default to root."]
    )
    private var dbUser = "root"

    @Option(
        names = ["--password", "--db-password"],
        description = ["Password of the database, default to password."]
    )
    private var dbPassword = "password"

    override fun call() = startServer(enablePrivate, privatePort, controlPort, publicPort, dbHost, dbPort, dbUser, dbPassword)
}

@Command(
    description = ["Control a running server."],
    name = "ctl",
    mixinStandardHelpOptions = true,
    version = ["BN$serverBuildNumber"],
)
class ServerControl : Callable<Unit> {
    @Option(
        names = ["--target", "-t"],
        description = ["Control url of the private server, default to http://localhost:5006/."]
    )
    private var controlUrl = "http://localhost:5006/"

    @Parameters(index = "0..*")
    private var args = arrayOf<String>()

    override fun call() = exitProcess(serverControl(controlUrl, args))
}
