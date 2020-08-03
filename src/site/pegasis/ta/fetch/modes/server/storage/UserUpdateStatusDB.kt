package site.pegasis.ta.fetch.modes.server.storage

import com.mongodb.client.model.Filters.eq
import io.fluidsonic.mongo.MongoCollection
import io.fluidsonic.mongo.MongoDatabase
import org.bson.Document
import site.pegasis.ta.fetch.modes.server.storage.UserUpdateStatusDB.IS_AUTO_UPDATING
import site.pegasis.ta.fetch.modes.server.storage.UserUpdateStatusDB.LAST_UPDATE_TIME
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
        (bson[LAST_UPDATE_TIME] as Date?)?.toZonedDateTime(),
        (bson[IS_AUTO_UPDATING] as Boolean?) ?: false
    )
}

object UserUpdateStatusDB {
    const val COLLECTION_NAME = "user-update-status"
    const val IS_AUTO_UPDATING = "is_auto_updating"
    const val LAST_UPDATE_TIME = "last_update_time"

    lateinit var collection: MongoCollection<Document>

    fun init(db: MongoDatabase) {
        collection = db.getCollection(COLLECTION_NAME)
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
            Document("\$set", Document(IS_AUTO_UPDATING, true)),
            enableUpsert
        )
        val result = action()
        collection.updateOne(
            eq("_id", number),
            Document("\$set",
                Document(IS_AUTO_UPDATING, false)
                    .append(LAST_UPDATE_TIME, Date())
            ),
            enableUpsert
        )

        return result
    }
}
