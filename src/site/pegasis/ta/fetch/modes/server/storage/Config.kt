package site.pegasis.ta.fetch.modes.server.storage

import org.json.simple.JSONObject
import site.pegasis.ta.fetch.tools.jsonParser
import site.pegasis.ta.fetch.tools.readFile

object Config {
    var notificationEnabled = false
    var autoUpdateEnabled = false
    var autoUpdateIntervalMinute = 40
    var webDriverPath = ""

    fun load() {
        val configJSON = jsonParser.parse(readFile("data/config.json")) as JSONObject
        webDriverPath = configJSON["web_driver_path"] as String
        notificationEnabled = configJSON["notification"] as Boolean
        autoUpdateEnabled = configJSON["auto_update"] as Boolean
        autoUpdateIntervalMinute = (configJSON["auto_update_interval_minute"] as Long).toInt()
    }
}