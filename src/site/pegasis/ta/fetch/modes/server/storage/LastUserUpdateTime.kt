package site.pegasis.ta.fetch.modes.server.storage

import com.mongodb.client.model.Filters.eq
import io.fluidsonic.mongo.MongoCollection
import io.fluidsonic.mongo.MongoDatabase
import org.bson.Document
import site.pegasis.ta.fetch.tools.enableUpsert
import site.pegasis.ta.fetch.tools.toDate
import site.pegasis.ta.fetch.tools.toZonedDateTime
import java.time.ZonedDateTime
import java.util.*

object LastUserUpdateTime {
    const val collectionName = "last-user-update-time"

    lateinit var collection: MongoCollection<Document>

    fun init(db: MongoDatabase) {
        collection = db.getCollection(collectionName)
    }

    suspend fun get(number: String): ZonedDateTime? {
        val time = collection.find(eq("_id", number))
            .limit(1)
            .firstOrNull()
            ?.get("time")
            as Date?

        return time?.toZonedDateTime()
    }

    suspend fun set(number: String, updateTime: ZonedDateTime) {
        collection.updateOne(
            eq("_id", number),
            Document("\$set", Document("time",updateTime.toDate())),
            enableUpsert
        )
    }
}
