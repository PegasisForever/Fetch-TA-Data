package site.pegasis.ta.fetch.fetchdata.chromepool

import site.pegasis.ta.fetch.fetchdata.getChromeWebDriver
import site.pegasis.ta.fetch.modes.server.storage.Config
import site.pegasis.ta.fetch.tools.logInfo
import site.pegasis.ta.fetch.tools.logWarn
import java.time.ZonedDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.round

object ChromePool {
    private val chromeDrivers = ArrayList<ChromeDriverWrapper>()
    private var timer: Timer? = null

    private fun startTimer() {
        timer?.cancel()
        timer = Timer("ChromePoolCleanTimer")
        timer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                clean()
            }
        },
            Config.chromePoolCleanIntervalMinute * 60 * 1000L,
            Config.chromePoolCleanIntervalMinute * 60 * 1000L)
    }

    fun init() {
        repeat(Config.chromePoolMinChromeCount) {
            chromeDrivers += getChromeWebDriver()
        }
        startTimer()
    }

    fun reload() {
        //So cleaner can clean existing chromes and create new ones
        chromeDrivers.forEach {
            it.getPageCount = Config.chromePoolMaxChromePageCount + 1
        }
        clean()
        startTimer()
    }

    fun close() {
        timer?.cancel()
        timer = null

        chromeDrivers.forEach { it.kill() }
    }

    fun get(user: Any): ChromeDriverWrapper {
        return synchronized(this) {
            var driver = chromeDrivers.find { it.user == null && it.getPageCount <= Config.chromePoolMaxChromePageCount }
            driver?.lastAssignTime = ZonedDateTime.now()
            if (driver == null) {
                logInfo("No available chrome drivers: $chromeDrivers, adding")
                driver = getChromeWebDriver()
                chromeDrivers += driver
            }
            driver.user = user
            driver
        }
    }

    fun clean() {
        synchronized(this) {
            logInfo("Chrome Pool clean started, chrome drivers list: $chromeDrivers")
            val overUsedChromes = chromeDrivers.filter { it.user == null && it.getPageCount > Config.chromePoolMaxChromePageCount }
            logInfo("Over used chrome drivers: $overUsedChromes")
            chromeDrivers.removeAll(overUsedChromes)
            overUsedChromes.forEach { it.kill() }
            logInfo("Over used chrome drivers removed, chrome drivers list: $chromeDrivers")

            //fixme
            val currentTime = ZonedDateTime.now().toEpochSecond()
            val timeoutChromes = chromeDrivers.filter { it.user != null && currentTime - it.lastAssignTime.toEpochSecond() > 10 * 60 }
            logWarn("Timeout chrome drivers: $timeoutChromes")
            chromeDrivers.removeAll(timeoutChromes)
            timeoutChromes.forEach { it.kill() }
            logInfo("Timeout chrome drivers removed, chrome drivers list: $chromeDrivers")

            val notInUseChromes = chromeDrivers.filter { it.user == null }
            logInfo("Not in use chrome drivers: $overUsedChromes")
            var maxNotInUseChromeCount = round((chromeDrivers.size - notInUseChromes.size) * 0.4).toInt()
            maxNotInUseChromeCount = maxOf(maxNotInUseChromeCount, Config.chromePoolMinChromeCount)
            logInfo("Calculated max not in use chrome count: $maxNotInUseChromeCount, notInUseChromes.size: ${notInUseChromes.size}")
            if (maxNotInUseChromeCount < notInUseChromes.size) {
                val spareChromes = notInUseChromes
                    .sortedBy { it.getPageCount }
                    .takeLast(notInUseChromes.size - maxNotInUseChromeCount)
                logInfo("Spare chrome drivers: $spareChromes")
                chromeDrivers.removeAll(spareChromes)
                spareChromes.forEach { it.kill() }
                logInfo("Spare chrome drivers removed, chrome drivers list: $chromeDrivers")
            }

            if (chromeDrivers.size < Config.chromePoolMinChromeCount) {
                logInfo("Chrome drivers count less than min, adding")
                repeat(Config.chromePoolMinChromeCount - chromeDrivers.size) {
                    chromeDrivers += getChromeWebDriver()
                }
            }
            logInfo("Chrome Pool clean done, chrome drivers list: $chromeDrivers")
        }

    }
}

fun main() {
    val time1 = ZonedDateTime.now()
    Thread.sleep(3000)
    println(time1.toEpochSecond() - ZonedDateTime.now().toEpochSecond())
}