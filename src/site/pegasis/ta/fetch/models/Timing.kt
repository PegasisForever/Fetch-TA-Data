package site.pegasis.ta.fetch.models

class Timing {
    private val timeArray = ArrayList<TimingItem>()
    private var lastTime = System.currentTimeMillis()

    fun setPoint(name: String) {
        val currentTime = System.currentTimeMillis()
        timeArray += TimingItem(name, currentTime - lastTime)
        lastTime = currentTime
    }

    fun getResult(divider: String = "  "): String {
        val sb = StringBuilder()
        timeArray.forEach { item ->
            sb.append("${item.name}: ${item.time}ms$divider")
        }
        return sb.toString()
    }

    operator fun invoke(name: String) {
        setPoint(name)
    }
}

class TimingItem(val name: String, val time: Long)