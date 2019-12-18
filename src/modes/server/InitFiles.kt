package modes.server

import fileExists
import writeToFile

fun initFiles() {
    if (fileExists("data/config.json")) return

    """
    {
      "notification": false,
      "auto_update": false,
      "auto_update_interval_minute": 40
    }
    """.trimIndent().writeToFile("data/config.json")
    "[]".writeToFile("data/users.json")
}