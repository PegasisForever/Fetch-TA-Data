package webpage

import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput
import com.gargoylesoftware.htmlunit.html.HtmlTextInput
import find
import getWebClient

class LoginPage {
    val htmlPage: HtmlPage = getWebClient().getPage("https://ta.yrdsb.ca/live/index.php")

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
        val summaryHtmlPage = loginBtn.click<HtmlPage>()
        if (summaryHtmlPage.titleText != "Student Reports") {
            val errorMsg = find(summaryHtmlPage.url.toString(), "\\d")[0]
            throw Exception("Login error, code: ${errorMsg}")
        }
        return SummaryPage(summaryHtmlPage)
    }

}