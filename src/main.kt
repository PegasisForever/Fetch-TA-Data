import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import modes.getMark
import modes.server.parsers.CourseListParsers
import modes.server.startServer
import java.util.logging.Level

fun main(args: Array<String>) = FetchTa()
    .subcommands(GetMark(), Server())
    .main(args)

class FetchTa : CliktCommand() {
    override fun run() {
        java.util.logging.Logger.getLogger("com.gargoylesoftware").level = Level.OFF
    }
}

class GetMark : CliktCommand(help = "Fetch a student's mark from YRDSB Teach Assist") {
    private val studentNumber by argument()
    private val password by argument()
    private val apiLevel by option(
        "--api", "-a",
        help = "API Level of the output JSON, default to ${CourseListParsers.keys.max()}"
    )
        .choice(*CourseListParsers.keys.map { it.toString() }.toTypedArray())
        .default(CourseListParsers.keys.max()!!.toString())
    private val quiet by option(
        "--quiet", "-q",
        help = "Don't output logs"
    )
        .flag(default = false)

    override fun run() {
        getMark(studentNumber, password, apiLevel.toInt(), quiet)
    }
}

class Server : CliktCommand(help = "Run as a server of unofficial YRDSB Teach Assist") {
    private val enablePrivate by option(
        "--enable-private", "-p",
        help = "Enable private server"
    )
        .flag(default = false)
    private val privatePort by option(
        "--private-port",
        help = "Port of private server, default to 5004"
    )
        .int()
        .default(5004)
    private val publicPort by option(
        "--public-port",
        help = "Port of public server, default to 5005"
    )
        .int()
        .default(5005)

    override fun run() {
        startServer(enablePrivate, privatePort, publicPort)
    }
}