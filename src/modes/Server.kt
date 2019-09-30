package modes

import com.google.firebase.FirebaseApp
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseOptions
import java.io.FileInputStream
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.sun.net.httpserver.HttpServer
import getReqString
import jsonParser
import log
import logUnhandled
import java.net.InetSocketAddress
import models.LoginException
import models.User
import org.json.simple.JSONObject
import org.json.simple.parser.ParseException
import send
import webpage.LoginPage
import java.lang.Thread.setDefaultUncaughtExceptionHandler




fun startServer() {
    log(LogLevel.INFO,"Starting server")

    setDefaultUncaughtExceptionHandler { thread:Thread?, e:Throwable ->
        logUnhandled(thread,e)
    }
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            log(LogLevel.INFO,"Server stopped")
        }
    })

    User.init()

    val server = HttpServer.create(InetSocketAddress(5004), 0)

    server.createContext("/getmark") { exchange ->
        var statusCode = 200  //200:success  400:bad request  401:pwd incorrect  500:internal error
        var res = ""

        val hash = exchange.hashCode()
        val reqString = exchange.getReqString()
        val ipAddress = exchange.remoteAddress.address.toString()
        log(LogLevel.INFO,"Request #$hash /getmark <- $ipAddress, data=$reqString")

        try {
            val req = jsonParser.parse(reqString) as JSONObject

            res = LoginPage()
                .gotoSummaryPage(req["number"] as String, req["password"] as String)
                .fillDetails()
                .courses
                .toJSONString()
            log(LogLevel.INFO,"Request #$hash /getmark :: Fetch successfully")
        } catch (e: LoginException) {
            log(LogLevel.INFO,"Request #$hash /getmark :: Login error")
            statusCode = 401
        } catch (e: ParseException) {
            log(LogLevel.INFO,"Request #$hash /getmark :: Can't parse request")
            statusCode = 400
        } catch (e: Exception) {
            log(LogLevel.ERROR,"Request #$hash /getmark :: Unknown error: ${e.message}",e)
            statusCode = 500
        }

        log(LogLevel.INFO,"Request #$hash /getmark -> $ipAddress, status=$statusCode, data=$res")
        exchange.send(statusCode, res)
    }

    server.createContext("/regi"){ exchange ->
        var statusCode = 200  //200:success  400:bad request  401:pwd incorrect  500:internal error
        var res = ""

        val hash = exchange.hashCode()
        val reqString = exchange.getReqString()
        val ipAddress = exchange.remoteAddress.address.toString()
        log(LogLevel.INFO,"Request #$hash /regi <- $ipAddress, data=$reqString")

        try {
            val req = jsonParser.parse(reqString) as JSONObject
            val user = User.fromClient(req)

            res = LoginPage()
                .gotoSummaryPage(user.number, user.password)
                .fillDetails()
                .courses
                .toJSONString()

            log(LogLevel.INFO,"Request #$hash /regi :: User verified successfully")

            if (user.receiveNotification){
                User.add(user)
            }
        } catch (e: LoginException) {
            log(LogLevel.INFO,"Request #$hash /regi :: Login error")
            statusCode = 401
        } catch (e: ParseException) {
            log(LogLevel.INFO,"Request #$hash /regi :: Can't parse request")
            statusCode = 400
        } catch (e: Exception) {
            log(LogLevel.ERROR,"Request #$hash /regi :: Unknown error: ${e.message}",e)
            statusCode = 500
        }

        log(LogLevel.INFO,"Request #$hash /regi -> $ipAddress, status=$statusCode, data=$res")
        exchange.send(statusCode, res)
    }

    server.createContext("/deregi"){ exchange ->
        var statusCode = 200  //200:success  400:bad request  500:internal error

        val hash = exchange.hashCode()
        val reqString = exchange.getReqString()
        val ipAddress = exchange.remoteAddress.address.toString()
        log(LogLevel.INFO,"Request #$hash /deregi <- $ipAddress, data=$reqString")

        try {
            val req = jsonParser.parse(reqString) as JSONObject
            val user = User.fromClient(req)

            User.remove(user)
        } catch (e: ParseException) {
            log(LogLevel.INFO,"Request #$hash /deregi :: Can't parse request")
            statusCode = 400
        } catch (e: Exception) {
            log(LogLevel.ERROR,"Request #$hash /deregi :: Unknown error: ${e.message}",e)
            statusCode = 500
        }

        log(LogLevel.INFO,"Request #$hash /deregi -> $ipAddress, status=$statusCode")
        exchange.send(statusCode, "")
    }

    server.start()
    log(LogLevel.INFO,"Server started")
}

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