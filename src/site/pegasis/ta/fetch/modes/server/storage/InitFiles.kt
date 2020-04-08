package site.pegasis.ta.fetch.modes.server.storage

import site.pegasis.ta.fetch.tools.fileExists
import site.pegasis.ta.fetch.tools.writeToFile

suspend fun initFiles() {
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
      "fetch_timeout_second": 100,
      "disable_course_related_actions": [
          {
            "start": "2020-02-28T14:00:00-05:00",
            "end": "2020-02-28T16:00:00-05:00"
          }
      ]
      "proxy": "",
      "proxy_port": 80
    }
    """.trimIndent().writeToFile("data/config.json")
    "[]".writeToFile("data/users.json")
    "{}".writeToFile("data/lastUserUpdateTime.json")
    "".writeToFile("data/announcement.txt")
    "".writeToFile("data/lastUpdateDoneTime.time")
    "".writeToFile("data/lastCleanDoneTime.time")
    """
    [
      {
        "name": {
          "zh": "劳动节",
          "en": "Labour Day"
        },
        "start_date": "2019-9-2",
        "end_date": null
      },
      {
        "name": {
          "zh": "学期开始",
          "en": "First Day of Classes"
        },
        "start_date": "2019-9-3",
        "end_date": null
      },
      {
        "name": {
          "zh": "教研日",
          "en": "PA Day"
        },
        "start_date": "2019-9-23",
        "end_date": null
      },
      {
        "name": {
          "zh": "感恩节",
          "en": "Thanksgiving Day"
        },
        "start_date": "2019-10-14",
        "end_date": null
      },
      {
        "name": {
          "zh": "教研日",
          "en": "PA Day"
        },
        "start_date": "2019-10-21",
        "end_date": null
      },
      {
        "name": {
          "zh": "教研日",
          "en": "PA Day"
        },
        "start_date": "2019-11-15",
        "end_date": null
      },
      {
        "name": {
          "zh": "寒假",
          "en": "Winter Break"
        },
        "start_date": "2019-12-23",
        "end_date": "2020-1-3"
      },
      {
        "name": {
          "zh": "教研日",
          "en": "PA Day"
        },
        "start_date": "2020-1-31",
        "end_date": null
      },
      {
        "name": {
          "zh": "家庭日",
          "en": "Family Day"
        },
        "start_date": "2020-2-17",
        "end_date": null
      },
      {
        "name": {
          "zh": "冬假",
          "en": "Mid-Winter Break"
        },
        "start_date": "2020-3-16",
        "end_date": "2020-3-20"
      },
      {
        "name": {
          "zh": "School Closure (COVID-19)",
          "en": "School Closure (COVID-19)"
        },
        "start_date": "2020-3-21",
        "end_date": "2020-4-5"
      },
      {
        "name": {
          "zh": "圣周五",
          "en": "Good Friday"
        },
        "start_date": "2020-4-10",
        "end_date": null
      },
      {
        "name": {
          "zh": "复活节星期一",
          "en": "Easter Monday"
        },
        "start_date": "2020-4-13",
        "end_date": null
      },
      {
        "name": {
          "zh": "教研日",
          "en": "PA Day"
        },
        "start_date": "2020-5-1",
        "end_date": null
      },
      {
        "name": {
          "zh": "维多利亚日",
          "en": "Victoria Day"
        },
        "start_date": "2020-5-18",
        "end_date": null
      },
      {
        "name": {
          "zh": "学期结束",
          "en": "Last Day of Classes"
        },
        "start_date": "2020-6-24",
        "end_date": null
      },
      {
        "name": {
          "zh": "教研日",
          "en": "PA Day"
        },
        "start_date": "2020-6-25",
        "end_date": null
      },
      {
        "name": {
          "zh": "教研日",
          "en": "PA Day"
        },
        "start_date": "2020-6-26",
        "end_date": null
      }
    ]
    """.trimIndent().writeToFile("data/calendar.json")
}