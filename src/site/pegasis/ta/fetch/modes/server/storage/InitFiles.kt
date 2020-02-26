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
      "web_driver_path": "web/driver/path/here"
    }
    """.trimIndent().writeToFile("data/config.json")
    "[]".writeToFile("data/users.json")
    "{}".writeToFile("data/lastUpdateTime.json")
    "{}".writeToFile("data/webdriverFallbackMap.json")
    "".writeToFile("data/announcement.txt")
}