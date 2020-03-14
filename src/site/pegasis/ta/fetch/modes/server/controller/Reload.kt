package site.pegasis.ta.fetch.modes.server.controller

import picocli.CommandLine.Command
import site.pegasis.ta.fetch.fetchdata.WebdriverFallbackMap
import site.pegasis.ta.fetch.fetchdata.chromepool.ChromePool
import site.pegasis.ta.fetch.models.User
import site.pegasis.ta.fetch.modes.server.storage.*
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
class Reload(private val printWriter: PrintWriter): Callable<Unit> {
    override fun call() {
        Config.load()
        User.load()
        LastUserUpdateTime.load()
        LastUpdateDoneTime.load()
        WebdriverFallbackMap.load()
        CalendarData.load()
        PCache.clearCache()
        ChromePool.reload()
        updateAutoUpdateThread()

        printWriter.println("Server config and cached file reloaded.")
    }
}