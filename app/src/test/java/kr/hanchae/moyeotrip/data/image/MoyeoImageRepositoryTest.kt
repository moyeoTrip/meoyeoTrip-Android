package kr.hanchae.moyeotrip.data.image

import org.junit.Assert.assertEquals
import org.junit.Test

class MoyeoImageRepositoryTest {
    @Test
    fun `cache key uses decoded file name and retry policy is three attempts`() {
        assertEquals(
            "profile-image.png",
            MoyeoImageRepository.cacheKey("https://cdn.example.com/users/profile-image.png?v=2")
        )
        assertEquals(3, MoyeoImageRepository.MAXIMUM_ATTEMPTS)
    }
}
