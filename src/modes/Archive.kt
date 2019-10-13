package modes

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import webpage.LoginPage
import writeToFile
import java.io.FileReader
import java.util.*

fun archive() {
    val jsonParser = JSONParser()

    FileReader("./data/student accounts.json").use { reader ->
        val students = jsonParser.parse(reader) as JSONArray

        for (student in students) if (student is JSONObject) {
            println(student["id"].toString())
            val time=Date().time
            val summaryPage = LoginPage().gotoSummaryPage(student["id"].toString(), student["pwd"].toString())
            summaryPage.htmlPage.webResponse.contentAsString
                .writeToFile("./ta-archive/${time}-${student["id"]}-summary.html")

            summaryPage.fillDetails()
            for (detailPage in summaryPage.detailPages) {
                detailPage.htmlPage.webResponse.contentAsString
                    .writeToFile("./ta-archive/${time}-${student["id"]}-detail-${detailPage.courseCode}.html")
            }
        }
    }
}