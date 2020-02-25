package site.pegasis.ta.fetch.webpage

import org.openqa.selenium.chrome.ChromeDriver
import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.findFirst
import site.pegasis.ta.fetch.getWebClient
import site.pegasis.ta.fetch.models.Timing

class LoginPage(private val timing: Timing = Timing(),
                private var webClient: ChromeDriver? = null) {

    init {
        if (webClient == null) webClient = getWebClient()
        timing("load login page") {
            webClient!!.get("https://ta.yrdsb.ca/live/index.php")
        }
        if (webClient!!.title != "YRDSB teachassist login") {
            throw Exception("Cannot get correct page.")
        }
    }

    fun gotoSummaryPage(studentNumber: String, password: String): SummaryPage {
        webClient!!.findElementByName("username").sendKeys(studentNumber)
        webClient!!.findElementByName("password").sendKeys(password)

        timing("load summary page") {
            webClient!!.findElementByName("submit").click()
        }
        if (webClient!!.title != "Student Reports") {
            val errorCode = findFirst(webClient!!.currentUrl.toString(), "\\d")?.toInt()
            throw LoginException(errorCode)
        }
        return SummaryPage(webClient!!, timing)
    }

}