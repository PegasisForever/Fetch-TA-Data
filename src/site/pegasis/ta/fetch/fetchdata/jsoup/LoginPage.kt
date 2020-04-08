package site.pegasis.ta.fetch.fetchdata.jsoup

import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.fetchdata.NetworkRequester
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.tools.findFirst

class LoginPage(requester: NetworkRequester, private val timing: Timing = Timing()) {
    private val session = JsoupSession(requester)

    suspend fun gotoSummaryPage(studentNumber: String, password: String): SummaryPage {
        timing.suspend("load summary page") {
            session.post("https://ta.yrdsb.ca/live/index.php",
                hashMapOf("username" to studentNumber, "password" to password))
        }

        if (session.currentPage!!.title() != "Student Reports") {
            val errorCode = findFirst(session.currentPage!!.location(), "\\d")?.toInt()
            throw LoginException(errorCode)
        }

        return SummaryPage(session, timing)
    }
}