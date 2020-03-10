package site.pegasis.ta.fetch.fetchdata


import site.pegasis.ta.fetch.fetchdata.chrome.LoginPage
import site.pegasis.ta.fetch.fetchdata.chromepool.ChromePool
import site.pegasis.ta.fetch.models.CourseList
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.tools.logInfo
import kotlin.concurrent.thread

private fun jsoupFetchCourseList(studentNumber: String, password: String, raw: Boolean, timing: Timing) =
    site.pegasis.ta.fetch.fetchdata.jsoup.LoginPage(timing)
        .gotoSummaryPage(studentNumber, password)
        .fillDetails(doCalculation = !raw)
        .courses

private fun htmlunitFetchCourseList(studentNumber: String, password: String, raw: Boolean, timing: Timing) =
    site.pegasis.ta.fetch.fetchdata.htmlunit.LoginPage(timing)
        .gotoSummaryPage(studentNumber, password)
        .fillDetails(doCalculation = !raw)
        .courses

private fun chromeFetchCourseList(studentNumber: String, password: String, raw: Boolean, timing: Timing): CourseList {
    val webDriver = ChromePool.get(studentNumber)
    try {
        val courses = LoginPage(webDriver, timing)
            .gotoSummaryPage(studentNumber, password)
            .fillDetails(doCalculation = !raw)
            .courses
        webDriver.finished()
        return courses
    } catch (e: Throwable) {
        webDriver.finished()
        throw e
    }
}

fun fetchUserCourseList(studentNumber: String,
                        password: String,
                        raw: Boolean = false,
                        timing: Timing = Timing(),
                        forceChrome: Boolean = false,
                        parallel: Boolean = false): CourseList {
    if (parallel && !forceChrome && !WebdriverFallbackMap.contains(studentNumber)) {
        var courseList: CourseList? = null
        var error: Throwable? = null
        thread(start = true) {
            try {
                courseList = jsoupFetchCourseList(studentNumber, password, raw, timing)
            } catch (e: Throwable) {
                if (e.isHtmlunitError()) {
                    logInfo("Fetch course list for ${studentNumber}: Fallback to web driver")
                    WebdriverFallbackMap += studentNumber
                } else {
                    error = e
                }
            }
        }
        thread(start = true) {
            try {
                courseList = chromeFetchCourseList(studentNumber, password, raw, timing)
            } catch (e: Throwable) {
                error = e
            }
        }

        while (courseList == null && error == null) Thread.sleep(20)
        if (error != null) throw error!!
        return courseList!!
    } else {
        if (WebdriverFallbackMap.contains(studentNumber) || forceChrome) {
            return chromeFetchCourseList(studentNumber, password, raw, timing)
        }
        return try {
            jsoupFetchCourseList(studentNumber, password, raw, timing)
        } catch (e: Throwable) {
            if (e.isHtmlunitError()) {
                logInfo("Fetch course list for ${studentNumber}: Fallback to web driver")
                WebdriverFallbackMap += studentNumber
                chromeFetchCourseList(studentNumber, password, raw, timing)
            } else {
                throw e
            }
        }
    }
}