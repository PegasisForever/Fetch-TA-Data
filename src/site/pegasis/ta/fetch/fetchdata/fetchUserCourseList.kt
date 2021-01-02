package site.pegasis.ta.fetch.fetchdata

import site.pegasis.ta.fetch.fetchdata.jsoup.JsoupSession
import site.pegasis.ta.fetch.models.CourseList
import site.pegasis.ta.fetch.models.Timing

private suspend fun jsoupFetchCourseList(studentNumber: String, password: String, raw: Boolean, timing: Timing, forceUseProxy: Boolean): CourseList {
    val session = JsoupSession(forceUseProxy)

    try {
        return site.pegasis.ta.fetch.fetchdata.jsoup.LoginPage(session, timing)
            .gotoSummaryPage(studentNumber, password)
            .fillDetails(doCalculation = !raw)
            .courses
    } catch (e: Throwable) {
        throw e
    } finally {
        session.close()
    }
}

suspend fun fetchUserCourseList(
    studentNumber: String,
    password: String,
    raw: Boolean = false,
    timing: Timing = Timing(),
    forceUseProxy: Boolean = false
): CourseList {
    return jsoupFetchCourseList(studentNumber, password, raw, timing, forceUseProxy)
}
