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
      "auto_update_interval_exceptions": [
          {
            "start": "00:00:00",
            "end": "06:00:00",
            "interval": 120
          },
          {
            "start": "18:00:00",
            "end": "23:59:59",
            "interval": 120
          }
        ],
      "ignore_last_update_done": false,
      "web_driver_path": "web/driver/path/here",
      "fetch_timeout_second": 100,
      "cp_min_chrome_count": 3,
      "cp_max_chrome_page_count": 100,
      "cp_clean_interval_minute": 10,
      "disable_course_related_actions": [
          {
            "start": "2020-02-28T14:00:00-05:00",
            "end": "2020-02-28T16:00:00-05:00"
          }
      ]
    }
    """.trimIndent().writeToFile("data/config.json")
    "[]".writeToFile("data/users.json")
    "{}".writeToFile("data/lastUserUpdateTime.json")
    "{}".writeToFile("data/webdriverFallbackMap.json")
    "".writeToFile("data/announcement.txt")
    "".writeToFile("data/lastUpdateDoneTime.time")
    "".writeToFile("data/lastCleanDoneTime.time")
}