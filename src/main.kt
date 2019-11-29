import modes.getMarks
import modes.server.startServer
import java.util.logging.Level

fun main(args: Array<String>) {
    java.util.logging.Logger.getLogger("com.gargoylesoftware").level = Level.OFF

    when (args[0]) {
        "getmark" -> {
            val studentNumber = args[1]
            val password = args[2]
            getMarks(studentNumber, password)
        }
        "server" -> {
            startServer()
        }
        else -> println("Unknown mode")
    }
}

