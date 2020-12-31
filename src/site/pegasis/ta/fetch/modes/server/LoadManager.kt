package site.pegasis.ta.fetch.modes.server

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import site.pegasis.ta.fetch.tools.fileExists
import site.pegasis.ta.fetch.tools.logWarn
import site.pegasis.ta.fetch.tools.readFile
import java.time.Duration

object LoadManager {
    var maxLoad = 2
    var load = 0.0

    suspend fun init() {
        if (isLoadFileExist()) {
            // update maxLoad (based on core count) every 10 min
            GlobalScope.launch {
                while (isActive) {
                    delay(Duration.ofMinutes(10))
                    maxLoad = Runtime.getRuntime().availableProcessors() * 2
                }
            }

            // update load every 5 sec
            GlobalScope.launch {
                while (isActive) {
                    delay(Duration.ofMinutes(1))
                    load = getLoad5()
                }
            }
        } else {
            logWarn("Can't read file /proc/loadavg, load manager disabled.")
        }
    }

    private suspend fun isLoadFileExist() = fileExists("/proc/loadavg")

    private suspend fun getLoad5() = readFile("/proc/loadavg").split(" ")[1].toDouble()

    fun isOverLoad() = load > maxLoad
}
