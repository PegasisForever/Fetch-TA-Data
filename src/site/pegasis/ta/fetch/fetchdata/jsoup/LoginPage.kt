package site.pegasis.ta.fetch.fetchdata.jsoup

import site.pegasis.ta.fetch.models.Timing

class LoginPage(private val timing: Timing = Timing()) {
    val session = JsoupSession()

    fun gotoSummaryPage(studentNumber: String, password: String): SummaryPage{
        session.post("https://ta.yrdsb.ca/live/index.php",
            hashMapOf("username" to studentNumber, "password" to password))

        return SummaryPage(session, timing)
    }
}