package site.pegasis.ta.fetch.modes.server.storage

import io.fluidsonic.mongo.MongoCollection
import io.fluidsonic.mongo.MongoDatabase
import org.bson.Document
import java.util.*

object FeedbackDB {
    const val collectionName = "feedback"

    lateinit var collection: MongoCollection<Document>

    fun init(db: MongoDatabase) {
        collection = db.getCollection(collectionName)
    }

    suspend fun add(contactInfo: String, feedback: String, platform: String, version: String) {
        val bson = Document("contact_info", contactInfo)
            .append("feedback", feedback)
            .append("platform", platform)
            .append("version", version)
            .append("time", Date())

        collection.insertOne(bson)
    }
}
