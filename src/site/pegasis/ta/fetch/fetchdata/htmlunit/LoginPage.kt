package site.pegasis.ta.fetch.fetchdata.htmlunit

import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput
import com.gargoylesoftware.htmlunit.html.HtmlTextInput
import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.fetchdata.getHtmlUnitWebClient
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.tools.findFirst

class LoginPage(private val timing: Timing = Timing()) {
    private val htmlPage: HtmlPage = timing("load login page") {
        getHtmlUnitWebClient().getPage("https://ta.yrdsb.ca/live/index.php")
    }

    init {
        if (htmlPage.titleText != "YRDSB teachassist login") {
            throw Exception("Cannot get correct page.")
        }
    }

    fun gotoSummaryPage(studentNumber: String, password: String): SummaryPage {
        val usernameInput = htmlPage.getElementByName<HtmlTextInput>("username")
        val passwordInput = htmlPage.getElementByName<HtmlPasswordInput>("password")
        usernameInput.valueAttribute = studentNumber
        passwordInput.valueAttribute = password

        val loginBtn = htmlPage.getElementByName<HtmlSubmitInput>("submit")
        val summaryHtmlPage = timing("load summary page"){
            loginBtn.click<HtmlPage>()
        }
        if (summaryHtmlPage.titleText != "Student Reports") {
            val errorCode = findFirst(summaryHtmlPage.url.toString(), "\\d")?.toInt()
            throw LoginException(errorCode)
        }
        return SummaryPage(summaryHtmlPage, timing)
    }

}