package modes

import models.toJSONArray
import webpage.LoginPage


fun getMarks(studentNumber: String, password: String) {
    val summaryPage = LoginPage().gotoSummaryPage(studentNumber, password)
    summaryPage.fillDetails()
    println(summaryPage.courses.toJSONArray().toJSONString())
}