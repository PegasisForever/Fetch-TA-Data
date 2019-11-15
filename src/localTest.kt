import com.gargoylesoftware.htmlunit.html.HtmlPage
import models.Course
import webpage.SummaryPage
import java.io.File
import java.time.ZonedDateTime
import java.util.logging.Level


fun main() {
    java.util.logging.Logger.getLogger("com.gargoylesoftware").level = Level.ALL


}

fun readLocalHtml(fileName:String,time:ZonedDateTime):ArrayList<Course>{
    val summaryPageHTML = webClient
        .getPage<HtmlPage>(File("ta-archive/$fileName").toURL())
    return SummaryPage(summaryPageHTML,fileName,time)
        .fillDetails()
        .courses
}