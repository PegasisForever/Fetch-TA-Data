package site.pegasis.ta.fetch.webpage

import site.pegasis.ta.fetch.logInfo
import site.pegasis.ta.fetch.models.CourseList
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.webpage.chrome.LoginPage
import kotlin.concurrent.thread

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
                courseList = site.pegasis.ta.fetch.webpage.htmlunit.LoginPage(timing)
                    .gotoSummaryPage(studentNumber, password)
                    .fillDetails(doCalculation = !raw)
                    .courses
            } catch (e: Throwable) {
                logInfo("Fetch course list for ${studentNumber}: Fallback to web driver")
                WebdriverFallbackMap += studentNumber
            }
        }
        thread(start = true) {
            courseList = LoginPage(timing)
                .gotoSummaryPage(studentNumber, password)
                .fillDetails(doCalculation = !raw)
                .courses
        }

        while (courseList == null) {
            Thread.sleep(20)
        }
        return courseList!!
    } else {
        if (WebdriverFallbackMap.contains(studentNumber) || forceChrome) {
            return LoginPage(timing)
                .gotoSummaryPage(studentNumber, password)
                .fillDetails(doCalculation = !raw)
                .courses
        }
        return try {
            site.pegasis.ta.fetch.webpage.htmlunit.LoginPage(timing)
                .gotoSummaryPage(studentNumber, password)
                .fillDetails(doCalculation = !raw)
                .courses
        } catch (e: Throwable) {
            logInfo("Fetch course list for ${studentNumber}: Fallback to web driver")
            WebdriverFallbackMap += studentNumber
            LoginPage(timing)
                .gotoSummaryPage(studentNumber, password)
                .fillDetails(doCalculation = !raw)
                .courses
        }
    }
}