package site.pegasis.ta.fetch.modes

import site.pegasis.ta.fetch.exceptions.LoginException
import site.pegasis.ta.fetch.fetchdata.fetchUserCourseList
import site.pegasis.ta.fetch.modes.server.serializers.serialize
import site.pegasis.ta.fetch.tools.isQuiet

fun getMark(studentNumber: String, password: String, apiLevel: Int, quiet: Boolean, raw: Boolean) {
    try {
        isQuiet = quiet
        val courseList = fetchUserCourseList(studentNumber, password, raw)
        println(courseList.serialize(apiLevel).toJSONString())
    } catch (e: LoginException) {
        println("Student number or password error.")
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}