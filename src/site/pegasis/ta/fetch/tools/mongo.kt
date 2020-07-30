package site.pegasis.ta.fetch.tools

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
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
