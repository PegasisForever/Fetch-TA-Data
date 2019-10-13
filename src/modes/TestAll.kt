package modes

import modes.server.serializers.CourseListSerializerV2
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import webpage.LoginPage
import writeToFile
import java.io.FileReader


fun testAll() {
    val jsonParser = JSONParser()

    FileReader("./data/student accounts.json").use { reader ->
        val students = jsonParser.parse(reader) as JSONArray

        for (student in students) if (student is JSONObject) {
            println(student["id"].toString())
            val courseList= LoginPage()
                .gotoSummaryPage(student["id"].toString(), student["pwd"].toString())
                .fillDetails().courses

            println("--------------------------------")
        }
    }


}
