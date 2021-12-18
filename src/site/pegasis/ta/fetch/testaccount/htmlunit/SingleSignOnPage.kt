package site.pegasis.ta.fetch.testaccount.htmlunit

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException
import com.gargoylesoftware.htmlunit.ProxyConfig
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import site.pegasis.ta.fetch.modes.server.storage.Config
import site.pegasis.ta.fetch.modes.server.storage.ProxyManager
import java.util.logging.Level
import java.util.logging.Logger

object SingleSignOnPage {
    private fun getHtmlUnitWebClient() = WebClient().apply {
        Logger.getLogger("com.gargoylesoftware").level = Level.OFF
        with(options) {
            val proxy = ProxyManager.getRandomProxy()
            if (proxy != null) {
                proxyConfig = ProxyConfig(proxy.ip, 81, false)
            }
            isCssEnabled = false
            isJavaScriptEnabled = true
            isRedirectEnabled = false
            isThrowExceptionOnScriptError = false
            isThrowExceptionOnFailingStatusCode = true
            isDownloadImages = false
            isDoNotTrackEnabled = true
            isAppletEnabled = false
            timeout = Config.fetchTimeoutSecond.toInt() * 1000
        }
    }

    suspend fun testAccount(studentNumber: String, password: String, forceUseProxy: Boolean): Boolean {
        val client = getHtmlUnitWebClient()
        return try {
            val page = withContext(Dispatchers.IO) {
                client.getPage<HtmlPage>("https://google.yrdsb.ca/LoginFormIdentityProvider/Login.aspx?ReturnUrl=%2fLoginFormIdentityProvider%2fDefault.aspx")
            }

            val form = page.getElementById("loginForm") as HtmlForm
            val studentNumberInput = form.getInputByName<HtmlTextInput>("UserName")
            val passwordInput = form.getInputByName<HtmlPasswordInput>("Password")
            val loginButton = form.getInputByName<HtmlSubmitInput>("LoginButton")

            val returnPage = withContext(Dispatchers.IO) {
                studentNumberInput.type(studentNumber)
                passwordInput.type(password)
                loginButton.click<HtmlPage>()
            }

            returnPage.titleText != "YRDSB Google Apps Single Sign On"
        } catch (e: Throwable) {
            if (e is FailingHttpStatusCodeException && e.statusCode == 302) {
                true
            } else {
                throw e
            }
        } finally {
            client.close()
        }
    }
}
