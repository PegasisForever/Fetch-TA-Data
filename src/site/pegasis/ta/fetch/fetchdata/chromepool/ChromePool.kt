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

    fun init() {
        repeat(Config.chromePoolMinChromeCount) {
            chromeDrivers += getChromeWebDriver()
        }
        timer = Timer("ChromePoolCleanTimer")
        timer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                clean()
            }
        },
            Config.chromePoolCleanIntervalMinute * 60 * 1000L,
            Config.chromePoolCleanIntervalMinute * 60 * 1000L)
    }

    fun close() {
        timer?.cancel()
        timer = null

        chromeDrivers.forEach { it.driver.close() }
    }

    fun get(): ChromeDriverWrapper {
        return synchronized(this) {
            var driver = chromeDrivers.find { !it.inUse && it.getPageCount <= Config.chromePoolMaxChromePageCount }
            driver?.lastAssignTime = ZonedDateTime.now()
            if (driver == null) {
                logInfo("No avaliable chrome drivers: $chromeDrivers, adding")
                driver = getChromeWebDriver()
                chromeDrivers += driver
            }
            driver.inUse = true
            driver
        }
    }

    fun clean() {
        synchronized(this) {
            logInfo("Chrome Pool clean started, chrome drivers list: $chromeDrivers")
            val overUsedChromes = chromeDrivers.filter { !it.inUse && it.getPageCount > Config.chromePoolMaxChromePageCount }
            logInfo("Over used chrome drivers: $overUsedChromes")
            chromeDrivers.removeAll(overUsedChromes)
            overUsedChromes.forEach { it.driver.close() }
            logInfo("Over used chrome drivers removed, chrome drivers list: $chromeDrivers")

            //fixme
            val currentTime = ZonedDateTime.now().toEpochSecond()
            val timeoutChromes = chromeDrivers.filter { it.inUse && currentTime - it.lastAssignTime.toEpochSecond() > 10 * 60 }
            logWarn("Timeout chrome drivers: $timeoutChromes")
            chromeDrivers.removeAll(timeoutChromes)
            timeoutChromes.forEach { it.driver.close() }
            logInfo("Timeout chrome drivers removed, chrome drivers list: $chromeDrivers")

            val notInUseChromes = chromeDrivers.filter { !it.inUse }
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
                spareChromes.forEach { it.driver.close() }
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