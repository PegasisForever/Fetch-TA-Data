import com.gargoylesoftware.htmlunit.html.HtmlPage
import models.Course
import modes.server.serializers.CourseListSerializerV2
import webpage.SummaryPage
import java.io.File
import java.util.logging.Level


fun main() {
    java.util.logging.Logger.getLogger("com.gargoylesoftware").level = Level.ALL


//    val dir = File("./ta-archive/")
//    dir.listFiles()?.forEach { file ->
//        println(file.name)
//        if (file.name.indexOf("summary") != -1) {
//            val summaryPage = getWebClient().getPage<HtmlPage>(file.toURL())
//            println(SummaryPage(summaryPage).courses.toJSONString())
//        } else {
//            val detailPage = getWebClient().getPage<HtmlPage>(file.toURL())
//            println(DetailPage(detailPage, "").details.toJSONString())
//        }
//        println("--------------------------------")
//    }

    val summaryPage = getWebClient()
        .getPage<HtmlPage>(File("ta-archive/1569869106897-350100749-summary.html").toURL())
    println(
        CourseListSerializerV2.serializeCourseList(
            SummaryPage(summaryPage,"1569869106897-350100749-summary.html")
                .fillDetails()
                .courses
        )
    )
}

fun readLocalHtml(fileName:String):ArrayList<Course>{
    val summaryPageHTML = getWebClient()
        .getPage<HtmlPage>(File("ta-archive/$fileName").toURL())
    return SummaryPage(summaryPageHTML,fileName)
        .fillDetails()
        .courses
}