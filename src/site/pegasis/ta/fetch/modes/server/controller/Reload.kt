package site.pegasis.ta.fetch.modes.server.controller

import picocli.CommandLine.Command
import site.pegasis.ta.fetch.models.User
import site.pegasis.ta.fetch.modes.server.storage.Config
import site.pegasis.ta.fetch.modes.server.storage.LastUpdateTime
import site.pegasis.ta.fetch.modes.server.storage.PCache
import site.pegasis.ta.fetch.modes.server.timeline.updateAutoUpdateThread
import site.pegasis.ta.fetch.serverBuildNumber
import site.pegasis.ta.fetch.webpage.WebdriverFallbackMap
import java.io.PrintWriter
import java.util.concurrent.Callable

@Command(
    name = "reload",
    description = ["Reload server config and all cached file."],
    mixinStandardHelpOptions = true,
    version = ["BN$serverBuildNumber"]
)
class Reload(private val printWriter: PrintWriter): Callable<Unit> {
    override fun call() {
        Config.load()
        User.load()
        LastUpdateTime.load()
        WebdriverFallbackMap.load()
        PCache.clearCache()
        updateAutoUpdateThread()

        printWriter.println("Server config and cached file reloaded.")
    }
}