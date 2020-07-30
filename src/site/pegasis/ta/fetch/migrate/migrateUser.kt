package site.pegasis.ta.fetch.migrate

import org.bson.Document
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import site.pegasis.ta.fetch.tools.getMongoClient
import site.pegasis.ta.fetch.tools.readFile
import java.util.concurrent.CopyOnWriteArrayList

suspend fun main() {
    User.load()

    val mongoClient = getMongoClient("mongodb://root:password@localhost:27017")
    val db = mongoClient.getDatabase("ta")
    val collection = db.getCollection("users")
    collection.insertMany(User.allUsers.map { it.toBSONObject() })
}

val jsonParser = JSONParser()

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

    fun toBSONObject(): Document {
        return Document("token", token)
            .append("name", name)
            .append("language", language)
            .append("receive", receive)
    }
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

    fun toBSONObject(): Document {
        return Document("number", number)
            .append("password", password)
            .append("devices", devices.map { it.toBSONObject() })
    }

    companion object {
        val allUsers = CopyOnWriteArrayList<User>()
        private const val fileName = "data/users.json"

        suspend fun load() {
            allUsers.clear()
            val users = jsonParser.parse(
                readFile(fileName)
            ) as JSONArray
            users.forEach { userJSON ->
                allUsers.add(
                    User(
                        userJSON as JSONObject
                    )
                )
            }
        }
    }
}
