package site.pegasis.ta.fetch.fetchdata.chromepool

import org.openqa.selenium.chrome.ChromeDriver
import java.time.ZonedDateTime

class ChromeDriverWrapper(val driver: ChromeDriver) {
    var getPageCount = 0
    var user:Any? = null
    var lastAssignTime = ZonedDateTime.now()

    fun get(url: String) {
        driver.get(url)
        getPageCount++
    }

    fun finished() {
        user = null
    }

    override fun toString() = "CDW user: $user getPageCount: $getPageCount"
}