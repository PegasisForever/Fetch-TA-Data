package modes.server.updater

import fileExists
import fill
import jsonParser
import org.json.simple.JSONObject
import java.io.FileReader

class TimelineManager {
    companion object {
        private const val lastDataFilePath = "./data/%s-lastdata.json"
        fun getUpdate(studentNumber: String, data: JSONObject) {
            var lastData = JSONObject()
            var firstTime = true
            if (fileExists(lastDataFilePath.fill(studentNumber))) FileReader(lastDataFilePath.fill(studentNumber)).use { reader ->
                lastData = jsonParser.parse(reader) as JSONObject
                firstTime = false
            }




        }
    }
}