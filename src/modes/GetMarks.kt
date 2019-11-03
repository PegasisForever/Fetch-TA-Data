package modes

import modes.server.serializers.CourseListSerializerV3.Companion.serializeCourseList
import webpage.LoginPage

fun getMarks(studentNumber: String, password: String) {
    val summaryPage = LoginPage().gotoSummaryPage(studentNumber, password)
    summaryPage.fillDetails()
    println(serializeCourseList(summaryPage.courses))
}