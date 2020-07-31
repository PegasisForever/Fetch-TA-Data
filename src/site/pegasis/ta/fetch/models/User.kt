package site.pegasis.ta.fetch.models

import com.mongodb.client.model.Filters.eq
import io.fluidsonic.mongo.MongoCollection
import io.fluidsonic.mongo.MongoDatabase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import org.bson.Document
import org.json.simple.JSONObject
import java.util.*
import kotlin.collections.HashSet

class Device() {
    var token = ""
    var name = ""
    var language = "en"
    var receive = true

    constructor(bson: Document) : this() {
        token = bson["token"] as String
        name = bson["name"] as String
        language = bson["language"] as String
        receive = bson["receive"] as Boolean
    }

    fun toBSONObject(): Document {
        return Document("token", token)
            .append("name", name)
            .append("language", language)
            .append("receive", receive)
    }

    override fun hashCode(): Int {
        return Objects.hash(token, name, language, receive)
    }

    override fun equals(other: Any?) = other is Device &&
        other.token == token &&
        other.name == name &&
        other.language == language &&
        other.receive == receive
}

class User() {
    var number = ""
    var password = ""
    var devices = HashSet<Device>()

    constructor(bson: Document) : this() {
        number = bson["_id"] as String
        password = bson["password"] as String

        (bson["devices"] as Iterable<*>).forEach { deviceBSON ->
            devices.add(Device(deviceBSON as Document))
        }
    }

    fun toBSONObject(): Document {
        return Document("_id", number)
            .append("password", password)
            .append("devices", devices.map { it.toBSONObject() })
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
                error("")
            }
        }

        const val COLLECTION_NAME = "users"
        lateinit var collection: MongoCollection<Document>

        fun init(db: MongoDatabase) {
            collection = db.getCollection(COLLECTION_NAME)
        }

        suspend inline fun forEach(crossinline action: suspend (User) -> Unit) {
            collection.find().map { User(it) }.collect(action)
        }

        suspend fun add(newUser: User) {
            val oldUser = get(newUser.number)
            if (oldUser == null) {
                collection.insertOne(newUser.toBSONObject())
            } else {
                newUser.devices.forEach { newDevice ->
                    val existingDevice = oldUser.devices.find { it.token == newDevice.token }
                    if (existingDevice == null) {
                        oldUser.devices.add(newDevice)
                    } else {
                        existingDevice.apply {
                            language = newDevice.language
                            receive = newDevice.receive
                            name = newDevice.name
                        }
                    }
                }
                collection.updateOne(eq("number", oldUser.number), Document("\$set", oldUser.toBSONObject()))
            }
        }

        suspend fun remove(removedUser: User) {
            val oldUser = get(removedUser.number)
            if (oldUser != null) {
                oldUser.devices.removeAll(removedUser.devices)
                collection.updateOne(eq("number", oldUser.number), Document("\$set", oldUser.toBSONObject()))
            }
        }

        suspend fun removeDevice(number: String, device: Device) {
            remove(User().apply {
                this.number = number
                devices = hashSetOf(device)
            })
        }

        suspend fun get(number: String): User? {
            return collection
                .find(eq("_id", number))
                .firstOrNull()
                ?.let { User(it) }
        }

        suspend fun validate(number: String, password: String): Boolean {
            val user = get(number)
            return user?.password == password
        }
    }
}
