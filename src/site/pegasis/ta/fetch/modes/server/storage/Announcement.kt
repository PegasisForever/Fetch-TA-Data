package site.pegasis.ta.fetch.modes.server.storage

import com.mongodb.client.model.Filters
import io.fluidsonic.mongo.MongoCollection
import io.fluidsonic.mongo.MongoDatabase
import org.bson.Document
import site.pegasis.ta.fetch.tools.toJSON

object Announcement {
    const val collectionName = "data"
    const val key = "announcement"

    lateinit var collection: MongoCollection<Document>

    fun init(db: MongoDatabase) {
        collection = db.getCollection(collectionName)
    }

    suspend fun get(): String {
        val data = collection
            .find(Filters.eq("_id", key))
            .limit(1)
            .firstOrNull()
            ?.toJSON()
            ?.get("data") as String?
        return data ?: "[]"
    }
}
