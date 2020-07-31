package site.pegasis.ta.fetch.modes.server.storage

import com.mongodb.client.model.Filters.eq
import io.fluidsonic.mongo.MongoCollection
import io.fluidsonic.mongo.MongoDatabase
import org.bson.Document
import site.pegasis.ta.fetch.tools.enableUpsert
import site.pegasis.ta.fetch.tools.toZonedDateTime
import java.time.ZonedDateTime
import java.util.*

data class UserUpdateStatus(
    val number: String,
    val lastUpdateTime: ZonedDateTime? = null,
    val isAutoUpdating: Boolean = false
) {
    constructor(bson: Document) : this(
        bson["_id"] as String,
        (bson["last_update_time"] as Date?)?.toZonedDateTime(),
        (bson["is_auto_updating"] as Boolean?) ?: false
    )
}

object UserUpdateStatusDB {
    const val collectionName = "user-update-status"

    lateinit var collection: MongoCollection<Document>

    fun init(db: MongoDatabase) {
        collection = db.getCollection(collectionName)
    }

    suspend fun get(number: String): UserUpdateStatus {
        val bson = collection.find(eq("_id", number))
            .limit(1)
            .firstOrNull()

        return bson?.let { UserUpdateStatus(it) } ?: UserUpdateStatus(number)
    }

    suspend fun <R> lockAutoUpdate(number: String, action: suspend () -> R): R {
        collection.updateOne(
            eq("_id", number),
            Document("\$set", Document("is_auto_updating", true)),
            enableUpsert
        )
        val result = action()
        collection.updateOne(
            eq("_id", number),
            Document("\$set",
                Document("is_auto_updating", false)
                    .append("last_update_time", Date())
            ),
            enableUpsert
        )

        return result
    }
}
