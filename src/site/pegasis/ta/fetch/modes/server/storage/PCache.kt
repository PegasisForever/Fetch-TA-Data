package site.pegasis.ta.fetch.modes.server.storage

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.UpdateOptions
import io.fluidsonic.mongo.MongoCollection
import io.fluidsonic.mongo.MongoDatabase
import org.bson.Document
import org.json.simple.JSONObject
import site.pegasis.ta.fetch.models.CourseList
import site.pegasis.ta.fetch.models.TimeLine
import site.pegasis.ta.fetch.modes.server.parsers.toCourseList
import site.pegasis.ta.fetch.modes.server.parsers.toTimeLine
import site.pegasis.ta.fetch.modes.server.serializers.serialize
import site.pegasis.ta.fetch.tools.*
import java.util.*
import kotlin.collections.HashMap

object PCache {
    private val archivedCourseListCacheMap = HashMap<String, CourseList>()
    private var announcementCache: String? = null

    const val timeLineCollectionName = "timeline"
    lateinit var timeLineCollection: MongoCollection<Document>

    const val courseListCollectionName = "courselists"
    lateinit var courseListCollection: MongoCollection<Document>

    const val courseListHistoryCollectionName = "courselists-history"
    lateinit var courseListHistoryCollection: MongoCollection<Document>

    fun init(db: MongoDatabase) {
        courseListCollection = db.getCollection(courseListCollectionName)
        courseListHistoryCollection = db.getCollection(courseListHistoryCollectionName)
        timeLineCollection = db.getCollection(timeLineCollectionName)
    }

    fun clearCache() {
        archivedCourseListCacheMap.clear()
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
        archivedCourseListCacheMap[number] = courseList
        courseList.serialize().toJSONString().writeToFile("data/courselists-archived/$number.json")
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
        return fileExists("data/courselists/$number.json")
    }

    suspend fun readCourseList(number: String): CourseList {
        val bson = courseListCollection.find(eq("_id", number)).limit(1).firstOrNull()
        return bson?.toCourseList() ?: CourseList()
    }

    suspend fun readArchivedCourseList(number: String): CourseList {
        return if (archivedCourseListCacheMap.containsKey(number)) {
            archivedCourseListCacheMap[number]!!
        } else {
            try {
                val text = readFile("data/courselists-archived/$number.json")
                val courseList = (jsonParser.parse(text) as JSONObject).toCourseList()
                archivedCourseListCacheMap[number] = courseList
                courseList
            } catch (e: java.nio.file.NoSuchFileException) {
                CourseList()
            } catch (e: Throwable) {
                log(
                    LogLevel.ERROR,
                    "Error when reading archived course list of $number",
                    e
                )
                CourseList()
            }
        }
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
