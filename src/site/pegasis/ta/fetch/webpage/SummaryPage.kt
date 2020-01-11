package site.pegasis.ta.fetch.webpage

import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HtmlTable
import site.pegasis.ta.fetch.*
import site.pegasis.ta.fetch.models.Course
import site.pegasis.ta.fetch.models.CourseList
import site.pegasis.ta.fetch.models.OverallMark
import java.time.LocalDate
import java.time.ZonedDateTime

class SummaryPage(val htmlPage: HtmlPage) {
    val courses = CourseList()
    val detailPages = ArrayList<DetailPage>()
    private val summaryTable = htmlPage.getElementsByTagName("table")[1] as HtmlTable

    init {
        for (i in 1 until summaryTable.rowCount) {
            try {
                val row = summaryTable.getRow(i)
                val course = Course()

                val classText = row.getCell(0).asText()
                course.code = findFirst(classText, "[A-Z\\d]{6}-[\\d]{2}")
                course.name = findFirst(classText, "(?<= : )[^\\n]+(?= )")
                course.block = findFirst(classText, "(?<=Block: )\\d")
                course.room = findFirst(classText, "(?<=rm\\. )\\d+")
                with(course) {
                    if (code == null && name == null && block == null && room == null && classText.isNotBlank()) {
                        course.name = classText
                    }
                }

                val timeText = row.getCell(1).asText()
                val times = find(timeText, "\\d+-\\d+-\\d+")
                if (times?.size == 2) {
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
                    if (currentMarkText != null) {
                        course.overallMark = OverallMark(currentMarkText.toDouble())
                    } else if (levelMarkText != null) {
                        course.overallMark = OverallMark(levelMarkText)
                    }
                }

                courses.add(course)
            } catch (e: Exception) {
                log(
                    LogLevel.ERROR,
                    "Cannot parse summary row #$i",
                    e
                )
            }
        }

    }

    fun gotoDetailPage(index: Int, time: ZonedDateTime): DetailPage {
        val detailHTMLPage = htmlPage.getAnchorByHref(
            summaryTable.getRow(index + 1).getCell(2)
                .getElementsByTagName("a")[0].getAttribute("href")
        ).click<HtmlPage>()

        return DetailPage(detailHTMLPage, courses[index].code, time)
    }

    fun fillDetails(): SummaryPage {
        val currentTime = ZonedDateTime.now(torontoZoneID)
        for (i in 0 until courses.size) {
            val course = courses[i]
            if (course.overallMark != null) {
                val detailPage = gotoDetailPage(i, currentTime)
                detailPages.add(detailPage)
                course.assignments = detailPage.assignments
                course.weightTable = detailPage.weightTable
                course.calculate()
            }
        }

        return this
    }
}