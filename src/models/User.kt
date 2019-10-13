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

    fun toJSONObject():JSONObject{
        val obj=JSONObject()
        obj["token"]=token
        obj["name"]=name
        obj["language"]=language
        obj["receive"]=receive

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

        (json["devices"] as JSONArray).forEach { deviceJSON->
            devices.add(Device(deviceJSON as JSONObject))
        }
    }

    fun toJSONObject(): JSONObject {
        val obj = JSONObject()

        obj["number"] = number
        obj["password"] = password

        val devicesArray=JSONArray()
        devices.forEach {
            devicesArray.add(it.toJSONObject())
        }
        obj["devices"] = devicesArray

        return obj
    }

    companion object {
        fun fromClient(JSON: JSONObject): User {
            val user = User()

            try {
                val userDataJSON = JSON["user"] as JSONObject
                user.number = userDataJSON["number"] as String
                user.password = userDataJSON["password"] as String

                val device=Device()
                device.receive=userDataJSON["receive"] as Boolean
                device.name=userDataJSON["displayname"] as String
                if (JSON["token"] != null) {
                    device.token=JSON["token"] as String
                }
                if (JSON.containsKey("language")){
                    device.language=JSON["language"] as String
                }
                user.devices.add(device)
            } catch (e: Exception) {
                throw UserParseException()
            }

            return user
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
            allUsers.forEach { user ->
                if (user.number == newUser.number) {
                    user.devices.addAll(newUser.devices)
                    save()
                    return
                }
            }
            allUsers.add(newUser)
            save()
        }

        fun remove(removedUser: User) {
            allUsers.forEach { user ->
                if (user.number == removedUser.number) {
                    removedUser.devices.forEach { deviceRemoved ->
                        user.devices.removeIf {
                            it.token==deviceRemoved.token
                        }
                    }
                    return@forEach
                }
            }
            save()
        }

        fun get(number:String):User?{
            allUsers.forEach {
                if (it.number==number){
                    return it
                }
            }
            return null
        }
    }
}