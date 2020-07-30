package site.pegasis.ta.fetch.modes.server.storage

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.UpdateOptions
import io.fluidsonic.mongo.MongoCollection
import io.fluidsonic.mongo.MongoDatabase
import org.bson.Document
import site.pegasis.ta.fetch.models.CourseList
import site.pegasis.ta.fetch.models.TimeLine
import site.pegasis.ta.fetch.modes.server.parsers.toCourseList
import site.pegasis.ta.fetch.modes.server.parsers.toTimeLine
import site.pegasis.ta.fetch.modes.server.serializers.serialize
import site.pegasis.ta.fetch.tools.readFile
import site.pegasis.ta.fetch.tools.toBSON
import java.util.*

object PCache {
    private var announcementCache: String? = null

    const val timeLineCollectionName = "timeline"
    lateinit var timeLineCollection: MongoCollection<Document>

    const val courseListCollectionName = "courselists"
    lateinit var courseListCollection: MongoCollection<Document>

    const val archivedCourseListCollectionName = "courselists-archived"
    lateinit var archivedCourseListCollection: MongoCollection<Document>

    const val courseListHistoryCollectionName = "courselists-history"
    lateinit var courseListHistoryCollection: MongoCollection<Document>

    fun init(db: MongoDatabase) {
        courseListCollection = db.getCollection(courseListCollectionName)
        courseListHistoryCollection = db.getCollection(courseListHistoryCollectionName)
        archivedCourseListCollection = db.getCollection(archivedCourseListCollectionName)
        timeLineCollection = db.getCollection(timeLineCollectionName)
    }

    fun clearCache() {
        announcementCache = null
    }

    suspend fun save(number: String, courseList: CourseList) {
        val bson = courseList.serialize().toBSON()
        courseListCollection.updateOne(eq("_id", number), Document("\$set", bson), UpdateOptions().apply { upsert(true) })
        courseListHistoryCollection
            .updateOne(eq("_id", number),
                Document(
                    "\$push",
                    Document("history",
                        bson.append("time", Date())
                    )
                )
                , UpdateOptions().apply { upsert(true) }
            )
    }

    @Synchronized
    suspend fun saveArchive(number: String, courseList: CourseList) {
        val bson = courseList.serialize().toBSON()
        archivedCourseListCollection.updateOne(eq("_id", number), Document("\$set", bson), UpdateOptions().apply { upsert(true) })
    }

    suspend fun save(number: String, timeLine: TimeLine) {
        val bson = timeLine.serialize().toBSON()
        timeLineCollection.updateOne(eq("_id", number), Document("\$set", bson), UpdateOptions().apply { upsert(true) })
    }

    suspend fun getAnnouncement(): String {
        if (announcementCache == null) {
            announcementCache = readFile("data/announcement.txt")
        }
        return announcementCache!!
    }

    suspend fun isExistsBefore(number: String): Boolean {
        return courseListCollection.find(eq("_id", number)).limit(1).firstOrNull() != null
    }

    suspend fun readCourseList(number: String): CourseList {
        val bson = courseListCollection.find(eq("_id", number)).limit(1).firstOrNull()
        return bson?.toCourseList() ?: CourseList()
    }

    suspend fun readArchivedCourseList(number: String): CourseList {
        val bson = archivedCourseListCollection.find(eq("_id", number)).limit(1).firstOrNull()
        return bson?.toCourseList() ?: CourseList()
    }

    suspend fun readTimeLine(number: String): TimeLine {
        val bson = timeLineCollection.find(eq("_id", number)).limit(1).firstOrNull()
        return bson?.toTimeLine() ?: TimeLine()
    }

}

suspend fun CourseList.save(number: String) {
    PCache.save(number, this)
}

suspend fun CourseList.saveArchive(number: String) {
    PCache.saveArchive(number, this)
}

suspend fun TimeLine.save(number: String) {
    PCache.save(number, this)
}
