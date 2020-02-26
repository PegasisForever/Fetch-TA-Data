package site.pegasis.ta.fetch.modes.server.storage

import org.json.simple.JSONObject
import site.pegasis.ta.fetch.*
import java.time.ZonedDateTime

object LastUpdateTime {
    private val updateTimeMap = HashMap<String, ZonedDateTime>()

    fun load() {
        updateTimeMap.clear()
        val jsonObject = jsonParser.parse(readFile("data/lastUpdateTime.json")) as JSONObject
        jsonObject.forEach { jsonNumber, jsonTime ->
            this[jsonNumber as String] = (jsonTime as String).toZonedDateTime()
        }
    }

    operator fun get(number: String): ZonedDateTime? {
        return if (updateTimeMap.containsKey(number)) updateTimeMap[number] else null
    }

    @Synchronized
    operator fun set(number: String, updateTime: ZonedDateTime) {
        updateTimeMap[number] = updateTime

        val obj = JSONObject()
        updateTimeMap.forEach { (number, time) ->
            obj[number] = time.toJSONString()
        }
        obj.toJSONString().writeToFile("data/lastUpdateTime.json")
    }
}