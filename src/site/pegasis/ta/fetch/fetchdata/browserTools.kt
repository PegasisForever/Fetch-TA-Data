package site.pegasis.ta.fetch.fetchdata

import com.gargoylesoftware.htmlunit.WebClient
import org.openqa.selenium.By
import org.openqa.selenium.UnexpectedAlertBehaviour
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.CapabilityType
import org.openqa.selenium.remote.DesiredCapabilities
import site.pegasis.ta.fetch.fetchdata.chromepool.ChromeDriverWrapper
import site.pegasis.ta.fetch.modes.server.storage.Config
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

fun getChromeWebDriver(showGui: Boolean = false): ChromeDriverWrapper {
    Logger.getLogger("org.openqa.selenium").level = Level.OFF
    System.setProperty("webdriver.chrome.driver", Config.webDriverPath)
    System.setProperty("webdriver.chrome.silentLogging", "true")
    System.setProperty("webdriver.chrome.silentOutput", "true")
    val options = ChromeOptions().apply {
        setExperimentalOption("prefs",
            hashMapOf("profile.default_content_setting_values" to
                hashMapOf("images" to 2, "stylesheet" to 2, "javascript" to 2)))
        addArguments("--silent", "--ignore-certificate-errors")
        if (!showGui) addArguments("--headless", "--disable-gpu")

        merge(DesiredCapabilities().apply {
            isJavascriptEnabled = false
            setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.DISMISS)
        })
    }
    val driver = ChromeDriver(options).apply {
        this.manage().timeouts().pageLoadTimeout(Config.fetchTimeoutSecond.toLong(), TimeUnit.SECONDS)
    }
    return ChromeDriverWrapper(driver)
}

fun WebElement.getDirectChildren() = findElements(By.xpath("*"))


fun getHtmlUnitWebClient() = WebClient().apply {
    Logger.getLogger("com.gargoylesoftware").level = Level.OFF
    with(options) {
        isCssEnabled = false
        isJavaScriptEnabled = false
        isRedirectEnabled = true
        isThrowExceptionOnScriptError = false
        isThrowExceptionOnFailingStatusCode = false
        isDownloadImages = false
        isAppletEnabled = false
        timeout = Config.fetchTimeoutSecond * 1000
    }
}

fun Throwable.isHtmlunitError() = stackTrace.find { it.className.contains("net.sourceforge.htmlunit") } != null

fun Throwable.isConnectionException() = this is java.net.SocketTimeoutException ||
    this is org.apache.http.conn.ConnectTimeoutException ||
    this is org.openqa.selenium.TimeoutException ||
    this is org.apache.http.conn.HttpHostConnectException ||
    (message ?: "").indexOf("SocketTimeoutException") != -1
