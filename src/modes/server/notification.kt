package modes.server

import LogLevel
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import log
import java.io.FileInputStream

private var initialized = false

fun sendFCM(token: String, notification: modes.server.updater.Notification): Boolean {
    if (!Config.notificationEnabled) {
        log(LogLevel.INFO, "Notification disabled, token: $token, content: $notification")
        return true
    }

    var deviceExists = true

    if (!initialized) {
        val serviceAccount = FileInputStream("data/serviceAccountKey.json")

        val options = FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl("https://yrdsb-teach-assist.firebaseio.com")
            .build()

        FirebaseApp.initializeApp(options)

        initialized = true
    }

    val message = Message.builder()
        .setNotification(Notification(notification.title, notification.body))
        .setToken(token)
        .build()

    try {
        val response = FirebaseMessaging.getInstance().send(message)
        log(LogLevel.INFO, "Sent notification to $token, content: $notification, response: $response")
    } catch (e: FirebaseMessagingException) {
        if (e.errorCode == "registration-token-not-registered") {
            log(LogLevel.INFO, "Failed to send notification to $token, device not exists. content: $notification")
            deviceExists = false
        }
        log(
            LogLevel.INFO,
            "Failed to send notification to $token, content: $notification, error code: ${e.errorCode}",
            e
        )
    } catch (e: Throwable) {
        log(LogLevel.INFO, "Failed to send notification to $token, content: $notification", e)
    }

    return deviceExists
}