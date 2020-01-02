package site.pegasis.ta.fetch.modes.server.storage

import site.pegasis.ta.fetch.fileExists
import site.pegasis.ta.fetch.writeToFile

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
    "{}".writeToFile("data/lastUpdateTime.json")
    "".writeToFile("data/announcement.txt")
}