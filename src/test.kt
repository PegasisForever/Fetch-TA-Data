import modes.server.sendFCM
import modes.server.updater.Notification

fun main() {
    sendFCM("epb3zVjYukI:APA91bGaYO8vm98kuQKHzpWLuuSWfqJIpScRe3iOmd-HZQlc1jHNik1LVpI8Q_uHp42amEwrG2nzy-4SKrLNcxjOqU3MV28os2G9nwKSkmUyJJ5sNP8Ipl32CJWa7GfVwUsMX48IVecq",
        Notification("title","body")
    )
}