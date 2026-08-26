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
                liveCapture = false,
                demoMode = false
            )
        )
        assertFalse(
            injectsServerData(
                startScreen = "explore",
                skipAuthentication = false,
                liveCapture = false,
                demoMode = false
            )
        )
    }

    @Test
    fun liveCaptureUnblocksServerDataOnCaptureRoutes() {
        assertTrue(
            injectsServerData(
                startScreen = "explore",
                skipAuthentication = true,
                liveCapture = true,
                demoMode = false
            )
        )
    }

    @Test
    fun demoBuildStaysMockEvenWithLiveFlag() {
        assertFalse(
            injectsServerData(
                startScreen = "explore",
                skipAuthentication = true,
                liveCapture = true,
                demoMode = true
            )
        )
    }

    @Test
    fun normalLaunchKeepsExistingBehaviour() {
        assertTrue(
            injectsServerData(
                startScreen = null,
                skipAuthentication = false,
                liveCapture = false,
                demoMode = false
            )
        )
        // 데모 모드·인증 우회 실행은 예전대로 서버를 타지 않는다
        assertFalse(
            injectsServerData(
                startScreen = null,
                skipAuthentication = true,
                liveCapture = false,
                demoMode = false
            )
        )
        assertFalse(
            injectsServerData(
                startScreen = null,
                skipAuthentication = false,
                liveCapture = false,
                demoMode = true
            )
        )
    }

    @Test
    fun planningMockDataOnlyInMockCapture() {
        assertTrue(usesPlanningMockData(captureMode = true, liveCapture = false))
        assertFalse(usesPlanningMockData(captureMode = true, liveCapture = true))
        assertFalse(usesPlanningMockData(captureMode = false, liveCapture = false))
    }

    @Test
    fun mockCaptureKeepsForcedOfflineOverride() {
        // 35·36·37 목 캡처는 실제 네트워크가 붙어 있어도 강제 플래그대로 그린다
        assertEquals(
            OfflineExperience.NoCache,
            resolveNetworkExperience(
                forcedOverride = QaStartRequest.parse("offline").offlineExperienceOverride,
                liveCapture = false,
                detectedOnline = true,
                hasCachedContent = true
            )
        )
        assertEquals(
            OfflineExperience.Cached,
            resolveNetworkExperience(
                forcedOverride = QaStartRequest.parse("offlineChat").offlineExperienceOverride,
                liveCapture = false,
                detectedOnline = true,
                hasCachedContent = false
            )
        )
    }

    @Test
    fun liveCaptureFollowsRealNetworkBlocking() {
        // 라이브 캡처는 `svc wifi disable` 결과를 그대로 그린다 — 강제 플래그가 덮어쓰지 않는다
        assertEquals(
            OfflineExperience.Online,
            resolveNetworkExperience(
                forcedOverride = QaStartRequest.parse("offline").offlineExperienceOverride,
                liveCapture = true,
                detectedOnline = true,
                hasCachedContent = true
            )
        )
        assertEquals(
            OfflineExperience.NoCache,
            resolveNetworkExperience(
                forcedOverride = null,
                liveCapture = true,
                detectedOnline = false,
                hasCachedContent = false
            )
        )
        assertEquals(
            OfflineExperience.Cached,
            resolveNetworkExperience(
                forcedOverride = null,
                liveCapture = true,
                detectedOnline = false,
                hasCachedContent = true
            )
        )
    }
}
