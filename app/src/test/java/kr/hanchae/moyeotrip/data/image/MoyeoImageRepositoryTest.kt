package kr.hanchae.moyeotrip.data.image

import kr.hanchae.moyeotrip.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

    /**
     * `travel-courses` 계열 썸네일은 상대경로(`tourism/image/….webp`)로 온다.
     * 그대로 내려받으려 하면 전부 실패해서 09 홈·12-1 검색·14 코스 상세의 사진이 통째로 사라졌다.
     */
    @Test
    fun `relative thumbnails get the cdn host and absolute urls pass through`() {
        assertEquals(
            "${BuildConfig.CDN_BASE_URL}/tourism/image/269cab52.webp",
            MoyeoImageRepository.absoluteUrl("tourism/image/269cab52.webp")
        )
        assertEquals(
            "${BuildConfig.CDN_BASE_URL}/tourism/image/269cab52.webp",
            MoyeoImageRepository.absoluteUrl("/tourism/image/269cab52.webp")
        )
        // tourism-contents 는 이미 절대 URL 이다 — 손대지 않는다
        assertEquals(
            "https://moyeo-trip-cdn.jayden-bin.cc/tourism/image/a.webp",
            MoyeoImageRepository.absoluteUrl("https://moyeo-trip-cdn.jayden-bin.cc/tourism/image/a.webp")
        )
        assertNull(MoyeoImageRepository.absoluteUrl(null))
        assertNull(MoyeoImageRepository.absoluteUrl("   "))
        assertNull(MoyeoImageRepository.absoluteUrl("data:image/png;base64,AAAA"))
    }
}
