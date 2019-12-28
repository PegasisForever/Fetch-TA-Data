package modes.server.storage

import jsonParser
import org.json.simple.JSONArray

val CalendarData = """
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
"""
    .let { jsonParser.parse(it) as JSONArray }
    .toJSONString() //remove all new lines and spaces