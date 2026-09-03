package kr.hanchae.moyeotrip.data

import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
import kr.hanchae.moyeotrip.data.auth.userIdClaim
import kr.hanchae.moyeotrip.data.courses.HttpTravelCourseRepository
import kr.hanchae.moyeotrip.data.courses.TravelCourseRepository
import kr.hanchae.moyeotrip.data.feed.FeedRepository
import kr.hanchae.moyeotrip.data.feed.HttpFeedRepository
import kr.hanchae.moyeotrip.data.notifications.HttpNotificationRepository
import kr.hanchae.moyeotrip.data.notifications.NotificationRepository
import kr.hanchae.moyeotrip.data.profile.HttpUserProfileRepository
import kr.hanchae.moyeotrip.data.profile.UserProfileRepository
import kr.hanchae.moyeotrip.data.rooms.ChatRoomRepository
import kr.hanchae.moyeotrip.data.rooms.HttpChatRoomRepository
import kr.hanchae.moyeotrip.data.search.HttpPopularKeywordRepository
import kr.hanchae.moyeotrip.data.search.PopularKeywordRepository
import kr.hanchae.moyeotrip.data.social.HttpSocialRepository
import kr.hanchae.moyeotrip.data.social.SocialRepository
import kr.hanchae.moyeotrip.data.terms.HttpTermsRepository
import kr.hanchae.moyeotrip.data.terms.TermsRepository
import kr.hanchae.moyeotrip.data.weather.HttpWeatherRepository
import kr.hanchae.moyeotrip.data.weather.WeatherRepository
import kr.hanchae.moyeotrip.domain.auth.SignupGateStage

/**
 * 로그인 세션이 있을 때 화면이 실서버 데이터를 읽어오는 통로.
 * 화면은 LaunchedEffect 에서 이 저장소들을 호출하고, 실패하면 오류 상태를 그린다 — 목데이터로 채우지 않는다.
 * 미로그인·데모 실행에서는 아예 만들지 않는다(null) — 화면은 로그인 안내를 그린다.
 */
class ServerDataDependencies(
    val chatRooms: ChatRoomRepository,
    val courses: TravelCourseRepository,
    val terms: TermsRepository,
    val notifications: NotificationRepository,
    val userProfile: UserProfileRepository,
    val social: SocialRepository,
    val feeds: FeedRepository,
    val weather: WeatherRepository,
    /** 12 검색 · 인기 검색어 (GET search/popular-keywords). 0건이면 섹션을 그리지 않는다. */
    val popularKeywords: PopularKeywordRepository,
    /**
     * 로그인한 사용자의 서버 id — 액세스 토큰 페이로드에서 **동기로** 읽는다.
     * 네트워크 응답을 기다려야 알 수 있는 값이 아니므로 첫 프레임부터 쓸 수 있다(정본 R6).
     */
    val signedInUserId: () -> Long? = { null }
) {
    companion object {
        fun create(
            baseUrl: String,
            accessToken: () -> String?,
            refreshAccessToken: suspend () -> String? = { null },
            // 가입이 안 끝난 사용자를 서버가 막았을 때(409 40902·40918) 알림받는 통로.
            // 화면마다 409 를 따로 다루면 어디선가 반드시 새므로 클라이언트 한 곳에서 잡는다.
            onSignupGate: (SignupGateStage) -> Unit = {}
        ): ServerDataDependencies {
            val client = MoyeoApiClient(baseUrl, accessToken, refreshAccessToken, onSignupGate)
            return ServerDataDependencies(
                chatRooms = HttpChatRoomRepository(client),
                courses = HttpTravelCourseRepository(client),
                terms = HttpTermsRepository(client),
                notifications = HttpNotificationRepository(client),
                userProfile = HttpUserProfileRepository(client),
                social = HttpSocialRepository(client),
                feeds = HttpFeedRepository(client),
                weather = HttpWeatherRepository(client),
                popularKeywords = HttpPopularKeywordRepository(client),
                signedInUserId = { accessToken()?.userIdClaim() }
            )
        }
    }
}
