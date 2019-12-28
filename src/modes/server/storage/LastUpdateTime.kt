package modes.server.storage

import jsonParser
import org.json.simple.JSONObject
import readFile
import toJSONString
import toZonedDateTime
import writeToFile
import java.time.ZonedDateTime

object LastUpdateTime {
    private val updateTimeMap = HashMap<String, ZonedDateTime>().apply {
        val jsonObject = jsonParser.parse(readFile("data/lastUpdateTime.json")) as JSONObject
        jsonObject.forEach { jsonNumber, jsonTime ->
            this[jsonNumber as String] = (jsonTime as String).toZonedDateTime()
        }
    }

    operator fun get(number: String): ZonedDateTime? {
        return updateTimeMap.getOrDefault(number, null)
    }

    @Synchronized
    operator fun set(number: String, updateTime: ZonedDateTime) {
        updateTimeMap[number] = updateTime

        val obj = JSONObject()
        updateTimeMap.forEach { n, t ->
            obj[n] = t.toJSONString()
        }
        obj.toJSONString().writeToFile("data/lastUpdateTime.json")
    }
}