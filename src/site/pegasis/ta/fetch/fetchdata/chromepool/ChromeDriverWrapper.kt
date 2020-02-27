package site.pegasis.ta.fetch.fetchdata.chromepool

import org.openqa.selenium.chrome.ChromeDriver
import java.time.ZonedDateTime

class ChromeDriverWrapper(val driver: ChromeDriver) {
    var getPageCount = 0
    var inUse = false
    var lastAssignTime = ZonedDateTime.now()

    fun get(url: String) {
        driver.get(url)
        getPageCount++
    }

    fun finished() {
        inUse = false
    }

    override fun toString() = "CDW inUse: $inUse getPageCount: $getPageCount"
}