package site.pegasis.ta.fetch.modes

import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.isQuiet
import site.pegasis.ta.fetch.modes.server.serializers.serialize
import site.pegasis.ta.fetch.webpage.LoginPage

fun getMark(studentNumber: String, password: String, apiLevel: Int, quiet: Boolean) {
    try {
        isQuiet = quiet
        val summaryPage = LoginPage()
            .gotoSummaryPage(studentNumber, password).fillDetails()
        println(summaryPage.courses.serialize(apiLevel).toJSONString())
    } catch (e: LoginException) {
        println("Student number or password error.")
    } catch (e: Throwable) {
        e.printStackTrace()
    }

}