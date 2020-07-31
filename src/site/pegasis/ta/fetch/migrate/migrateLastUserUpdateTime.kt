package site.pegasis.ta.fetch.migrate

import org.bson.Document
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import site.pegasis.ta.fetch.tools.getMongoClient
import site.pegasis.ta.fetch.tools.toDate
import site.pegasis.ta.fetch.tools.toZonedDateTime
import java.io.File

suspend fun main() {
    val jsonParser = JSONParser()

    val jsonObject = jsonParser.parse(File("data/lastUserUpdateTime.json").readText()) as JSONObject
    val list = jsonObject.map { (jsonNumber, jsonTime) ->
        Document("_id", jsonNumber as String)
            .append("time", (jsonTime as String).toZonedDateTime().toDate())
    }

    val mongoClient = getMongoClient("mongodb://root:password@localhost:27017")
    val db = mongoClient.getDatabase("ta")

    db.getCollection("last-user-update-time").insertMany(list)
}
