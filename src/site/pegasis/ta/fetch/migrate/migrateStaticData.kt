package site.pegasis.ta.fetch.migrate

import io.fluidsonic.mongo.MongoDatabase
import org.bson.Document
import org.json.simple.JSONArray
import org.json.simple.parser.JSONParser
import site.pegasis.ta.fetch.modes.server.storage.StaticData
import site.pegasis.ta.fetch.tools.logInfo
import java.io.File

suspend fun migrateStaticData(db: MongoDatabase) {
    val collection = db.getCollection(StaticData.COLLECTION_NAME)

    val jsonParser = JSONParser()

    val announcement = File("data/announcement.txt").readText().trim()
    collection.insertOne(Document("_id", StaticData.ANNOUNCEMENT_KEY).append("data", announcement))

    val calendar = jsonParser.parse(File("data/calendar.json").readText()) as JSONArray
    collection.insertOne(Document("_id", StaticData.CALENDAR_KEY).append("data", calendar))

    logInfo("Migrated static data.")
}
