package models

import exceptions.UserParseException
import jsonParser
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import writeToFile
import java.io.FileReader


class User() {
    var number: String =""
    var password: String=""
    var receiveNotification: Boolean =true
    var tokenNames = HashMap<String,String>()

    constructor(JSON: JSONObject):this(){
        val userDataJSON = JSON["user"] as JSONObject
        number = userDataJSON["number"] as String
        password = userDataJSON["password"] as String
        receiveNotification = true

        val tokenNamesJSON=JSON["tokennames"] as JSONObject
        tokenNamesJSON.forEach { token, name ->
            tokenNames.put(token as String,name as String)
        }
    }

    fun toJSONObject(): JSONObject {
        val obj = JSONObject()

        val userDataJSON = JSONObject()
        userDataJSON["number"] = number
        userDataJSON["password"] = password
        userDataJSON["receive"] = receiveNotification
        obj["user"] = userDataJSON

        val tokenNamesJSON = JSONObject()
        tokenNames.forEach { token, name ->
            tokenNamesJSON[token]=name
        }
        obj["tokennames"] = tokenNamesJSON

        return obj
    }

    companion object {
        fun fromClient(JSON: JSONObject):User{
            val user=User()

            try{
                val userDataJSON = JSON["user"] as JSONObject
                user.number = userDataJSON["number"] as String
                user.password = userDataJSON["password"] as String
                user.receiveNotification = userDataJSON["receive"] as Boolean

                if (JSON["token"]!=null){
                    user.tokenNames[JSON["token"] as String] = userDataJSON["displayname"] as String
                }
            }catch (e:Exception){
                throw UserParseException()
            }

            return user
        }

        val allUsers = ArrayList<User>()
        val fileName="./data/users.json"

        fun init() {
            FileReader(fileName).use { reader ->
                val users = jsonParser.parse(reader) as JSONArray
                users.forEach { userJSON ->
                    allUsers.add(User(userJSON as JSONObject))
                }
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
                    user.tokenNames.putAll(newUser.tokenNames)
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
                    removedUser.tokenNames.forEach { token, name ->
                        user.tokenNames.remove(token,name)
                    }
                    return@forEach
                }
            }
            save()
        }
    }
}