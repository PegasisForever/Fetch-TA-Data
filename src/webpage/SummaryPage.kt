package webpage

import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HtmlTable
import find
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
            val row = summaryTable.getRow(i)
            val course = Course()

            val classText = row.getCell(0).asText()
            course.code = find(classText, "^[^ ]+")[0]
            course.name = find(classText, "(?<= : )[^\\n]*(?= )", true)[0]
            course.block = find(classText, "(?<=Block: )\\d")[0]
            course.room = find(classText, "(?<=rm\\. )\\d+", true)[0]

            val timeText = row.getCell(1).asText()
            val times = find(timeText, "\\d+-\\d+-\\d+")
            course.startTime = LocalDate.parse(times[0])
            course.endTime = LocalDate.parse(times[1])

            val markText = row.getCell(2).asText()

            if (markText.indexOf("current mark") == -1) {
                course.overallMark = null
            } else {
                course.overallMark = find(markText, "(?<=current mark = )[^%]+")[0].toDouble()
            }

            courses.add(course)
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