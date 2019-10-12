package modes

import modes.server.Serializers.CourseListSerializerV2
import webpage.LoginPage


fun getMarks(studentNumber: String, password: String) {
    val summaryPage = LoginPage().gotoSummaryPage(studentNumber, password)
    summaryPage.fillDetails()
    println(CourseListSerializerV2.serializeCourseList(summaryPage.courses))
}