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

    suspend fun set(number: String, lastUpdateTime: ZonedDateTime? = null, isAutoUpdating: Boolean? = null) {
        val bson = Document()
        if (lastUpdateTime != null) bson.append("last_update_time", lastUpdateTime.toDate())
        if (isAutoUpdating != null) bson.append("is_auto_updating", isAutoUpdating)

        collection.updateOne(eq("_id", number), Document("\$set", bson), enableUpsert)
    }
}
