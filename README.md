# Fetch TA Data

One of the three programs in the project. Other two:  [YRDSB Teach Assist Mobile](https://github.com/PegasisForever/YRDSB-Teach-Assist-Mobile), [YRDSB Teach Assist Web](https://github.com/PegasisForever/YRDSB-Teach-Assist-Web).

Made by a grade 11 student in MCI.

This is a tool for fetching data from [Official Teach Assist](https://ta.yrdsb.ca/yrdsb/index.php) and rearranging it to a machine-friendly JSON format. It is also a server of the unofficial mobile APP and the website. The [TA Public API](https://api.pegasis.site/docs/ta/) is provided by it.

When used as a server, it acts as a "compatibility layer" which allows me to update TA-fetching-algorithm without upgrading my app or website. Also, it allows me to fetch data from official TA periodically and send notifications (Using FCM).

```
My YRDSB Teach Assist APP <-┐
                            ├-> Fetch TA Data Server <--> Official TA Website
My YRDSB Teach Assist Web <-┘
```

## To Get JSON-Formatted Data From TA

`java -jar fetch_ta_data.jar getmark [student_number] [password]`

add `-h` to get help

## To Run as a Server

`java -jar fetch_ta_data.jar server`

add `-h` to get help

The config file is `data/config.json`. If you need to send notifications, you need to add `data/serviceAccountKey.json` file (download it from firebase cloud messaging)

## Support Me

I spent hundreds of hours on this project, consider buy me a cup of coffee?

Donate: [patreon](https://www.patreon.com/yrdsbta)

Feedback: Create a GitHub issue or email me [admin@pegasis.site](mailto:admin@pegasis.site).

Development: Create a pull request or email me [admin@pegasis.site](mailto:admin@pegasis.site)
