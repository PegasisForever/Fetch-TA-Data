package site.pegasis.ta.fetch.tools

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import io.fluidsonic.mongo.MongoClient
import io.fluidsonic.mongo.MongoClients

fun getMongoClient(url: String): MongoClient {
    return MongoClients.create(MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(url))
        .applyToConnectionPoolSettings {
            it.maxSize(20)
        }
        .build())
}
