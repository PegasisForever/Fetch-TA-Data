package site.pegasis.ta.fetch.modes.server.storage

import com.mongodb.client.model.Filters
import io.fluidsonic.mongo.MongoCollection
import io.fluidsonic.mongo.MongoDatabase
import org.bson.Document
import org.json.simple.JSONArray
import site.pegasis.ta.fetch.tools.VALUE
import site.pegasis.ta.fetch.tools.toJSON

object StaticData {
    const val COLLECTION_NAME = "static-data"
    const val ANNOUNCEMENT_KEY = "announcement"
    const val CALENDAR_KEY = "calendar"

    private lateinit var collection: MongoCollection<Document>

    fun init(db: MongoDatabase) {
        collection = db.getCollection(COLLECTION_NAME)
    }

    suspend fun getAnnouncement(): String {
        val data = collection
            .find(Filters.eq("_id", ANNOUNCEMENT_KEY))
            .limit(1)
            .firstOrNull()
            ?.toJSON()
            ?.get(VALUE) as String?
        return data ?: ""
    }

    suspend fun getCalendar(): String {
        val data = collection
            .find(Filters.eq("_id", CALENDAR_KEY))
            .limit(1)
            .firstOrNull()
            ?.toJSON()
            ?.get(VALUE) as JSONArray?
        return data?.toJSONString() ?: "[]"
    }
}
