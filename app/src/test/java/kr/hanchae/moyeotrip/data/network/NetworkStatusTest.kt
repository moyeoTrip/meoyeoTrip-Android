package kr.hanchae.moyeotrip.data.network

import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkStatusTest {
    @Test
    fun `offline experience preserves cached content when available`() {
        assertEquals(OfflineExperience.Online, offlineExperience(isOnline = true, hasCachedContent = false))
        assertEquals(OfflineExperience.Cached, offlineExperience(isOnline = false, hasCachedContent = true))
        assertEquals(OfflineExperience.NoCache, offlineExperience(isOnline = false, hasCachedContent = false))
    }
}
