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
import site.pegasis.ta.fetch.tools.toBSON
import java.util.*

object CourseListDB {
    const val TIME_LINE_COLLECTION_NAME = "time-lines"
    private lateinit var timeLineCollection: MongoCollection<Document>

    const val COURSE_LIST_COLLECTION_NAME = "course-lists"
    private lateinit var courseListCollection: MongoCollection<Document>

    const val ARCHIVED_COURSE_LIST_COLLECTION_NAME = "archived-course-lists"
    private lateinit var archivedCourseListCollection: MongoCollection<Document>

    const val HISTORY_COURSE_LIST_COLLECTION_NAME = "history-course-lists"
    private lateinit var courseListHistoryCollection: MongoCollection<Document>

    fun init(db: MongoDatabase) {
        courseListCollection = db.getCollection(COURSE_LIST_COLLECTION_NAME)
        courseListHistoryCollection = db.getCollection(HISTORY_COURSE_LIST_COLLECTION_NAME)
        archivedCourseListCollection = db.getCollection(ARCHIVED_COURSE_LIST_COLLECTION_NAME)
        timeLineCollection = db.getCollection(TIME_LINE_COLLECTION_NAME)
    }

    suspend fun save(number: String, courseList: CourseList) {
        val bson = courseList.serialize().toBSON()
        courseListCollection.updateOne(eq("_id", number), Document("\$set", bson), UpdateOptions().apply { upsert(true) })

        val lastCourseList = courseListHistoryCollection
            .aggregate(
                listOf(
                    // match this student number
                    Document(
                        "\$match",
                        Document("_id", number)
                    ),
                    // get last element in the history array
                    Document(
                        "\$project",
                        Document(
                            "last",
                            Document(
                                "\$arrayElemAt",
                                listOf("\$history", -1)
                            )
                        )
                    ),
                    // get data in the last element
                    Document(
                        "\$project",
                        Document("data", "\$last.data")
                    )
                )
            )
            .firstOrNull()

        if (bson["data"] != lastCourseList?.get("data")) {
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
    }

    suspend fun saveArchive(number: String, courseList: CourseList) {
        val bson = courseList.serialize().toBSON()
        archivedCourseListCollection.updateOne(eq("_id", number), Document("\$set", bson), UpdateOptions().apply { upsert(true) })
    }

    suspend fun save(number: String, timeLine: TimeLine) {
        val bson = timeLine.serialize().toBSON()
        timeLineCollection.updateOne(eq("_id", number), Document("\$set", bson), UpdateOptions().apply { upsert(true) })
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
    CourseListDB.save(number, this)
}

suspend fun CourseList.saveArchive(number: String) {
    CourseListDB.saveArchive(number, this)
}

suspend fun TimeLine.save(number: String) {
    CourseListDB.save(number, this)
}
