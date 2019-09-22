package webpage

import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HtmlTable
import find
import org.json.simple.JSONArray
import org.json.simple.JSONObject

class SummaryPage(val htmlPage: HtmlPage) {
    val courses = JSONArray()
    val detailPages = ArrayList<DetailPage>()
    private val summaryTable = htmlPage.getElementsByTagName("table")[1] as HtmlTable

    init {
        for (i in 1 until summaryTable.rowCount) {
            val row = summaryTable.getRow(i)
            val course = JSONObject()

            val classText = row.getCell(0).asText()
            course["code"] = find(classText, "^[^ ]+")[0]
            course["name"] = find(classText, "(?<= : )[^\\n]*(?= )")[0]
            course["block"] = find(classText, "(?<=Block: )\\d")[0]
            course["room"] = find(classText, "(?<=rm\\. )\\d+")[0]

            val timeText = row.getCell(1).asText()
            val times = find(timeText, "\\d+-\\d+-\\d+")
            course["start_time"] = times[0]
            course["end_time"] = times[1]

            val markText = row.getCell(2).asText()

            if (markText.indexOf("current mark") == -1) {
                course["overall_mark"] = "N/A"
            } else {
                course["overall_mark"] = find(markText, "(?<=current mark = )[^%]*")[0]
            }

            course["mark_detail"] = JSONObject()

            courses.add(course)
        }

    }

    fun gotoDetailPage(index: Int): DetailPage {
        val pageAnchor = htmlPage.getAnchorByHref(
            summaryTable.getRow(index + 1).getCell(2)
                .getElementsByTagName("a")[0].getAttribute("href")
        )
        return DetailPage(pageAnchor.click(), (courses[index] as JSONObject)["code"] as String)
    }

    fun fillDetails():SummaryPage {
        for (i in 0 until courses.size) {
            val course = courses[i] as JSONObject
            if (course["overall_mark"] != "N/A") {
                val detailPage = gotoDetailPage(i)
                detailPages.add(detailPage)
                course["mark_detail"] = detailPage.details
            }
        }
        return this
    }
}