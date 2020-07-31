package site.pegasis.ta.fetch.migrate

import org.bson.Document
import org.json.simple.JSONArray
import org.json.simple.parser.JSONParser
import site.pegasis.ta.fetch.tools.getMongoClient
import java.io.File

suspend fun main(){
    val mongoClient = getMongoClient("mongodb://root:password@localhost:27017")
    val db = mongoClient.getDatabase("ta")
    val collection = db.getCollection("data")

    val jsonParser = JSONParser()

    val announcement= File("data/announcement.txt").readText().trim()
    collection.insertOne(Document("_id","announcement").append("data",announcement))

    val calendar=jsonParser.parse(File("data/calendar.json").readText()) as JSONArray
    collection.insertOne(Document("_id","calendar").append("data",calendar))
}
