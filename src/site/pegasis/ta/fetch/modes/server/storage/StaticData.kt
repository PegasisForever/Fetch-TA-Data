package site.pegasis.ta.fetch.modes.server.storage

import com.mongodb.client.model.Filters
import io.fluidsonic.mongo.MongoCollection
import io.fluidsonic.mongo.MongoDatabase
import org.bson.Document
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.tools.BSONToJSON
import site.pegasis.ta.fetch.tools.VALUE

object StaticData {
    const val COLLECTION_NAME = "static-data"
    const val ANNOUNCEMENT_KEY = "announcement"
    const val CALENDAR_V1_KEY = "calendar"
    const val CALENDAR_V2_KEY = "calendar_v2"

    private lateinit var collection: MongoCollection<Document>

    fun init(db: MongoDatabase) {
        collection = db.getCollection(COLLECTION_NAME)
    }

    private suspend fun getValue(id: String): Any? {
        return collection
            .find(Filters.eq("_id", id))
            .limit(1)
            .firstOrNull()
            ?.BSONToJSON()
            ?.get(VALUE)
    }

    suspend fun getAnnouncement(): String {
        val data = getValue(ANNOUNCEMENT_KEY) as String?
        return data ?: ""
    }

    suspend fun getCalendarV1(): String {
        val data = getValue(CALENDAR_V1_KEY) as JSONArray?
        return data?.toJSONString() ?: "[]"
    }

    suspend fun getCalendarV2(): String {
        val data = getValue(CALENDAR_V2_KEY) as JSONObject?
        return data?.toJSONString() ?: ""
    }
}
