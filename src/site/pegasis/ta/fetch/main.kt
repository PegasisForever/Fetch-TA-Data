package site.pegasis.ta.fetch

import picocli.CommandLine
import picocli.CommandLine.*
import site.pegasis.ta.fetch.modes.getMark
import site.pegasis.ta.fetch.modes.server.latestApiVersion
import site.pegasis.ta.fetch.modes.server.minApiVersion
import site.pegasis.ta.fetch.modes.server.startServer
import site.pegasis.ta.fetch.modes.server.storage.Config
import site.pegasis.ta.fetch.modes.server.storage.initFiles
import site.pegasis.ta.fetch.fetchdata.WebdriverFallbackMap
import java.util.concurrent.Callable
import java.util.logging.Level

fun main(args: Array<String>) {
    initFiles()
    Config.load()
    WebdriverFallbackMap.load()
    CommandLine(FetchTa())
        .addSubcommand(GetMark())
        .addSubcommand(Server())
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
        if (apiVersion < minApiVersion || apiVersion > latestApiVersion) {
            error("Api version must between $minApiVersion and $latestApiVersion.")
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
        description = ["API Level of the output JSON, default to $latestApiVersion."],
        converter = [ApiVersionConverter::class]
    )
    private var apiLevel = latestApiVersion

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

    override fun call() = startServer(enablePrivate, privatePort, controlPort, publicPort)
}