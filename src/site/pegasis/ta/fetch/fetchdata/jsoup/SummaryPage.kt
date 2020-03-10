package site.pegasis.ta.fetch.fetchdata.jsoup

import site.pegasis.ta.fetch.models.Course
import site.pegasis.ta.fetch.models.CourseList
import site.pegasis.ta.fetch.models.OverallMark
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.tools.defaultZoneID
import site.pegasis.ta.fetch.tools.find
import site.pegasis.ta.fetch.tools.findFirst
import site.pegasis.ta.fetch.tools.noThrow
import java.time.LocalDate
import java.time.ZonedDateTime

class SummaryPage(private val session: JsoupSession, private val timing: Timing = Timing()) {
    val courses = CourseList()
    private val detailLinkMap = HashMap<Course, String>()

    init {
        val summaryTable = session.currentPage!!.getElementsByTag("table")[1]
        val rows = summaryTable.getElementsByTag("tr")
        for (i in 1 until rows.size) {
            val row = rows[i]
            val cells = row.getElementsByTag("td")
            val course = Course()

            val classText = cells[0].wholeText().replace(Regex("(\\t)+"), " ").replace("\r", "").trim()
            course.code = findFirst(classText, "[A-Z\\d\\.]{6}-[\\d]{2}")
            course.name = findFirst(classText, "(?<= : )[^\\n]+(?= )")
            course.block = findFirst(classText, "(?<=Block: )[^ ]+")
            course.room = findFirst(classText, "(?<=rm\\. ).+$")
            with(course) {
                if (code == null && name == null && block == null && room == null && classText.isNotBlank()) {
                    course.name = classText
                }
            }

            val timeText = cells[1].text()
            val times = find(timeText, "\\d+-\\d+-\\d+")
            if (times?.size == 2) noThrow {
                course.startTime = LocalDate.parse(times[0])
                course.endTime = LocalDate.parse(times[1])
            }

            if (cells.size == 3) {
                val markText = cells[2].text()
                val currentMarkText = find(markText, "(?<=current mark = )[^%]+")?.get(0)
                val levelMarkText = find(markText, "(?<=Level ).*")?.get(0)
                val isClickHere = markText == "Click Here"
                if (currentMarkText != null) {
                    course.overallMark = OverallMark(currentMarkText.toDouble())
                } else if (levelMarkText != null) {
                    course.overallMark = OverallMark(levelMarkText)
                } else if (isClickHere) {
                    course.overallMark = OverallMark()
                }
            }

            courses.add(course)
            if (course.overallMark != null) {
                detailLinkMap[course] = cells[2].getElementsByTag("a")[0].attr("abs:href")
            }
        }
    }

    fun gotoDetailPage(course: Course, time: ZonedDateTime): DetailPage {
        val link = detailLinkMap[course]!!
        session.get(link)
        course.id = findFirst(link, "(?<=subject_id=)\\d+")!!.toInt()
        return DetailPage(session, course.code, time, timing)
    }

    fun fillDetails(doCalculation: Boolean = true): SummaryPage {
        val currentTime = ZonedDateTime.now(defaultZoneID)
        for (i in 0 until courses.size) {
            val course = courses[i]
            if (course.overallMark != null) {
                val detailPage = gotoDetailPage(course, currentTime)
                course.assignments = detailPage.assignments
                course.weightTable = detailPage.weightTable
                if (doCalculation) course.calculate()
            }
        }

        return this
    }
}