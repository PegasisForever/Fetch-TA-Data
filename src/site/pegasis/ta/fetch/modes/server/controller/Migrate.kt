package site.pegasis.ta.fetch.modes.server.controller

import kotlinx.coroutines.runBlocking
import picocli.CommandLine
import picocli.CommandLine.Command
import site.pegasis.ta.fetch.modes.server.controller.migrate.migrate
import site.pegasis.ta.fetch.modes.server.database
import site.pegasis.ta.fetch.tools.serverBuildNumber
import java.io.PrintWriter
import java.util.concurrent.Callable

@Command(
    name = "migrate",
    description = ["Migrate JSON files to mongodb"],
    mixinStandardHelpOptions = true,
    version = ["BN$serverBuildNumber"]
)
class Migrate(private val printWriter: PrintWriter) : Callable<Unit> {
    @CommandLine.Option(
        names = ["--courseList"]
    )
    private var courseList = false

    @CommandLine.Option(
        names = ["--archivedCourseList"]
    )
    private var archivedCourseList = false

    @CommandLine.Option(
        names = ["--historyCourseList"]
    )
    private var historyCourseList = false

    @CommandLine.Option(
        names = ["--staticData"]
    )
    private var staticData = false

    @CommandLine.Option(
        names = ["--timeLine"]
    )
    private var timeLine = false

    @CommandLine.Option(
        names = ["--userUpdateStatus"]
    )
    private var userUpdateStatus = false

    @CommandLine.Option(
        names = ["--user"]
    )
    private var user = false

    override fun call() {
        runBlocking {
            if (courseList ||
                archivedCourseList ||
                historyCourseList ||
                staticData ||
                timeLine ||
                userUpdateStatus ||
                user
            ) {
                migrate(database, courseList, archivedCourseList, historyCourseList, staticData, timeLine, userUpdateStatus, user)
            } else {
                migrate(database)
            }
        }

        printWriter.println("Migrated to MongoDB.")
    }
}
