package site.pegasis.ta.fetch.fetchdata.htmlunit

import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HtmlTable
import site.pegasis.ta.fetch.models.Course
import site.pegasis.ta.fetch.models.CourseList
import site.pegasis.ta.fetch.models.OverallMark
import site.pegasis.ta.fetch.models.Timing
import site.pegasis.ta.fetch.tools.*
import java.time.LocalDate
import java.time.ZonedDateTime

class SummaryPage(private val htmlPage: HtmlPage, private val timing: Timing = Timing()) {
    val courses = CourseList()
    private val detailPages = ArrayList<DetailPage>()
    private val detailHrefMap = HashMap<Course, String>()

    init {
        timing("parse summary page") {
            val summaryTable = htmlPage.getElementsByTagName("table")[1] as HtmlTable
            for (i in 1 until summaryTable.rowCount) {
                try {
                    val row = summaryTable.getRow(i)
                    val course = Course()

                    val classText = row.getCell(0).asText().replace("\r", "")
                    course.code = findFirst(classText, "[A-Z\\d\\.]{6}-[\\d]{2}")
                    course.name = findFirst(classText, "(?<= : )[^\\n]+(?= )")
                    course.block = findFirst(classText, "(?<=Block: )[^ ]+")
                    course.room = findFirst(classText, "(?<=rm\\. ).+$")
                    with(course) {
                        if (code == null && name == null && block == null && room == null && classText.isNotBlank()) {
                            course.name = classText
                        }
                    }

                    val timeText = row.getCell(1).asText()
                    val times = find(timeText, "\\d+-\\d+-\\d+")
                    if (times?.size == 2) noThrow {
                        course.startTime = LocalDate.parse(times[0])
                        course.endTime = LocalDate.parse(times[1])
                    }

                    if (row.cells.size == 3) {
                        val markText = row.getCell(2).asText()
                        val currentMarkText = find(
                            markText,
                            "(?<=current mark = )[^%]+"
                        )?.get(0)
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
                        detailHrefMap[course] = row.getCell(2)
                            .getElementsByTagName("a")[0]
                            .getAttribute("href")!!
                    }

                } catch (e: Exception) {
                    log(
                        LogLevel.ERROR,
                        "Cannot parse summary row #$i",
                        e
                    )
                }
            }
        }
    }

    fun gotoDetailPage(course: Course, time: ZonedDateTime): DetailPage {
        val detailHTMLPage = timing("get detail page ${course.code}") {
            htmlPage.getAnchorByHref(detailHrefMap[course]).click<HtmlPage>()
        }
        noThrow {
            course.id = findFirst(detailHTMLPage.baseURL.toExternalForm(), "(?<=subject_id=)\\d+")!!.toInt()
        }

        return DetailPage(detailHTMLPage, course.code, time, timing)
    }

    fun fillDetails(doCalculation: Boolean = true): SummaryPage {
        val currentTime = ZonedDateTime.now(defaultZoneID)
        for (i in 0 until courses.size) {
            val course = courses[i]
            if (course.overallMark != null) {
                val detailPage = gotoDetailPage(course, currentTime)
                detailPages.add(detailPage)
                course.assignments = detailPage.assignments
                course.weightTable = detailPage.weightTable
                if (doCalculation) course.calculate()
            }
        }

        return this
    }
}