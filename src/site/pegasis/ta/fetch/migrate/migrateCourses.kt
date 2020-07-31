package site.pegasis.ta.fetch.migrate

import io.fluidsonic.mongo.MongoDatabase
import org.bson.Document
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.modes.server.controller.Clean
import site.pegasis.ta.fetch.modes.server.storage.PCache.ARCHIVED_COURSE_LIST_COLLECTION_NAME
import site.pegasis.ta.fetch.modes.server.storage.PCache.COURSE_LIST_COLLECTION_NAME
import site.pegasis.ta.fetch.modes.server.storage.PCache.HISTORY_COURSE_LIST_COLLECTION_NAME
import site.pegasis.ta.fetch.tools.logInfo
import site.pegasis.ta.fetch.tools.toBSON
import java.io.File
import java.io.PrintWriter
import java.time.Instant
import java.util.*

suspend fun migrateCourseLists(db: MongoDatabase) {
    val list = File("data/courselists")
        .walk()
        .filter { it.isFile }
        .map { file ->
            file.nameWithoutExtension to file.readText()
        }
        .map { (number, text) ->
            number to (jsonParser.parse(text) as JSONObject).toBSON()
        }
        .map { (number, bson) ->
            bson.append("_id", number)
        }
        .toList()

    db.getCollection(COURSE_LIST_COLLECTION_NAME).insertMany(list)
    logInfo("Migrated course lists, ${list.size} items.")
}

suspend fun migrateArchivedCourseLists(db: MongoDatabase) {
    val list = File("data/courselists-archived")
        .walk()
        .filter { it.isFile }
        .map { file ->
            file.nameWithoutExtension to file.readText()
        }
        .map { (number, text) ->
            number to (jsonParser.parse(text) as JSONObject).toBSON()
        }
        .map { (number, bson) ->
            bson.append("_id", number)
        }
        .toList()

    db.getCollection(ARCHIVED_COURSE_LIST_COLLECTION_NAME).insertMany(list)
    logInfo("Migrated archived course lists, ${list.size} items.")
}

suspend fun migrateHistoryCourseLists(db: MongoDatabase) {
    Clean(PrintWriter(System.out)).call()
    val list = File("data/courselists-history")
        .walk()
        .filter { it.isDirectory && it.name.toIntOrNull() != null }
        .map { dir ->
            val historyList = dir.walk()
                .filter { it.isFile }
                .map { file -> file.nameWithoutExtension to file.readText() }
                .map { (time, text) ->
                    Date.from(Instant.ofEpochMilli(time.toLong())) to
                        (jsonParser.parse(text) as JSONObject).toBSON()
                }
                .map { (time, bson) ->
                    bson.append("time", time)
                }
                .toList()
            val number = dir.name
            Document("_id", number).append("history", historyList)
        }
        .toList()

    db.getCollection(HISTORY_COURSE_LIST_COLLECTION_NAME).insertMany(list)
    logInfo("Migrated history course lists, ${list.size} items.")
}
