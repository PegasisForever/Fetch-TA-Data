package modes

import com.google.firebase.FirebaseApp
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseOptions
import java.io.FileInputStream
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.sun.net.httpserver.HttpServer
import getReqJSONObject
import java.net.InetSocketAddress
import models.LoginException
import models.User
import org.json.simple.parser.ParseException
import send
import webpage.LoginPage


fun startServer() {
    User.init()

    val server = HttpServer.create(InetSocketAddress(1560), 0)

    server.createContext("/getmark") { exchange ->
        var statusCode = 200  //200:success  400:bad request  401:pwd incorrect  500:internal error
        var res = ""

        try {
            val req = exchange.getReqJSONObject()

            res = LoginPage()
                .gotoSummaryPage(req["number"] as String, req["password"] as String)
                .fillDetails()
                .courses
                .toJSONString()
        } catch (e: LoginException) {
            e.printStackTrace()
            statusCode = 401
        } catch (e: ParseException) {
            e.printStackTrace()
            statusCode = 400
        } catch (e: Exception) {
            e.printStackTrace()
            statusCode = 500
        }

        exchange.send(statusCode, res)
    }

    server.createContext("/regi"){ exchange ->
        var statusCode = 200  //200:success  400:bad request  401:pwd incorrect  500:internal error
        var res = ""

        try {
            val req = exchange.getReqJSONObject()
            val user = User.fromClient(req)

            res = LoginPage()
                .gotoSummaryPage(user.number, user.password)
                .courses
                .toJSONString()

            if (user.receiveNotification){
                User.add(user)
            }
        } catch (e: LoginException) {
            e.printStackTrace()
            statusCode = 401
        } catch (e: ParseException) {
            e.printStackTrace()
            statusCode = 400
        } catch (e: Exception) {
            e.printStackTrace()
            statusCode = 500
        }

        exchange.send(statusCode, res)
    }

    server.createContext("/deregi"){ exchange ->
        var statusCode = 200  //200:success  400:bad request  500:internal error

        try {
            val req = exchange.getReqJSONObject()
            val user = User.fromClient(req)

            User.remove(user)
        } catch (e: ParseException) {
            e.printStackTrace()
            statusCode = 400
        } catch (e: Exception) {
            e.printStackTrace()
            statusCode = 500
        }

        exchange.send(statusCode, "")
    }

    server.start()
    println("Server started.")
}

fun sendNotification() {
    val serviceAccount = FileInputStream("data/serviceAccountKey.json")

    val options = FirebaseOptions.Builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .setDatabaseUrl("https://yrdsb-teach-assist.firebaseio.com")
        .build()

    FirebaseApp.initializeApp(options)


    val registrationToken =
        "c1uqeYKMihE:APA91bFKAW-syXg5s5nVQzayGuA-PB03oPtQ0r2fKVmd8pXXep15Y38QA5tGko95FH2Ohq97nhyW7wCH34Nd8PJ-g3xVGIhJi-Nm5jYD3Snzsu27Lgt64GmMmyRFsQH95eHoHcAYMm8R"

    val message = Message.builder()
        .setNotification(Notification("server", "body"))
        .setToken(registrationToken)
        .build()

    val response = FirebaseMessaging.getInstance().send(message)
    println("Successfully sent message: $response")
}