package site.pegasis.ta.fetch.modes.server.timeline

import site.pegasis.ta.fetch.models.User
import java.util.*

typealias Batch = MutableList<User>

object AutoUpdateUserQueue {
    private val random = Random()
    private val batches: Queue<Batch> = LinkedList()

    fun addBatch(batch: List<User>) = batches.offer(batch.toMutableList())

    fun poll(): User? {
        var user: User? = null

        if (batches.isNotEmpty()) {
            val batch = batches.peek()!!
            val randomIndex = random.nextInt(batch.size)
            user = batch.removeAt(randomIndex)
            if (batch.isEmpty()) batches.poll()
        }

        return user
    }
}