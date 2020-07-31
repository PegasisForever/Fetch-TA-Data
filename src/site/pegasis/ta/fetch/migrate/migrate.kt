package site.pegasis.ta.fetch.migrate

import io.fluidsonic.mongo.MongoDatabase
import site.pegasis.ta.fetch.modes.server.DB_NAME
import site.pegasis.ta.fetch.tools.getMongoClient
import site.pegasis.ta.fetch.tools.logInfo

suspend fun main() {
    val mongoClient = getMongoClient("mongodb://root:password@localhost:27017")
    val db = mongoClient.getDatabase(DB_NAME)

    migrate(db)
}

suspend fun migrate(db: MongoDatabase,
                    courseList: Boolean = true,
                    archivedCourseList: Boolean = true,
                    historyCourseList: Boolean = true,
                    staticData: Boolean = true,
                    timeLine: Boolean = true,
                    userUpdateStatus: Boolean = true,
                    user: Boolean = true
) {
    if (courseList) migrateCourseLists(db)
    if (archivedCourseList) migrateArchivedCourseLists(db)
    if (historyCourseList) migrateHistoryCourseLists(db)
    if (staticData) migrateStaticData(db)
    if (timeLine) migrateTimeLine(db)
    if (userUpdateStatus) migrateUserUpdateStatus(db)
    if (user) migrateUser(db)

    logInfo("Migration done.")
}
