package site.pegasis.ta.fetch.modes.server.controller

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import picocli.CommandLine
import picocli.CommandLine.Command
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.modes.ctl.CONTROL_API_VERSION
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

class ControllerResponse {
    interface Line {
        val text: String
        fun toJSON(): JSONObject
    }

    data class StdLine(override val text: String) : Line {
        override fun toJSON(): JSONObject {
            val obj = JSONObject()
            obj["type"] = "std"
            obj["text"] = text
            return obj
        }
    }

    data class ErrLine(override val text: String) : Line {
        override fun toJSON(): JSONObject {
            val obj = JSONObject()
            obj["type"] = "err"
            obj["text"] = text
            return obj
        }
    }

    private val stdStringWriter = StringWriter()
    val stdPrintWriter = PrintWriter(stdStringWriter)

    private val errStringWriter = StringWriter()
    val errPrintWriter = PrintWriter(errStringWriter)

    private val lines = arrayListOf<Line>()

    var exitCode = 0

    fun writeStdLine(text: String) = lines.add(StdLine(text))
    fun writeErrLine(text: String) = lines.add(ErrLine(text))

    fun toJSON(): JSONObject {
        val obj = JSONObject()
        obj["lines"] = lines
            .map { it.toJSON() }
            .toJSONArray()
            .apply {
                this += StdLine(stdStringWriter.toString().trimEnd()).toJSON()
                this += ErrLine(errStringWriter.toString().trimEnd()).toJSON()
            }
        obj["exit_code"] = exitCode
        obj["version"] = CONTROL_API_VERSION
        return obj
    }
}

class Controller : BaseRoute() {
    override fun path() = "/"

    override fun isController() = true

    override suspend fun route(session: HttpSession, timing: Timing): Response {
        var statusCode = 200
        val response = ControllerResponse()

        val hash = session.hashCode()
        val reqString = session.getReqString()

        try {
            val args = (JSONParser().parse(reqString) as JSONArray)
                .map { it as String }
                .toTypedArray()

            val commandLine = CommandLine(TAControl())
                .addSubcommand(HealthManager(response))
                .addSubcommand(Reload(response))
            commandLine.out = response.stdPrintWriter
            commandLine.err = response.errPrintWriter
            commandLine.execute(*args)
        } catch (e: Throwable) {
            logError("Control #$hash Unknown error: ${e.message}", e)
            statusCode = 500
            response.writeErrLine(e.toStackTrace())
            response.exitCode = 1
        }

        return Response(statusCode, response.toJSON().toJSONString(), false)
    }
}
