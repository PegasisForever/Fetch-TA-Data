package site.pegasis.ta.fetch.testaccount

import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.testaccount.htmlunit.SingleSignOnPage

suspend fun testUserAccount(studentNumber: String, password: String, timing: Timing = Timing(), forceUseProxy: Boolean = false): Boolean {
    return timing("test account") {
        SingleSignOnPage.testAccount(studentNumber, password, forceUseProxy)
    }
}
