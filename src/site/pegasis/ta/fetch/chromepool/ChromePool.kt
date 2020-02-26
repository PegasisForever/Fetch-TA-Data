package site.pegasis.ta.fetch.chromepool

import site.pegasis.ta.fetch.getChromeWebDriver
import site.pegasis.ta.fetch.logInfo
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.round

object ChromePool {
    private val chromeDrivers = ArrayList<ChromeDriverWrapper>()
    private const val minChromeCount = 3
    private const val maxChromePageCount = 100
    private const val timerIntervalMinutes = 10
    private var timer: Timer? = null

    fun init() {
        repeat(minChromeCount) {
            chromeDrivers += getChromeWebDriver()
        }
        timer = Timer("ChromePoolCleanTimer")
        timer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                clean()
            }
        }, timerIntervalMinutes * 60 * 1000L, timerIntervalMinutes * 60 * 1000L)
    }

    fun close() {
        timer?.cancel()
        timer = null

        chromeDrivers.forEach { it.driver.close() }
    }

    fun get(): ChromeDriverWrapper {
        return synchronized(this) {
            var driver = chromeDrivers.find { !it.inUse && it.getPageCount <= maxChromePageCount }
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
            val overUsedChromes = chromeDrivers.filter { !it.inUse && it.getPageCount > maxChromePageCount }
            logInfo("Over used chrome drivers: $overUsedChromes")
            chromeDrivers.removeAll(overUsedChromes)
            overUsedChromes.forEach { it.driver.close() }
            logInfo("Over used chrome drivers removed, chrome drivers list: $chromeDrivers")

            val notInUseChromes = chromeDrivers.filter { !it.inUse }
            logInfo("Not in use chrome drivers: $overUsedChromes")
            var maxNotInUseChromeCount = round((chromeDrivers.size - notInUseChromes.size) * 0.4).toInt()
            maxNotInUseChromeCount = maxOf(maxNotInUseChromeCount, minChromeCount)
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

            if(chromeDrivers.size< minChromeCount){
                logInfo("Chrome drivers count less than min, adding")
                repeat(minChromeCount-chromeDrivers.size){
                    chromeDrivers += getChromeWebDriver()
                }
            }
            logInfo("Chrome Pool clean done, chrome drivers list: $chromeDrivers")
        }

    }
}