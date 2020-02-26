package site.pegasis.ta.fetch.webpage

import org.json.simple.JSONObject
import site.pegasis.ta.fetch.*
import java.time.ZonedDateTime

object WebdriverFallbackMap {
    private val fallbackMap = HashMap<String, ZonedDateTime>()
    private const val timeoutDays = 5L

    fun load() {
        fallbackMap.clear()
        val jsonObject = jsonParser.parse(readFile("data/webdriverFallbackMap.json")) as JSONObject
        jsonObject.forEach { jsonNumber, jsonTime ->
            fallbackMap[jsonNumber as String] = (jsonTime as String).toZonedDateTime()
        }
    }

    fun contains(number: String): Boolean {
        return fallbackMap[number]?.isAfter(ZonedDateTime.now()) ?: false
    }

    @Synchronized
    operator fun plusAssign(number: String) {
        fallbackMap[number] = ZonedDateTime.now().plusDays(timeoutDays)

        val obj = JSONObject()
        fallbackMap.forEach { (number, time) ->
            obj[number] = time.toJSONString()
        }
        obj.toJSONString().writeToFile("data/webdriverFallbackMap.json")
    }
}