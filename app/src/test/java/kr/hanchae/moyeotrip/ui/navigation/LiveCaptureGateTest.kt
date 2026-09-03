package kr.hanchae.moyeotrip.ui.navigation

import kr.hanchae.moyeotrip.data.network.OfflineExperience
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 라이브 캡처(`moyeo_live_data`) 게이트.
 *
 * 가장 중요한 계약: **라이브 플래그가 없으면 기존 목 캡처 경로와 동일**해야 한다.
 * 이게 깨지면 화면기획 4열 대조 PDF 가 깨진다.
 */
class LiveCaptureGateTest {
    @Test
    fun mockCaptureRouteNeverInjectsServerData() {
        // 캡처 도구가 쓰는 조합: moyeo_screen + moyeo_skip_auth
        assertFalse(
            injectsServerData(
                startScreen = "explore",
                skipAuthentication = true,
                liveCapture = false
            )
        )
        assertFalse(
            injectsServerData(
                startScreen = "explore",
                skipAuthentication = false,
                liveCapture = false
            )
        )
    }

    @Test
    fun liveCaptureUnblocksServerDataOnCaptureRoutes() {
        assertTrue(
            injectsServerData(
                startScreen = "explore",
                skipAuthentication = true,
                liveCapture = true
            )
        )
    }

    @Test
    fun normalLaunchKeepsExistingBehaviour() {
        assertTrue(
            injectsServerData(
                startScreen = null,
                skipAuthentication = false,
                liveCapture = false
            )
        )
        // 인증 우회 실행은 예전대로 서버를 타지 않는다
        assertFalse(
            injectsServerData(
                startScreen = null,
                skipAuthentication = true,
                liveCapture = false
            )
        )
    }

    @Test
    fun captureKeepsForcedOfflineOverrideEvenWhenLive() {
        // 35·36·37 캡처는 실제 네트워크가 붙어 있어도 강제 플래그대로 그린다.
        // 라이브 캡처는 서버를 타야 해서 네트워크를 켠 채 돌린다 — 여기서 플래그를 버리면
        // 35·36 자리에 오프라인 표시가 없는 홈 화면이 찍힌다.
        assertEquals(
            OfflineExperience.NoCache,
            resolveNetworkExperience(
                forcedOverride = QaStartRequest.parse("offline").offlineExperienceOverride,
                detectedOnline = true,
                hasCachedContent = true
            )
        )
        assertEquals(
            OfflineExperience.Cached,
            resolveNetworkExperience(
                forcedOverride = QaStartRequest.parse("offlineCached").offlineExperienceOverride,
                detectedOnline = true,
                hasCachedContent = false
            )
        )
        assertEquals(
            OfflineExperience.Cached,
            resolveNetworkExperience(
                forcedOverride = QaStartRequest.parse("offlineChat").offlineExperienceOverride,
                detectedOnline = true,
                hasCachedContent = false
            )
        )
    }

    @Test
    fun runsWithoutForcedFlagFollowRealNetwork() {
        // 강제 플래그가 없는 실행은 실제 연결 상태를 그대로 그린다
        assertEquals(
            OfflineExperience.Online,
            resolveNetworkExperience(
                forcedOverride = null,
                detectedOnline = true,
                hasCachedContent = true
            )
        )
        assertEquals(
            OfflineExperience.NoCache,
            resolveNetworkExperience(
                forcedOverride = null,
                detectedOnline = false,
                hasCachedContent = false
            )
        )
        assertEquals(
            OfflineExperience.Cached,
            resolveNetworkExperience(
                forcedOverride = null,
                detectedOnline = false,
                hasCachedContent = true
            )
        )
    }
}
