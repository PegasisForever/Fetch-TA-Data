package site.pegasis.ta.fetch.modes.server.controller

import picocli.CommandLine.Command
import site.pegasis.ta.fetch.modes.server.storage.LastCleanDoneTime
import site.pegasis.ta.fetch.tools.serverBuildNumber
import java.io.File
import java.io.PrintWriter
import java.util.concurrent.Callable

@Command(
    name = "clean",
    description = ["Remove duplicate files in course list history."],
    mixinStandardHelpOptions = true,
    version = ["BN$serverBuildNumber"]
)
class Clean(private val printWriter: PrintWriter) : Callable<Unit> {
    override fun call() {
        val lastCleanDoneMillis = LastCleanDoneTime.getMillis()
        File("data/courselists-history")
            .listFiles { file, _ -> file.isDirectory }!!
            .forEach { directory ->
                var lastFileText: String? = null
                val files = directory.listFiles()!!.sorted()
                val indexOfFirstFileAfterLastClean = files.indexOfFirst { it.nameWithoutExtension.toLong() > lastCleanDoneMillis }
                if (indexOfFirstFileAfterLastClean == -1) return@forEach

                files
                    .subList(indexOfFirstFileAfterLastClean - 1, files.lastIndex+1)
                    .map { file ->
                        val fileText = file.readText()
                        file to if (lastFileText == null || lastFileText != fileText) {
                            lastFileText = fileText
                            false
                        } else {
                            true //should delete
                        }
                    }
                    .forEach { (file, shouldDelete) ->
                        if (shouldDelete) {
                            file.delete()
                        }
                    }
            }

        LastCleanDoneTime.set()
        printWriter.println("Duplicate files in course list history removed.")
    }
}
