import com.gargoylesoftware.htmlunit.html.HtmlPage
import webpage.DetailPage
import webpage.SummaryPage
import java.io.File
import java.util.logging.Level


fun main() {
    java.util.logging.Logger.getLogger("com.gargoylesoftware").level = Level.ALL


    val dir = File("./ta-archive/")
    dir.listFiles()?.forEach { file ->
        println(file.name)
        if (file.name.indexOf("summary") != -1) {
            val summaryPage = getWebClient().getPage<HtmlPage>(file.toURL())
            println(SummaryPage(summaryPage).courses.toJSONString())
        } else {
            val detailPage = getWebClient().getPage<HtmlPage>(file.toURL())
            println(DetailPage(detailPage, "").details.toJSONString())
        }
        println("--------------------------------")
    }
}
