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

fun getChromeWebDriver(): ChromeDriverWrapper {
    Logger.getLogger("org.openqa.selenium").level = Level.OFF
    System.setProperty("webdriver.chrome.driver", Config.webDriverPath)
    System.setProperty("webdriver.chrome.silentLogging", "true")
    System.setProperty("webdriver.chrome.silentOutput", "true")
    val options = ChromeOptions().apply {
        setExperimentalOption("prefs",
            hashMapOf("profile.default_content_setting_values" to
                hashMapOf("images" to 2, "stylesheet" to 2, "javascript" to 2)))
        addArguments("--silent",
            "--headless",
            "--disable-gpu",
            "--window-size=300,200",
            "--ignore-certificate-errors")
        merge(DesiredCapabilities().apply {
            isJavascriptEnabled = false
            setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.DISMISS)
        })
    }
    val driver = ChromeDriver(options).apply {
        this.manage().timeouts().pageLoadTimeout(40, TimeUnit.SECONDS)
    }
    return ChromeDriverWrapper(driver)
}

fun WebElement.getDirectChildren() = findElements(By.xpath("*"))


fun getHtmlUnitWebClient() = WebClient().apply {
    Logger.getLogger("com.gargoylesoftware").level = Level.OFF
    options.isCssEnabled = false
    options.isJavaScriptEnabled = false
    options.isRedirectEnabled = true
    options.isThrowExceptionOnScriptError = false
    options.isThrowExceptionOnFailingStatusCode = false
    options.isDownloadImages = false
    options.isAppletEnabled = false
    options.timeout = 100000
}

fun Throwable.isHtmlunitError() = stackTrace.find { it.className.contains("net.sourceforge.htmlunit") } != null

fun Throwable.isTimeoutException() = (message ?: "").indexOf("SocketTimeoutException") != -1