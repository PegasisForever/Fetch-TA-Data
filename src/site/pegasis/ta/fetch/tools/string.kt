package site.pegasis.ta.fetch.tools

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream


fun find(str: String, regex: String): ArrayList<String>? {
    var result: ArrayList<String>? = ArrayList()

    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(str)

    while (matcher.find()) {
        result!!.add(matcher.group(0))
    }

    if (result!!.size == 0) {
        result = null
    }

    return result
}

fun findFirst(str: String, regex: String): String? {
    return find(str, regex)?.get(0)
}

fun String.matches(regex: String) = this.matches(Regex(regex))

fun String.fill(str: String): String {
    return replace("%s", str)
}

fun String.gzip(): ByteArray {
    val bos = ByteArrayOutputStream()
    GZIPOutputStream(bos).bufferedWriter(StandardCharsets.UTF_8).use { it.write(this) }
    return bos.toByteArray()
}

fun ByteArray.unGzip(): String {
    return GZIPInputStream(inputStream()).bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
}

fun String.removeBlank(): String {
    return this.replace("\n", "").replace(" ", "")
}

operator fun String.times(time: Int): String {
    val builder = StringBuilder()
    repeat(time) {
        builder.append(this)
    }

    return builder.toString()
}