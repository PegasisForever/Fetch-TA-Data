import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun main() {
    println(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
}