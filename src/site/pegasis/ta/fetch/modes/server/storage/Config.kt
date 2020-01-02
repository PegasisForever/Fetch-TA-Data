package site.pegasis.ta.fetch.modes.server.storage

import org.json.simple.JSONObject
import site.pegasis.ta.fetch.jsonParser
import site.pegasis.ta.fetch.modes.server.timeline.startAutoUpdateThread
import site.pegasis.ta.fetch.modes.server.timeline.stopAutoUpdateThread
import site.pegasis.ta.fetch.readFile

object Config {
    var notificationEnabled = false
    var autoUpdateEnabled = false
    var autoUpdateIntervalMinute = 40

    fun load(){
        val configJSON = jsonParser.parse(readFile("data/config.json")) as JSONObject
        notificationEnabled = configJSON["notification"] as Boolean
        autoUpdateEnabled = configJSON["auto_update"] as Boolean
        autoUpdateIntervalMinute = (configJSON["auto_update_interval_minute"] as Long).toInt()
    }
}