package site.pegasis.ta.fetch.modes.server.storage

import site.pegasis.ta.fetch.tools.fileExists
import site.pegasis.ta.fetch.tools.writeToFile

fun initFiles() {
    if (fileExists("data/config.json")) return

    """
    {
      "notification": false,
      "auto_update": false,
      "auto_update_interval_minute": 40,
      "web_driver_path": "web/driver/path/here",
      "fetch_timeout_minute": 100,
      "cp_min_chrome_count": 3,
      "cp_max_chrome_page_count": 100,
      "cp_clean_interval_minute": 10
    }
    """.trimIndent().writeToFile("data/config.json")
    "[]".writeToFile("data/users.json")
    "{}".writeToFile("data/lastUpdateTime.json")
    "{}".writeToFile("data/webdriverFallbackMap.json")
    "".writeToFile("data/announcement.txt")
}