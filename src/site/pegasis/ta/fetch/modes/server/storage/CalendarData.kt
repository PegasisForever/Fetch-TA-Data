package site.pegasis.ta.fetch.modes.server.storage

import com.mongodb.client.model.Filters.eq
import io.fluidsonic.mongo.MongoCollection
import io.fluidsonic.mongo.MongoDatabase
import org.bson.Document
import org.json.simple.JSONArray
import site.pegasis.ta.fetch.tools.toJSON

object CalendarData {
    const val collectionName = "data"
    const val key = "calendar"

    lateinit var collection: MongoCollection<Document>

    fun init(db: MongoDatabase) {
        collection = db.getCollection(collectionName)
    }

    suspend fun get(): String {
        val data = collection
            .find(eq("_id", key))
            .limit(1)
            .firstOrNull()
            ?.toJSON()
            ?.get("data") as JSONArray?
        return data?.toJSONString() ?: "[]"
    }
}
