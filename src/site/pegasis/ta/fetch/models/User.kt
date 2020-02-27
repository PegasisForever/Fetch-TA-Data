package site.pegasis.ta.fetch.models

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.exceptions.UserParseException
import site.pegasis.ta.fetch.tools.jsonParser
import site.pegasis.ta.fetch.tools.readFile
import site.pegasis.ta.fetch.tools.writeToFile
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.collections.HashSet

class Device() {
    var token = ""
    var name = ""
    var language = "en"
    var receive = true

    constructor(json: JSONObject) : this() {
        token = json["token"] as String
        name = json["name"] as String
        language = json["language"] as String
        receive = json["receive"] as Boolean
    }

    fun toJSONObject(): JSONObject {
        val obj = JSONObject()
        obj["token"] = token
        obj["name"] = name
        obj["language"] = language
        obj["receive"] = receive

        return obj
    }

    override fun hashCode(): Int {
        return Objects.hash(token, name, language, receive)
    }

    override fun equals(other: Any?) = other is Device &&
            other.token === token &&
            other.name == name &&
            other.language == language &&
            other.receive == receive
}

class User() {
    var number = ""
    var password = ""
    var devices = HashSet<Device>()

    constructor(json: JSONObject) : this() {
        number = json["number"] as String
        password = json["password"] as String

        (json["devices"] as JSONArray).forEach { deviceJSON ->
            devices.add(Device(deviceJSON as JSONObject))
        }
    }

    fun toJSONObject(): JSONObject {
        val obj = JSONObject()

        obj["number"] = number
        obj["password"] = password

        val devicesArray = JSONArray()
        devices.forEach {
            devicesArray.add(it.toJSONObject())
        }
        obj["devices"] = devicesArray

        return obj
    }

    companion object {
        fun fromClient(json: JSONObject): User {
            try {
                val userDataJson = json["user"] as JSONObject
                val user = User().apply {
                    number = userDataJson["number"] as String
                    password = userDataJson["password"] as String
                }


                val device = Device().apply {
                    receive = userDataJson["receive"] as Boolean
                    name = userDataJson["displayname"] as String
                    language = json["language"] as String
                    if (json["token"] != null) {
                        token = json["token"] as String
                    }
                }

                user.devices.add(device)
                return user
            } catch (e: Exception) {
                throw UserParseException()
            }
        }

        val allUsers = CopyOnWriteArrayList<User>()
        private const val fileName = "data/users.json"

        fun load() {
            allUsers.clear()
            val users = jsonParser.parse(
                readFile(fileName)
            ) as JSONArray
            users.forEach { userJSON ->
                allUsers.add(User(userJSON as JSONObject))
            }
        }

        fun save() {
            val array = JSONArray()
            allUsers.forEach { user ->
                array.add(user.toJSONObject())
            }
            array.toJSONString().writeToFile(fileName)
        }

        fun add(newUser: User) {
            get(newUser.number)?.run {
                newUser.devices.forEach { newDevice ->
                    //If non device of existing user have this token, then add it, else, update language
                    val existingDevice = devices.find { it.token == newDevice.token }
                    if (existingDevice == null) {
                        devices.add(newDevice)
                    } else {
                        existingDevice.language = newDevice.language
                    }
                }
                password = newUser.password
                save()
                return
            }
            allUsers += newUser
            save()
        }

        fun remove(removedUser: User) {
            get(removedUser.number)?.run {
                removedUser.devices.forEach { deviceRemoved ->
                    devices.removeIf {
                        it.token == deviceRemoved.token
                    }
                }
            }
            save()
        }

        fun removeToken(token: String) {
            allUsers.forEach { user ->
                user.devices.removeIf {
                    it.token == token
                }
            }
            save()
        }

        fun get(number: String) = allUsers.find { it.number == number }

        fun validate(number: String, password: String): Boolean {
            val user = get(number)
            return user != null && user.password == password
        }
    }
}