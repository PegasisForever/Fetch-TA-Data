package site.pegasis.ta.fetch.migrate

import org.bson.Document
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.modes.server.controller.Clean
import site.pegasis.ta.fetch.tools.getMongoClient
import site.pegasis.ta.fetch.tools.toBSON
import java.io.File
import java.io.PrintWriter
import java.time.Instant
import java.util.*

suspend fun main() {
    val mongoClient = getMongoClient("mongodb://root:password@localhost:27017")
    val db = mongoClient.getDatabase("ta")

    var list = File("data/courselists")
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

    db.getCollection("courselists").insertMany(list)

    ////
    list = File("data/courselists-archived")
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
    db.getCollection("courselists-archived").insertMany(list)

    ////
    Clean(PrintWriter(System.out)).call()
    list = File("data/courselists-history")
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
    db.getCollection("courselists-history").insertMany(list)
}
