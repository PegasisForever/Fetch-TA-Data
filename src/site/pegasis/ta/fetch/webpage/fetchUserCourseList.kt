package site.pegasis.ta.fetch.webpage

import site.pegasis.ta.fetch.logInfo
import site.pegasis.ta.fetch.models.CourseList
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.webpage.chrome.LoginPage
import kotlin.concurrent.thread

private fun htmlunitFetchCourseList(studentNumber: String, password: String, raw: Boolean, timing: Timing) =
    site.pegasis.ta.fetch.webpage.htmlunit.LoginPage(timing)
        .gotoSummaryPage(studentNumber, password)
        .fillDetails(doCalculation = !raw)
        .courses

private fun chromeFetchCourseList(studentNumber: String, password: String, raw: Boolean, timing: Timing) =
    LoginPage(timing)
        .gotoSummaryPage(studentNumber, password)
        .fillDetails(doCalculation = !raw)
        .courses

fun fetchUserCourseList(studentNumber: String,
                        password: String,
                        raw: Boolean = false,
                        timing: Timing = Timing(),
                        forceChrome: Boolean = false,
                        parallel: Boolean = false): CourseList {
    if (parallel && !forceChrome && !WebdriverFallbackMap.contains(studentNumber)) {
        var courseList: CourseList? = null
        thread(start = true) {
            try {
                courseList = htmlunitFetchCourseList(studentNumber, password, raw, timing)
            } catch (e: Throwable) {
                logInfo("Fetch course list for ${studentNumber}: Fallback to web driver")
                WebdriverFallbackMap += studentNumber
            }
        }
        thread(start = true) {
            courseList = chromeFetchCourseList(studentNumber, password, raw, timing)
        }

        while (courseList == null) {
            Thread.sleep(20)
        }
        return courseList!!
    } else {
        if (WebdriverFallbackMap.contains(studentNumber) || forceChrome) {
            return chromeFetchCourseList(studentNumber, password, raw, timing)
        }
        return try {
            htmlunitFetchCourseList(studentNumber, password, raw, timing)
        } catch (e: Throwable) {
            logInfo("Fetch course list for ${studentNumber}: Fallback to web driver")
            WebdriverFallbackMap += studentNumber
            chromeFetchCourseList(studentNumber, password, raw, timing)
        }
    }
}