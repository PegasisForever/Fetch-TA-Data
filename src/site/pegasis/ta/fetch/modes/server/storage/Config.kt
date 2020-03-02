package site.pegasis.ta.fetch.modes.server.storage

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.tools.jsonParser
import site.pegasis.ta.fetch.tools.readFile
import site.pegasis.ta.fetch.tools.toZonedDateTime
import java.time.LocalTime
import java.time.ZonedDateTime

object Config {
    var notificationEnabled = false
    var autoUpdateEnabled = false
    var autoUpdateIntervalMinute = 40
    var autoUpdateIntervalExceptions = HashMap<ClosedRange<LocalTime>, Int>()
    var webDriverPath = ""
    var fetchTimeoutSecond = 100
    var chromePoolMinChromeCount = 3
    var chromePoolMaxChromePageCount = 100
    var chromePoolCleanIntervalMinute = 10
    var disableCourseRelatedActions = ArrayList<ClosedRange<ZonedDateTime>>()
    var ignoreLastUpdateDone = false

    fun load() {
        val configJSON = jsonParser.parse(readFile("data/config.json")) as JSONObject
        webDriverPath = configJSON["web_driver_path"] as String
        notificationEnabled = configJSON["notification"] as Boolean
        autoUpdateEnabled = configJSON["auto_update"] as Boolean
        autoUpdateIntervalMinute = (configJSON["auto_update_interval_minute"] as Long).toInt()
        ignoreLastUpdateDone = configJSON["ignore_last_update_done"] as Boolean
        fetchTimeoutSecond = (configJSON["fetch_timeout_second"] as Long).toInt()
        chromePoolMinChromeCount = (configJSON["cp_min_chrome_count"] as Long).toInt()
        chromePoolMaxChromePageCount = (configJSON["cp_max_chrome_page_count"] as Long).toInt()
        chromePoolCleanIntervalMinute = (configJSON["cp_clean_interval_minute"] as Long).toInt()
        (configJSON["disable_course_related_actions"] as JSONArray).forEach { obj ->
            if (obj is JSONObject) {
                disableCourseRelatedActions.add(obj["start"].toString().toZonedDateTime()..obj["end"].toString().toZonedDateTime())
            }
        }
        (configJSON["auto_update_interval_exceptions"] as JSONArray).forEach { obj ->
            if (obj is JSONObject) {
                autoUpdateIntervalExceptions[LocalTime.parse(obj["start"].toString())..LocalTime.parse(obj["end"].toString())] =
                    (obj["interval"] as Long).toInt()
            }
        }
    }

    fun isEnableCourseActions(time: ZonedDateTime = ZonedDateTime.now()): Boolean {
        disableCourseRelatedActions.forEach { timeRange ->
            if (time in timeRange) return false
        }
        return true
    }

    fun getUpdateInterval(time: LocalTime = LocalTime.now()): Int {
        autoUpdateIntervalExceptions.forEach { (range, interval) ->
            if (time in range) return interval
        }
        return autoUpdateIntervalMinute
    }
}