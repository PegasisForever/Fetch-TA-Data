package modes

import modes.server.parsers.CourseListParser.Companion.parseCourseList
import modes.server.serializers.CourseListSerializerV2
import modes.server.serializers.CourseListSerializerV2.Companion.serializeCourseList
import webpage.LoginPage

fun getMarks(studentNumber: String, password: String) {
    val summaryPage = LoginPage().gotoSummaryPage(studentNumber, password)
    summaryPage.fillDetails()
    println(serializeCourseList(summaryPage.courses))
}