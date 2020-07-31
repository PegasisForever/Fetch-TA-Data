package site.pegasis.ta.fetch.modes.server.storage

import com.mongodb.client.model.Filters
import io.fluidsonic.mongo.MongoCollection
import io.fluidsonic.mongo.MongoDatabase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import org.bson.Document
import site.pegasis.ta.fetch.models.Device
import site.pegasis.ta.fetch.models.User

object UserDB {
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
            collection.updateOne(Filters.eq("number", oldUser.number), Document("\$set", oldUser.toBSONObject()))
        }
    }

    suspend fun remove(removedUser: User) {
        val oldUser = get(removedUser.number)
        if (oldUser != null) {
            oldUser.devices.removeAll(removedUser.devices)
            collection.updateOne(Filters.eq("number", oldUser.number), Document("\$set", oldUser.toBSONObject()))
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
            .find(Filters.eq("_id", number))
            .firstOrNull()
            ?.let { User(it) }
    }

    suspend fun validate(number: String, password: String): Boolean {
        val user = get(number)
        return user?.password == password
    }
}
