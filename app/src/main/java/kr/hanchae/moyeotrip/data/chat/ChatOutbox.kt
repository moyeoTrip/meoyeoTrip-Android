package kr.hanchae.moyeotrip.data.chat

data class QueuedChatMessage(val id: Long, val text: String, val time: String)

class ChatOutbox {
    private val queue = ArrayDeque<QueuedChatMessage>()
    private var nextId = 1L

    val pending: List<QueuedChatMessage>
        get() = queue.toList()

    fun enqueue(text: String, time: String): QueuedChatMessage {
        val message = QueuedChatMessage(id = nextId++, text = text, time = time)
        queue.addLast(message)
        return message
    }

    fun drainInOrder(): List<QueuedChatMessage> = buildList {
        while (queue.isNotEmpty()) add(queue.removeFirst())
    }
}
