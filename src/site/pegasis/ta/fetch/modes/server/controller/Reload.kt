package site.pegasis.ta.fetch.modes.server.controller

import kotlinx.coroutines.runBlocking
import picocli.CommandLine.Command
import site.pegasis.ta.fetch.modes.server.storage.Config
import site.pegasis.ta.fetch.modes.server.storage.ProxyManager
import site.pegasis.ta.fetch.modes.server.timeline.updateAutoUpdateThread
import site.pegasis.ta.fetch.tools.serverBuildNumber
import java.io.PrintWriter
import java.util.concurrent.Callable

@Command(
    name = "reload",
    description = ["Reload server config and all cached file."],
    mixinStandardHelpOptions = true,
    version = ["BN$serverBuildNumber"]
)
class Reload(private val controllerResponse: ControllerResponse): Callable<Unit> {
    override fun call() {
        runBlocking {
            Config.load()
            updateAutoUpdateThread()
            ProxyManager.updateConfig()
        }

        controllerResponse.writeStdLine("Server config and cached file reloaded.")
    }
}
