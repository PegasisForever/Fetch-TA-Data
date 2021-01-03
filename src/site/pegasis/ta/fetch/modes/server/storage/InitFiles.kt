package site.pegasis.ta.fetch.modes.server.storage

import io.fluidsonic.mongo.MongoDatabase
import kotlinx.coroutines.flow.toList
import org.bson.Document
import org.json.simple.JSONArray
import org.json.simple.parser.JSONParser
import site.pegasis.ta.fetch.tools.*

suspend fun initFiles() {
    if (!fileExists("data/config.json")){
        logInfo("Creating config files.....")
        """{
  "notification": false,
  "auto_update": false,
  "auto_update_interval_minute": 120,
  "auto_update_interval_exceptions": [
    {
      "start": "00:00:00",
      "end": "06:00:00",
      "interval": 240
    },
    {
      "start": "18:00:00",
      "end": "23:59:59",
      "interval": 240
    }
  ],
  "ignore_last_update_done": false,
  "fetch_timeout_second": 5,
  "disable_course_related_actions": [
    {
      "start": "2020-02-28T14:00:00-05:00",
      "end": "2020-02-28T16:00:00-05:00"
    }
  ],
  "proxies": [
  ],
  "use_proxy": false,
  "use_local_ip": true
}
""".writeToFile("data/config.json")
    }
}

suspend fun initMongoDB(db: MongoDatabase) {
    if (db.listCollectionNames().toList().isEmpty()) {
        logInfo("Creating database.....")
        with(db.getCollection(StaticData.COLLECTION_NAME)) {
            insertOne(Document("_id", StaticData.ANNOUNCEMENT_KEY).append(VALUE, ""))
            val calendarData = (JSONParser().parse("""[
    {
        "end_date": null,
        "name": {
            "en": "Labour Day",
            "zh": "劳动节"
        },
        "start_date": "2019-9-2"
    },
    {
        "end_date": null,
        "name": {
            "en": "First Day of Classes",
            "zh": "学期开始"
        },
        "start_date": "2019-9-3"
    },
    {
        "end_date": null,
        "name": {
            "en": "PA Day",
            "zh": "教研日"
        },
        "start_date": "2019-9-23"
    },
    {
        "end_date": null,
        "name": {
            "en": "Thanksgiving Day",
            "zh": "感恩节"
        },
        "start_date": "2019-10-14"
    },
    {
        "end_date": null,
        "name": {
            "en": "PA Day",
            "zh": "教研日"
        },
        "start_date": "2019-10-21"
    },
    {
        "end_date": null,
        "name": {
            "en": "PA Day",
            "zh": "教研日"
        },
        "start_date": "2019-11-15"
    },
    {
        "end_date": "2020-1-3",
        "name": {
            "en": "Winter Break",
            "zh": "寒假"
        },
        "start_date": "2019-12-23"
    },
    {
        "end_date": null,
        "name": {
            "en": "PA Day",
            "zh": "教研日"
        },
        "start_date": "2020-1-31"
    },
    {
        "end_date": null,
        "name": {
            "en": "Family Day",
            "zh": "家庭日"
        },
        "start_date": "2020-2-17"
    },
    {
        "end_date": "2020-3-20",
        "name": {
            "en": "Mid-Winter Break",
            "zh": "冬假"
        },
        "start_date": "2020-3-16"
    },
    {
        "end_date": "2020-4-5",
        "name": {
            "en": "School Closure (COVID-19)",
            "zh": "School Closure (COVID-19)"
        },
        "start_date": "2020-3-21"
    },
    {
        "end_date": null,
        "name": {
            "en": "Good Friday",
            "zh": "圣周五"
        },
        "start_date": "2020-4-10"
    },
    {
        "end_date": null,
        "name": {
            "en": "Easter Monday",
            "zh": "复活节星期一"
        },
        "start_date": "2020-4-13"
    },
    {
        "end_date": null,
        "name": {
            "en": "PA Day",
            "zh": "教研日"
        },
        "start_date": "2020-5-1"
    },
    {
        "end_date": null,
        "name": {
            "en": "Victoria Day",
            "zh": "维多利亚日"
        },
        "start_date": "2020-5-18"
    },
    {
        "end_date": null,
        "name": {
            "en": "Last Day of Classes",
            "zh": "学期结束"
        },
        "start_date": "2020-6-24"
    },
    {
        "end_date": null,
        "name": {
            "en": "PA Day",
            "zh": "教研日"
        },
        "start_date": "2020-6-25"
    },
    {
        "end_date": null,
        "name": {
            "en": "PA Day",
            "zh": "教研日"
        },
        "start_date": "2020-6-26"
    },
    {
        "end_date": null,
        "name": {
            "en": "Labour Day",
            "zh": "劳动节"
        },
        "start_date": "2020-9-7"
    },
    {
        "end_date": null,
        "name": {
            "en": "First Day of Classes",
            "zh": "学期开始"
        },
        "start_date": "2020-9-8"
    },
    {
        "end_date": null,
        "name": {
            "en": "Thanksgiving Day",
            "zh": "感恩节"
        },
        "start_date": "2020-10-12"
    },
    {
        "end_date": null,
        "name": {
            "en": "PA Day",
            "zh": "教研日"
        },
        "start_date": "2019-11-13"
    },
    {
        "end_date": "2021-1-1",
        "name": {
            "en": "Winter Break",
            "zh": "寒假"
        },
        "start_date": "2020-12-21"
    },
    {
        "end_date": null,
        "name": {
            "en": "PA Day",
            "zh": "教研日"
        },
        "start_date": "2021-2-1"
    },
    {
        "end_date": null,
        "name": {
            "en": "Family Day",
            "zh": "家庭日"
        },
        "start_date": "2021-2-15"
    },
    {
        "end_date": "2021-3-19",
        "name": {
            "en": "Mid-Winter Break",
            "zh": "冬假"
        },
        "start_date": "2021-3-15"
    },
    {
        "end_date": null,
        "name": {
            "en": "Good Friday",
            "zh": "圣周五"
        },
        "start_date": "2021-4-2"
    },
    {
        "end_date": null,
        "name": {
            "en": "Easter Monday",
            "zh": "复活节星期一"
        },
        "start_date": "2021-4-5"
    },
    {
        "end_date": null,
        "name": {
            "en": "PA Day",
            "zh": "教研日"
        },
        "start_date": "2021-5-7"
    },
    {
        "end_date": null,
        "name": {
            "en": "Victoria Day",
            "zh": "维多利亚日"
        },
        "start_date": "2021-5-24"
    },
    {
        "end_date": null,
        "name": {
            "en": "Last Day of Classes",
            "zh": "学期结束"
        },
        "start_date": "2021-6-28"
    },
    {
        "end_date": null,
        "name": {
            "en": "PA Day",
            "zh": "教研日"
        },
        "start_date": "2021-6-29"
    }
]""") as JSONArray).toBSON()
            insertOne(Document("_id", StaticData.CALENDAR_KEY).append(VALUE, calendarData))
        }
        db.createCollection(CourseListDB.ARCHIVED_COURSE_LIST_COLLECTION_NAME)
        db.createCollection(CourseListDB.COURSE_LIST_COLLECTION_NAME)
        db.createCollection(CourseListDB.HISTORY_COURSE_LIST_COLLECTION_NAME)
        db.createCollection(CourseListDB.TIME_LINE_COLLECTION_NAME)
        db.createCollection(UserUpdateStatusDB.COLLECTION_NAME)
        db.createCollection(UserDB.COLLECTION_NAME)
    }
}
