package site.pegasis.ta.fetch.modes.server.storage

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.tools.*
import java.net.InetSocketAddress
import java.time.LocalTime
import java.time.ZonedDateTime
import java.net.Proxy as JavaProxy

object Config {
    interface Proxy {
        fun toJavaProxy(): JavaProxy
    }

    data class HttpProxy(val host: String, val port: Int, val user: String? = null, val password: String? = null) : Proxy {
        override fun toJavaProxy(): JavaProxy {
            return JavaProxy(JavaProxy.Type.HTTP, InetSocketAddress(host, port))
        }

        fun hasAuth() = user != null && password != null

        fun authText() = "Basic " + "$user:$password".toBase64()

        companion object {
            fun fromJson(json: JSONObject): HttpProxy {
                return HttpProxy(
                    json["host"] as String,
                    (json["port"] as Long).toInt(),
                    json["user"] as String?,
                    json["password"] as String?,
                )
            }
        }
    }

    object NoProxy : Proxy {
        override fun toJavaProxy() = JavaProxy.NO_PROXY
    }

    var notificationEnabled = false
    var autoUpdateEnabled = false
    var autoUpdateIntervalMinute = 40
    var autoUpdateIntervalExceptions = HashMap<ClosedRange<LocalTime>, Int>()
    var fetchTimeoutSecond = 5L
    var disableCourseRelatedActions = ArrayList<ClosedRange<ZonedDateTime>>()
    var remoteProxies = emptyList<Proxy>()
    var proxies = emptyList<Proxy>()
    var useProxy = false
    var useLocalIP = false

    suspend fun load() {
        val configJSON = jsonParser.parse(readFile("data/config.json")) as JSONObject
        notificationEnabled = configJSON["notification"] as Boolean
        autoUpdateEnabled = configJSON["auto_update"] as Boolean
        autoUpdateIntervalMinute = (configJSON["auto_update_interval_minute"] as Long).toInt()
        fetchTimeoutSecond = configJSON["fetch_timeout_second"] as Long
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
                HttpProxy.fromJson(json)
            }
        proxies = arrayListOf<Proxy>(NoProxy).apply {
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

    fun getRandomProxy(forceRemote: Boolean = false): Proxy {
        return if (forceRemote || (useProxy && !useLocalIP)) {
            // only use remote proxy
            remoteProxies.random()
        } else if (useProxy && useLocalIP) {
            // use remote proxy and local ip
            proxies.random()
        } else {
            // only use local ip
            NoProxy
        }
    }
}
