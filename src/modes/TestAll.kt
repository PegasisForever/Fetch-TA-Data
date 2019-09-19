package modes

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.FileReader


fun testAll() {
    val jsonParser = JSONParser()

    FileReader("./data/student accounts.json").use { reader ->
        val students = jsonParser.parse(reader) as JSONArray

        for (student in students) if (student is JSONObject) {
            println(student["id"].toString())
            getMarks(student["id"].toString(), student["pwd"].toString())
            println("--------------------------------")
        }
    }


}
