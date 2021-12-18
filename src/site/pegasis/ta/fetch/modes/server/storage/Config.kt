package site.pegasis.ta.fetch.modes.server.storage

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import site.pegasis.ta.fetch.tools.readFile
import site.pegasis.ta.fetch.tools.toZonedDateTime
import java.time.LocalTime
import java.time.ZonedDateTime

object Config {
//    data class HttpProxy(val host: String, val port: Int, val user: String? = null, val password: String? = null) : Proxy {
//        override fun toJavaProxy(): JavaProxy {
//            return JavaProxy(JavaProxy.Type.HTTP, InetSocketAddress(host, port))
//        }
//
//        fun hasAuth() = user != null && password != null
//
//        fun authText() = "Basic " + "$user:$password".toBase64()
//
//        companion object {
//            fun fromJson(json: JSONObject): HttpProxy {
//                return HttpProxy(
//                    json["host"] as String,
//                    (json["port"] as Long).toInt(),
//                    json["user"] as String?,
//                    json["password"] as String?,
//                )
//            }
//        }
//    }

    var notificationEnabled = false
    var autoUpdateEnabled = false
    var autoUpdateIntervalMinute = 40
    var autoUpdateIntervalExceptions = HashMap<ClosedRange<LocalTime>, Int>()
    var fetchTimeoutSecond = 5L
    var disableCourseRelatedActions = ArrayList<ClosedRange<ZonedDateTime>>()
    var useProxy = false
    var proxyManagerBaseUrl = ""
    var proxyManagerPassword = ""

    suspend fun load() {
        val configJSON = JSONParser().parse(readFile("data/config.json")) as JSONObject
        proxyManagerBaseUrl = configJSON["proxy_manager_url"] as String
        proxyManagerPassword = configJSON["proxy_manager_password"] as String
        notificationEnabled = configJSON["notification"] as Boolean
        autoUpdateEnabled = configJSON["auto_update"] as Boolean
        autoUpdateIntervalMinute = (configJSON["auto_update_interval_minute"] as Long).toInt()
        fetchTimeoutSecond = configJSON["fetch_timeout_second"] as Long
        (configJSON["disable_course_related_actions"] as JSONArray).forEach { obj ->
            if (obj is JSONObject) {
                disableCourseRelatedActions.add(
                    obj["start"].toString().toZonedDateTime()..obj["end"].toString().toZonedDateTime()
                )
            }
        }
        (configJSON["auto_update_interval_exceptions"] as JSONArray).forEach { obj ->
            if (obj is JSONObject) {
                autoUpdateIntervalExceptions[LocalTime.parse(obj["start"].toString())..LocalTime.parse(obj["end"].toString())] =
                    (obj["interval"] as Long).toInt()
            }
        }
        useProxy = configJSON["use_proxy"] as Boolean
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
