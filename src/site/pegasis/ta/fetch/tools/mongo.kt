package site.pegasis.ta.fetch.tools

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.model.UpdateOptions
import io.fluidsonic.mongo.MongoClient
import io.fluidsonic.mongo.MongoClients
import org.bson.Document
import org.json.simple.JSONArray
import org.json.simple.JSONObject

fun getMongoClient(url: String): MongoClient {
    return MongoClients.create(MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(url))
        .applyToConnectionPoolSettings {
            it.maxSize(20)
        }
        .build())
}

fun JSONObject.toBSON(): Document {
    val bson = Document()
    forEach { (key, item) ->
        if (key !is String) error("Expected String as key, got $key.")
        bson.append(key, when (item) {
            is JSONArray -> item.toBSON()
            is JSONObject -> item.toBSON()
            else -> item
        })
    }
    return bson
}

fun JSONArray.toBSON(): List<Any?> {
    return map { item ->
        when (item) {
            is JSONArray -> item.toBSON()
            is JSONObject -> item.toBSON()
            else -> item
        }
    }
}

fun Document.BSONToJSON(): JSONObject {
    val obj = JSONObject()
    forEach { key: String, value: Any? ->
        obj[key] = when (value) {
            is List<*> -> value.BSONToJSON()
            is Document -> value.BSONToJSON()
            else -> value
        }
    }
    return obj
}

fun List<*>.BSONToJSON(): JSONArray {
    val array = JSONArray()
    forEach { item ->
        array += when (item) {
            is List<*> -> item.BSONToJSON()
            is Document -> item.BSONToJSON()
            else -> item
        }
    }
    return array
}

val enableUpsert = UpdateOptions().apply { upsert(true) }

const val DATABASE_NAME = "ta"
const val VALUE = "value"
