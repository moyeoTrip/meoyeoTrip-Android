package kr.hanchae.moyeotrip.data.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatOutboxTest {
    @Test
    fun `queued messages survive offline and drain in send order`() {
        val outbox = ChatOutbox()
        outbox.enqueue("첫 번째", "10:01")
        outbox.enqueue("두 번째", "10:02")

        assertEquals(listOf("첫 번째", "두 번째"), outbox.pending.map { it.text })
        assertEquals(listOf("첫 번째", "두 번째"), outbox.drainInOrder().map { it.text })
        assertTrue(outbox.pending.isEmpty())
    }
}
