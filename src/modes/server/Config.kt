package modes.server

import jsonParser
import org.json.simple.JSONObject
import readFile

private val configJSON = jsonParser.parse(readFile("data/config.json")) as JSONObject

object Config {
    val notificationEnabled = configJSON["notification"] as Boolean
    val autoUpdateEnabled = configJSON["auto_update"] as Boolean
    val autoUpdateIntervalMinute = (configJSON["auto_update_interval_minute"] as Long).toInt()
}