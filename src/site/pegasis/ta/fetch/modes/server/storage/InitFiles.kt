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
      "ta_certificate_path": "data/ta.yrdsb.ca.cer",
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
    """
        -----BEGIN CERTIFICATE-----
        MIIG1TCCBb2gAwIBAgIQBS36w/OdwrHm/HPWmO/wlTANBgkqhkiG9w0BAQsFADBw
        MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMRkwFwYDVQQLExB3
        d3cuZGlnaWNlcnQuY29tMS8wLQYDVQQDEyZEaWdpQ2VydCBTSEEyIEhpZ2ggQXNz
        dXJhbmNlIFNlcnZlciBDQTAeFw0xODEwMTgwMDAwMDBaFw0yMDEwMjgxMjAwMDBa
        MHIxCzAJBgNVBAYTAkNBMRAwDgYDVQQIEwdPbnRhcmlvMQ8wDQYDVQQHEwZBdXJv
        cmExKjAoBgNVBAoTIVlvcmsgUmVnaW9uIERpc3RyaWN0IFNjaG9vbCBCb2FyZDEU
        MBIGA1UEAxMLdGEueXJkc2IuY2EwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEK
        AoIBAQDTKU6EYMn6Iq9f/dpPfKw3SuGbTgQqNXpKuF9UA0SNHLyxmMIF9Yei8v2F
        1Vk+k6WaP1yT8lxR/Iim/XqldpfrDymABNmWhGXl37V2DZM5GKSb47g/+FQdclXu
        PKvQ6fHQ9d5pn3wS6Wh6ZLRIKdF1LiikS9nTw1N4pgWlsHQVYhZHgNbJnPZjhbQw
        MomDTSada/tixgp8BXXAfyWXy9ONagyreDInB3fhaBF3rCINRhTM9EiIc2q4tynl
        bct58+mJOtUCIg3xBur5m154wf9qI2VvgXoeGxSSrySAVpXSy1s805E2UxBKo54Q
        hYuWRTCjAFNKZLOitYONxEbCKfWXAgMBAAGjggNnMIIDYzAfBgNVHSMEGDAWgBRR
        aP+QrwIHdTzM2WVkYqISuFlyOzAdBgNVHQ4EFgQU0nlLx6NS/ibxKj0Gr7EUoBmu
        CwkwFgYDVR0RBA8wDYILdGEueXJkc2IuY2EwDgYDVR0PAQH/BAQDAgWgMB0GA1Ud
        JQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjB1BgNVHR8EbjBsMDSgMqAwhi5odHRw
        Oi8vY3JsMy5kaWdpY2VydC5jb20vc2hhMi1oYS1zZXJ2ZXItZzYuY3JsMDSgMqAw
        hi5odHRwOi8vY3JsNC5kaWdpY2VydC5jb20vc2hhMi1oYS1zZXJ2ZXItZzYuY3Js
        MEwGA1UdIARFMEMwNwYJYIZIAYb9bAEBMCowKAYIKwYBBQUHAgEWHGh0dHBzOi8v
        d3d3LmRpZ2ljZXJ0LmNvbS9DUFMwCAYGZ4EMAQICMIGDBggrBgEFBQcBAQR3MHUw
        JAYIKwYBBQUHMAGGGGh0dHA6Ly9vY3NwLmRpZ2ljZXJ0LmNvbTBNBggrBgEFBQcw
        AoZBaHR0cDovL2NhY2VydHMuZGlnaWNlcnQuY29tL0RpZ2lDZXJ0U0hBMkhpZ2hB
        c3N1cmFuY2VTZXJ2ZXJDQS5jcnQwDAYDVR0TAQH/BAIwADCCAX8GCisGAQQB1nkC
        BAIEggFvBIIBawFpAHcApLkJkLQYWBSHuxOizGdwCjw1mAT5G9+443fNDsgN3BAA
        AAFmiK35LwAABAMASDBGAiEAqT55y0nUufBRcwIMrfx0YEXte9aRvVWOZOby0xF/
        iXYCIQC0am0t2Qhc25+TYdhPReA82sSQmnzGkaNRRdz0BPG31AB2AId1v+dZfPiM
        Q5lfvfNu/1aNR1Y2/0q1YMG06v9eoIMPAAABZoit+ikAAAQDAEcwRQIgexPHJZYK
        QEZR+OptWd1hKD4cDJ4RSi7ay92ze+3bA6ACIQCbIGWmavGFCfjyL2U34M2FJO1F
        P8H4DpUEK4k9ObNu6QB2AO5Lvbd1zmC64UJpH6vhnmajD35fsHLYgwDEe4l6qP3L
        AAABZoit+XcAAAQDAEcwRQIhAK4nI7L/g3UAiCtMaGGQ0Fo+Ka96SI6Bq10WaMXe
        uAd9AiBqUMnyV2kzQCuJVe8JSbpCm96CHKtaaSCqc+a1pcOUrjANBgkqhkiG9w0B
        AQsFAAOCAQEAjYdVxjCo0YOWx0mN4SlNG/QL9Uuw/4py0evAIdJmjMKcnLq7VwMP
        kCI4GecoRkKPDr40d2KvYZ+I0LEIUPKTChCLex58YxrkNUKefi+tmuhfHCTY4wE3
        4DuE1hl2ESCoYuJy3fJ3GwqEoMIAvP38zKuoxnAA1eRdgw0ecRX5Nwj9KSM+p9cj
        j+wGHCLEIKCgsfrXScRpyizAgeFEDXcAC7ZpzXt9uJfzyIvkxEF0v0zjNVLLNGz+
        DT14220WAAF5hxphfaWULzv9oHSiLmZ//MhzWS8UgU9ZhqhSLQRXR6zZzzVIwO7I
        D0QHUJzF7i95f0Zv9l4QdT4ztnEoYHaxuQ==
        -----END CERTIFICATE-----
    """.trimIndent().writeToFile("data/ta.yrdsb.ca.cer")
}