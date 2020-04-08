package site.pegasis.ta.fetch.fetchdata

import site.pegasis.ta.fetch.fetchdata.jsoup.LoginPage
import site.pegasis.ta.fetch.models.CourseList
import site.pegasis.ta.fetch.models.Timing

private suspend fun jsoupFetchCourseList(studentNumber: String, password: String, raw: Boolean, timing: Timing, requester: NetworkRequester) =
    LoginPage(requester, timing)
        .gotoSummaryPage(studentNumber, password)
        .fillDetails(doCalculation = !raw)
        .closeSession()
        .courses

suspend fun fetchUserCourseList(studentNumber: String,
                                password: String,
                                raw: Boolean = false,
                                timing: Timing = Timing(),
                                requester: NetworkRequester = KtorNetworkRequester()): CourseList {
    return jsoupFetchCourseList(studentNumber, password, raw, timing, requester)
}