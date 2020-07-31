package site.pegasis.ta.fetch.migrate

import io.fluidsonic.mongo.MongoDatabase
import org.bson.Document
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import site.pegasis.ta.fetch.modes.server.storage.UserUpdateStatusDB
import site.pegasis.ta.fetch.modes.server.storage.UserUpdateStatusDB.LAST_UPDATE_TIME
import site.pegasis.ta.fetch.tools.logInfo
import site.pegasis.ta.fetch.tools.toDate
import site.pegasis.ta.fetch.tools.toZonedDateTime
import java.io.File

suspend fun migrateUserUpdateStatus(db: MongoDatabase) {
    val jsonParser = JSONParser()

    val jsonObject = jsonParser.parse(File("data/lastUserUpdateTime.json").readText()) as JSONObject
    val list = jsonObject.map { (jsonNumber, jsonTime) ->
        Document("_id", jsonNumber as String)
            .append(LAST_UPDATE_TIME, (jsonTime as String).toZonedDateTime().toDate())
    }

    db.getCollection(UserUpdateStatusDB.COLLECTION_NAME).insertMany(list)
    logInfo("Migrated user update status, ${list.size} items.")
}
