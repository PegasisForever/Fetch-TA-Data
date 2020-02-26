package site.pegasis.ta.fetch.chromepool

import org.openqa.selenium.chrome.ChromeDriver

class ChromeDriverWrapper(val driver: ChromeDriver) {
    var getPageCount = 0
    var inUse = false

    fun get(url: String) {
        driver.get(url)
        getPageCount++
    }

    fun finished() {
        inUse = false
    }

    override fun toString() = "CDW inUse: $inUse getPageCount: $getPageCount"
}