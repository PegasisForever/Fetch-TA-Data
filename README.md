# Fetch TA Data

One of the three programs in the project. Other two:  [YRDSB Teach Assist Mobile](https://github.com/PegasisForever/YRDSB-Teach-Assist-Mobile), [YRDSB Teach Assist Web](https://github.com/PegasisForever/YRDSB-Teach-Assist-Web).

Made by a grade 11 student in MCI.

This is a tool for fetching data from [Official Teach Assist](https://ta.yrdsb.ca/yrdsb/index.php) and rearrange it to a machine-friendly JSON format. It is also a server of the unofficial mobile APP and the website. The [TA Public API](https://api.pegasis.site/docs/ta/) is provided by it.

When used as a server, it acts as a "compatibility layer" which allows me to update TA-fetching-algorithm without upgrading my app or website. Also, it allows me to fetch data from official TA periodically and send notifications (Using FCM).

```
My YRDSB Teach Assist APP <-┐
                            ├-> Fetch TA Data Server <--> Official TA Website
My YRDSB Teach Assist Web <-┘
```

## To Get JSON-Formatted Data From TA

Run command line: `java -jar fetch_ta_data.jar [student_number] [password]`

## To Run as Server

1. Create following folders and files:

   ```
   ├── data
   │   ├── timelines
   │   ├── courselists
   │   ├── courselists-archived
   │   ├── log
   │   ├── feedback.txt
   │   ├── serviceAccountKey.json
   │   ├── config.json
   │   └── users.json
   └── fetch_ta_data.jar
   ```

   Text in `config.json`:

   ```json
   {
     "notification": true,
     "auto_update": true,
     "auto_update_interval_minute": 40
   }
   ```

   Text in `users.json`:

   ```json
   []
   ```

   `serviceAccountKey.json` is used for sending FCM messages, you can get this file from FCM.

   Leave other folders and files blank.

2. Run command line: `java -jar fetch_ta_data.jar server`

3. The private server for app will start on port `5004` and the public server is on port `5005`.

## Support Me

I spent hundreds of hours on this project, consider buy me a cup of coffee?

Donate: [patreon](https://www.patreon.com/yrdsbta)

Feedback: Create a GitHub issue or email me [admin@pegasis.site](mailto:admin@pegasis.site).

Development: Create a pull request or email me [admin@pegasis.site](mailto:admin@pegasis.site)