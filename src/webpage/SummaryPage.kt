package webpage

import LogLevel
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HtmlTable
import find
import findFirst
import log
import models.Course
import models.CourseList
import webClient
import java.io.File
import java.time.LocalDate
import java.time.ZonedDateTime

class SummaryPage(val htmlPage: HtmlPage, val fileName: String? = null, val time: ZonedDateTime? = null) {
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

                val timeText = row.getCell(1).asText()
                val times = find(timeText, "\\d+-\\d+-\\d+")
                if (times?.size == 2) {
                    course.startTime = LocalDate.parse(times[0])
                    course.endTime = LocalDate.parse(times[1])
                }

                if (row.cells.size == 3) {
                    val markText = row.getCell(2).asText()
                    val currentMarkText = find(markText, "(?<=current mark = )[^%]+")?.get(0)
                    if (currentMarkText != null) {
                        course.overallMark = currentMarkText.toDouble()
                    }
                }

                courses.add(course)
            } catch (e: Exception) {
                log(LogLevel.ERROR, "Cannot parse summary row #$i", e)
            }
        }

    }

    fun gotoDetailPage(index: Int): DetailPage {
        val detailHTMLPage = if (fileName == null) {
            htmlPage.getAnchorByHref(
                summaryTable.getRow(index + 1).getCell(2)
                    .getElementsByTagName("a")[0].getAttribute("href")
            ).click()
        } else {
            val file = File("ta-archive/" + fileName.replace("summary", "detail-${courses[index].code}"))
            if (!file.exists()) {
                throw Exception("file ${file.name} not found, summary file name is ${fileName}")
            }
            webClient.getPage<HtmlPage>(file.toURL())
        }

        return DetailPage(detailHTMLPage, courses[index].code,time)
    }

    fun fillDetails(): SummaryPage {
        for (i in 0 until courses.size) {
            val course = courses[i]
            if (course.overallMark != null) {
                val detailPage = gotoDetailPage(i)
                detailPages.add(detailPage)
                course.assignments = detailPage.assignments
                course.weightTable = detailPage.weightTable

            }
        }

        return this
    }
}