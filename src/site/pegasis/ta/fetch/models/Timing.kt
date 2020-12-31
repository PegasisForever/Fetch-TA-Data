package site.pegasis.ta.fetch.models

class Timing {
    private val timeArray = ArrayList<TimingItem>()
    private var startTime = System.currentTimeMillis()
    var lastTime = startTime

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
        sb.append("Total: ${System.currentTimeMillis() - startTime}ms")
        return sb.toString()
    }

    override fun toString() = getResult()

    operator fun invoke(name: String) {
        setPoint(name)
    }

    inline operator fun <T> invoke(name: String, action: () -> T): T {
        lastTime = System.currentTimeMillis()
        val result = action()
        setPoint(name)
        return result
    }

    suspend fun <T> suspend(name: String, action: suspend () -> T): T {
        lastTime = System.currentTimeMillis()
        val result = action()
        setPoint(name)
        return result
    }
}

class TimingItem(val name: String, val time: Long)
