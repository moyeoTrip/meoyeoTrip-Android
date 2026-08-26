package kr.hanchae.moyeotrip.data

import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
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
import kr.hanchae.moyeotrip.data.social.HttpSocialRepository
import kr.hanchae.moyeotrip.data.social.SocialRepository
import kr.hanchae.moyeotrip.data.terms.HttpTermsRepository
import kr.hanchae.moyeotrip.data.terms.TermsRepository

/**
 * 로그인 세션이 있을 때 화면이 실서버 데이터를 읽어오는 통로.
 * 화면은 LaunchedEffect 에서 이 저장소들을 호출하고, 실패하면 기존 목데이터를 유지한다.
 * 목 캡처 라우트(moyeo_screen / QaStartRequest)에서는 아예 만들지 않는다 — 네트워크를 타지 않는다.
 * 라이브 캡처(moyeo_live_data)는 그 차단만 풀어 같은 라우팅으로 실서버 데이터를 그린다.
 */
class ServerDataDependencies(
    val chatRooms: ChatRoomRepository,
    val courses: TravelCourseRepository,
    val terms: TermsRepository,
    val notifications: NotificationRepository,
    val userProfile: UserProfileRepository,
    val social: SocialRepository,
    val feeds: FeedRepository
) {
    companion object {
        fun create(
            baseUrl: String,
            accessToken: () -> String?,
            refreshAccessToken: suspend () -> String? = { null }
        ): ServerDataDependencies {
            val client = MoyeoApiClient(baseUrl, accessToken, refreshAccessToken)
            return ServerDataDependencies(
                chatRooms = HttpChatRoomRepository(client),
                courses = HttpTravelCourseRepository(client),
                terms = HttpTermsRepository(client),
                notifications = HttpNotificationRepository(client),
                userProfile = HttpUserProfileRepository(client),
                social = HttpSocialRepository(client),
                feeds = HttpFeedRepository(client)
            )
        }
    }
}
