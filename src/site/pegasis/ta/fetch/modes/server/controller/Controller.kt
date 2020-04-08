package site.pegasis.ta.fetch.modes.server.controller

import org.json.simple.JSONArray
import picocli.CommandLine
import picocli.CommandLine.Command
import site.pegasis.ta.fetch.modes.server.route.HttpSession
import site.pegasis.ta.fetch.tools.*
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.Callable

object Controller {
    @Command(
        name = "tacontrol",
        mixinStandardHelpOptions = true,
        version = ["BN$serverBuildNumber"]
    )
    class TAControl : Callable<Unit> {
        override fun call() {}
    }

    suspend fun route(session: HttpSession){
        var statusCode = 200
        var res = ""

        val hash = session.hashCode()
        val reqString = session.getReqString()
        val ipAddress = session.getIP()

        log(
            LogLevel.INFO,
            "Control #$hash <- $ipAddress, data=$reqString"
        )

        try {
            val args = (jsonParser.parse(reqString) as JSONArray)
                .map { it as String }
                .toTypedArray()

            val stringWriter = StringWriter()
            val printWriter = PrintWriter(stringWriter)

            val errStringWriter = StringWriter()
            val errPrintWriter = PrintWriter(errStringWriter)

            val commandLine = CommandLine(TAControl())
                .addSubcommand(Reload(printWriter))
                .addSubcommand(Clean(printWriter))
                .addSubcommand(Regen(printWriter))
            commandLine.out = printWriter
            commandLine.err = errPrintWriter
            commandLine.execute(*args)

            res = stringWriter.toString() + ANSI_RED + errStringWriter.toString() + ANSI_RESET

            logInfo("Control #$hash -> $ipAddress, data=$res")
        } catch (e: Throwable) {
            logError("Control #$hash Unknown error: ${e.message}", e)
            statusCode = 500
            res = ANSI_RED + e.toStackTrace() + ANSI_RESET
        }

        session.send(statusCode, res, false)
    }
}