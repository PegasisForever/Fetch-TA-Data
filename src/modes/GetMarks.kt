package modes

import webpage.LoginPage


fun getMarks(studentNumber: String, password: String) {
    val summaryPage = LoginPage().gotoSummaryPage(studentNumber, password)
    summaryPage.fillDetails()
    println(summaryPage.courses.toJSONString())
}