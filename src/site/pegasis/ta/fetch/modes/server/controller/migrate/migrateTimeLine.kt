package site.pegasis.ta.fetch.modes.server.controller.migrate

import io.fluidsonic.mongo.MongoDatabase
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import site.pegasis.ta.fetch.modes.server.storage.CourseListDB
import site.pegasis.ta.fetch.tools.logInfo
import site.pegasis.ta.fetch.tools.toBSON
import java.io.File

suspend fun migrateTimeLine(db: MongoDatabase) {
    val jsonParser = JSONParser()

    val list = File("data/timelines")
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

    db.getCollection(CourseListDB.TIME_LINE_COLLECTION_NAME).insertMany(list)

    logInfo("Migrated time line, ${list.size} items.")
}
