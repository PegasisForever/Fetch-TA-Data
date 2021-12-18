package site.pegasis.ta.fetch.fetchdata

import site.pegasis.ta.fetch.fetchdata.jsoup.JsoupSession
import site.pegasis.ta.fetch.models.CourseList
import site.pegasis.ta.fetch.models.Timing

private suspend fun jsoupFetchCourseList(studentNumber: String, password: String, raw: Boolean, timing: Timing): CourseList {
    return site.pegasis.ta.fetch.fetchdata.jsoup.LoginPage( JsoupSession(), timing)
        .gotoSummaryPage(studentNumber, password)
        .fillDetails(doCalculation = !raw)
        .courses
}

suspend fun fetchUserCourseList(
    studentNumber: String,
    password: String,
    raw: Boolean = false,
    timing: Timing = Timing(),
): CourseList {
    return jsoupFetchCourseList(studentNumber, password, raw, timing)
}
