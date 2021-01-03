package site.pegasis.ta.fetch.modes.server.controller

import picocli.CommandLine
import site.pegasis.ta.fetch.tools.serverBuildNumber
import java.io.PrintWriter
import java.util.*
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "health",
    description = ["Get health of the server"],
    mixinStandardHelpOptions = true,
    version = ["BN$serverBuildNumber"]
)
class HealthManager(private val printWriter: PrintWriter) : Callable<Unit> {
    override fun call() {
        var hasError = false
        records.forEach { (path, record) ->
            val errorCount = record.count { (it < 200 || it >= 500) }
            if (errorCount > 0) hasError = true
            printWriter.println("$path: error $errorCount/${record.size}")
        }

        printWriter.println(if (hasError) "Unhealthy" else "Healthy")
    }

    companion object {
        private val records = hashMapOf<String, Queue<Int>>()
        private const val maxRecordCount = 30

        fun addRecord(path: String, status: Int) {
            val record = if (path in records.keys) {
                records[path]!!
            } else {
                val newRecord = ArrayDeque<Int>()
                records[path] = newRecord
                newRecord
            }

            record.offer(status)
            while (record.size > maxRecordCount) {
                record.poll()
            }
        }
    }
}
