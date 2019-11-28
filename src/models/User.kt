package models

import exceptions.UserParseException
import jsonParser
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import readFile
import writeToFile

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
}

class User() {
    var number = ""
    var password = ""
    var devices = ArrayList<Device>()

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

        val allUsers = ArrayList<User>()
        private const val fileName = "data/users.json"

        fun init() {
            val users = jsonParser.parse(readFile(fileName)) as JSONArray
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
            //todo update password
            get(newUser.number)?.run {
                devices.addAll(newUser.devices)
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