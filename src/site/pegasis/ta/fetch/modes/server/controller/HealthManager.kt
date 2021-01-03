package site.pegasis.ta.fetch.modes.server.controller

import picocli.CommandLine
import site.pegasis.ta.fetch.tools.serverBuildNumber
import java.util.*
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "health",
    description = ["Get health of the server"],
    mixinStandardHelpOptions = true,
    version = ["BN$serverBuildNumber"]
)
class HealthManager(private val controllerResponse: ControllerResponse) : Callable<Unit> {
    override fun call() {
        with(controllerResponse) {
            var hasError = false
            records.forEach { (path, record) ->
                val errorCount = record.count { it >= 500 }
                if (errorCount > 0) {
                    hasError = true
                    writeErrLine("$path: error $errorCount/${record.size}")
                } else {
                    writeStdLine("$path: error $errorCount/${record.size}")
                }
            }

            exitCode = if (hasError) 1 else 0
        }
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
