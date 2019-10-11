package modes.server

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import java.io.FileInputStream

fun sendNotification() {
    val serviceAccount = FileInputStream("data/serviceAccountKey.json")

    val options = FirebaseOptions.Builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .setDatabaseUrl("https://yrdsb-teach-assist.firebaseio.com")
        .build()

    FirebaseApp.initializeApp(options)

    val registrationToken =
        "cIZ9DJvBeiM:APA91bGccS9MCW-T19MbN_M8py54c6KUCZbPhE4LH3kOw49qVxNrmZUP6gNdc86LViiObBzGicmIRStzgKMEujZJx0n-dSnKoR1i9Pc96wm1YrUKEd7rpZscK0_yuqeOClH2QGIvZrV7"

    val message = Message.builder()
        .setNotification(Notification("server", "body"))
        .setToken(registrationToken)
        .build()

    val response = FirebaseMessaging.getInstance().send(message)
    println("Successfully sent message: $response")
}