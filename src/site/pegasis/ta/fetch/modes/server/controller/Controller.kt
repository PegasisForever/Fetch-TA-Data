package site.pegasis.ta.fetch.modes.server.controller

import org.json.simple.JSONArray
import picocli.CommandLine
import picocli.CommandLine.Command
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.modes.server.route.BaseRoute
import site.pegasis.ta.fetch.modes.server.route.HttpSession
import site.pegasis.ta.fetch.modes.server.route.Response
import site.pegasis.ta.fetch.tools.*
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.Callable

@Command(
    name = "tacontrol",
    mixinStandardHelpOptions = true,
    version = ["BN$serverBuildNumber"]
)
class TAControl : Callable<Unit> {
    override fun call() {}
}

class Controller: BaseRoute() {
    override fun path() = "/"

    override fun isController() = true

    override suspend fun route(session: HttpSession, timing: Timing): Response {
        var statusCode = 200
        var res: String

        val hash = session.hashCode()
        val reqString = session.getReqString()
        val ipAddress = session.getIP()

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
                .addSubcommand(Migrate(printWriter))
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

        return Response(statusCode, res, false)
    }
}
