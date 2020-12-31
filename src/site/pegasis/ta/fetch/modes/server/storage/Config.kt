package site.pegasis.ta.fetch.modes.server.storage

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.tools.jsonParser
import site.pegasis.ta.fetch.tools.logWarn
import site.pegasis.ta.fetch.tools.readFile
import site.pegasis.ta.fetch.tools.toZonedDateTime
import java.time.LocalTime
import java.time.ZonedDateTime

object Config {
    interface Proxy
    data class RemoteProxy(val host: String, val port: Int) : Proxy
    object LocalProxy : Proxy

    var notificationEnabled = false
    var autoUpdateEnabled = false
    var autoUpdateIntervalMinute = 40
    var autoUpdateIntervalExceptions = HashMap<ClosedRange<LocalTime>, Int>()
    var fetchTimeoutSecond = 100
    var disableCourseRelatedActions = ArrayList<ClosedRange<ZonedDateTime>>()
    var remoteProxies = emptyList<RemoteProxy>()
    var proxies = emptyList<Proxy>()
    var useProxy = false
    var useLocalIP = false

    suspend fun load() {
        val configJSON = jsonParser.parse(readFile("data/config.json")) as JSONObject
        notificationEnabled = configJSON["notification"] as Boolean
        autoUpdateEnabled = configJSON["auto_update"] as Boolean
        autoUpdateIntervalMinute = (configJSON["auto_update_interval_minute"] as Long).toInt()
        fetchTimeoutSecond = (configJSON["fetch_timeout_second"] as Long).toInt()
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
        remoteProxies = (configJSON["proxies"] as JSONArray)
            .filterIsInstance<JSONObject>()
            .map { json ->
                RemoteProxy(json["host"] as String, (json["port"] as Long).toInt())
            }
        proxies = arrayListOf<Proxy>(LocalProxy).apply {
            addAll(remoteProxies)
        }
        useProxy = configJSON["use_proxy"] as Boolean
        useLocalIP = configJSON["use_local_ip"] as Boolean

        if (useProxy && remoteProxies.isEmpty()) {
            logWarn("useProxy is set to true but no proxy is provided, setting useProxy to false")
            useProxy = false
        }
        if (!useProxy && !useLocalIP) {
            logWarn("useProxy and useLocalIP are both false, setting useLocalIP to true")
            useLocalIP = true
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

    fun getRandomProxy(): Proxy {
        return proxies.random()
    }

    fun getRandomRemoteProxy(): RemoteProxy? {
        return if (remoteProxies.isEmpty()){
            null
        }else{
            remoteProxies.random()
        }
    }
}
