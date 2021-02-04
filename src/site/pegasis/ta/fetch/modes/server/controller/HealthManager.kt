package site.pegasis.ta.fetch.modes.server.controller

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
            var maxErrorRate = 0.0

            // todo maybe some ways to remove runBlocking
            runBlocking {
                addRequestMutex.withLock {
                    recordsMap.forEach { (path, record) ->
                        val errorCount = errorCountMap[path] ?: 0
                        val errorRate = errorCount.toDouble() / record.size
                        maxErrorRate = maxOf(maxErrorRate, errorRate)
                        if (errorCount > 0) {
                            writeErrLine("$path: error $errorCount/${record.size}")
                        } else {
                            writeStdLine("$path: error $errorCount/${record.size}")
                        }
                    }
                }
            }

            exitCode = if (maxErrorRate > warningErrorRate) 1 else 0
        }
    }

    companion object {
        private val recordsMap = hashMapOf<String, Queue<Record>>()
        private val errorCountMap = hashMapOf<String, Int>()
        private val addRequestMutex = Mutex()
        private const val warningErrorRate = 0.1

        suspend fun addRecord(path: String, status: Int) = addRequestMutex.withLock {
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
            while (records.peek().time < currTime - 5 * 60 * 1000) {
                val removedRecord = records.poll()
                if (removedRecord.isError()) errorCountMap[removedRecord.path] = errorCountMap[removedRecord.path]!! - 1
            }
        }
    }
}
