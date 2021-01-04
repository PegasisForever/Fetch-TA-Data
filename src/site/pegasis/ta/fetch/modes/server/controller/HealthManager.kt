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
    private data class Record(val path: String, val statusCode: Int) {
        val time = System.currentTimeMillis()

        fun isError() = statusCode >= 500
    }

    override fun call() {
        with(controllerResponse) {
            var hasError = false
            recordsMap.forEach { (path, record) ->
                val errorCount = errorCountMap[path] ?: 0
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
        private val recordsMap = hashMapOf<String, Queue<Record>>()
        private val errorCountMap = hashMapOf<String, Int>()
        private const val maxRecordCount = 30

        fun addRecord(path: String, status: Int) {
            val records = if (path in recordsMap.keys) {
                recordsMap[path]!!
            } else {
                val newRecord = ArrayDeque<Record>()
                recordsMap[path] = newRecord
                newRecord
            }

            val record = Record(path, status)
            records.offer(record)
            if (record.isError()) errorCountMap[path] = (errorCountMap[path] ?: 0) + 1

            val currTime = System.currentTimeMillis()
            while (records.size > maxRecordCount && records.peek().time < currTime - 5 * 60 * 1000) {
                val removedRecord = records.poll()
                if (removedRecord.isError()) errorCountMap[removedRecord.path] = errorCountMap[removedRecord.path]!! - 1
            }
        }
    }
}
