package site.pegasis.ta.fetch.modes.server

import com.sun.management.OperatingSystemMXBean
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import site.pegasis.ta.fetch.tools.fileExists
import site.pegasis.ta.fetch.tools.logInfo
import site.pegasis.ta.fetch.tools.logWarn
import site.pegasis.ta.fetch.tools.readFile
import java.lang.management.ManagementFactory
import java.time.Duration

object LoadManager {
    private var cores = 1.0
    private var memoryBytes = 1024L

    var cpuPercentage = 0.0
        private set
    var memoryPercentage = 0.0
        private set

    private const val CPU_OVERLOAD_THRESHOLD_PERCENTAGE = 0.9
    private const val MEMORY_OVERLOAD_THRESHOLD_PERCENTAGE = 0.9

    suspend fun init() {
        if (isCgroupFileExist()) {
            // update core count and memory every 10 min
            GlobalScope.launch {
                while (isActive) {
                    val cfsQuota = readFile("/sys/fs/cgroup/cpu/cpu.cfs_quota_us").trim().toLong()
                    cores = if (cfsQuota == -1L) {
                        Runtime.getRuntime().availableProcessors().toDouble()
                    } else {
                        val cfsPeriod = readFile("/sys/fs/cgroup/cpu/cpu.cfs_period_us").trim().toDouble()
                        cfsQuota / cfsPeriod
                    }

                    memoryBytes = minOf(
                        readFile("/sys/fs/cgroup/memory/memory.limit_in_bytes").trim().toLong(),
                        (ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean).totalMemorySize,
                    )
                    delay(Duration.ofMinutes(10))
                }
            }

            // update cpu and ram percentage every 2 sec
            GlobalScope.launch {
                var lastTime = System.nanoTime()
                var lastUsage = readFile("/sys/fs/cgroup/cpu/cpuacct.usage").trim().toLong()

                delay(Duration.ofSeconds(2))
                while (isActive) {
                    val time = System.nanoTime()
                    val usage = readFile("/sys/fs/cgroup/cpu/cpuacct.usage").trim().toLong()
                    val timeDelta = time - lastTime
                    val usageDelta = usage - lastUsage
                    lastTime = time
                    lastUsage = usage

                    cpuPercentage = usageDelta.toDouble() / timeDelta / cores
                    val memUsageBytes = getMemUsageBytes()
                    memoryPercentage = memUsageBytes.toDouble() / memoryBytes

                    delay(Duration.ofSeconds(2))
                }
            }
        } else {
            logWarn("Can't read cgroup files, load manager disabled.")
        }
    }

    private suspend fun getMemUsageBytes(): Long {
        val stats = readFile("/sys/fs/cgroup/memory/memory.stat").split("\n")
        for (stat in stats) {
            val (name, value) = stat.split(" ")
            if (name == "total_rss") return value.toLong()
        }
        error("No rss entry")
    }

    private suspend fun isCgroupFileExist() = fileExists("/sys/fs/cgroup/cpu/cpu.cfs_period_us") &&
        fileExists("/sys/fs/cgroup/cpu/cpu.cfs_quota_us") &&
        fileExists("/sys/fs/cgroup/cpu/cpuacct.usage") &&
        fileExists("/sys/fs/cgroup/memory/memory.limit_in_bytes") &&
        fileExists("/sys/fs/cgroup/memory/memory.stat")

    fun isOverLoad() = cpuPercentage > CPU_OVERLOAD_THRESHOLD_PERCENTAGE ||
        memoryPercentage > MEMORY_OVERLOAD_THRESHOLD_PERCENTAGE
}
