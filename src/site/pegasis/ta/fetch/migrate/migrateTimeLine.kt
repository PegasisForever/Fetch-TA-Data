package site.pegasis.ta.fetch.migrate

import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import site.pegasis.ta.fetch.modes.server.storage.PCache
import site.pegasis.ta.fetch.tools.getMongoClient
import site.pegasis.ta.fetch.tools.toBSON
import java.io.File

suspend fun main() {
    val mongoClient = getMongoClient("mongodb://root:password@localhost:27017")
    val db = mongoClient.getDatabase("ta")

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

    db.getCollection(PCache.TIME_LINE_COLLECTION_NAME).insertMany(list)
}
