package site.pegasis.ta.fetch.fetchdata.chrome

import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.fetchdata.chromepool.ChromeDriverWrapper
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.tools.findFirst

class LoginPage(private val webDriver: ChromeDriverWrapper, private val timing: Timing = Timing()) {
    init {
        timing("load login page") {
            webDriver.get("https://ta.yrdsb.ca/live/index.php")
        }
        if (webDriver.driver.title != "YRDSB teachassist login") {
            throw Exception("Cannot get correct page.")
        }
    }

    fun gotoSummaryPage(studentNumber: String, password: String): SummaryPage {
        webDriver.driver.findElementByName("username").sendKeys(studentNumber)
        webDriver.driver.findElementByName("password").sendKeys(password)

        timing("load summary page") {
            webDriver.driver.findElementByName("submit").click()
        }
        if (webDriver.driver.title != "Student Reports") {
            val errorCode = findFirst(webDriver.driver.currentUrl.toString(), "\\d")?.toInt()
            throw LoginException(errorCode)
        }
        return SummaryPage(webDriver, timing)
    }
}