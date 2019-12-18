package modes

import exceptions.LoginException
import isQuiet
import modes.server.serializers.serialize
import webpage.LoginPage

fun getMark(studentNumber: String, password: String, apiLevel: Int, quiet: Boolean) {
    try {
        isQuiet = quiet
        val summaryPage = LoginPage().gotoSummaryPage(studentNumber, password).fillDetails()
        println(summaryPage.courses.serialize(apiLevel).toJSONString())
    } catch (e: LoginException) {
        println("Student number or password error.")
    } catch (e: Throwable) {
        e.printStackTrace()
    }

}