package site.pegasis.ta.fetch.modes.server.storage

import org.json.simple.JSONObject
import site.pegasis.ta.fetch.tools.*
import java.time.ZonedDateTime

object LastUserUpdateTime {
    private val updateTimeMap = HashMap<String, ZonedDateTime>()

    suspend fun load() {
        updateTimeMap.clear()
        val jsonObject = jsonParser.parse(readFile("data/lastUserUpdateTime.json")) as JSONObject
        jsonObject.forEach { (jsonNumber, jsonTime) ->
            this.set(jsonNumber as String, (jsonTime as String).toZonedDateTime())
        }
    }

    operator fun get(number: String): ZonedDateTime? {
        return if (updateTimeMap.containsKey(number)) updateTimeMap[number] else null
    }

    @Synchronized
    suspend fun set(number: String, updateTime: ZonedDateTime) {
        updateTimeMap[number] = updateTime

        val obj = JSONObject()
        updateTimeMap.forEach { (number, time) ->
            obj[number] = time.toJSONString()
        }
        obj.toJSONString().writeToFile("data/lastUpdateTime.json")
    }
}