package kr.hanchae.moyeotrip.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import kr.hanchae.moyeotrip.domain.WeatherSignal

object MockTripRepository {
    val currentWeatherSignal = WeatherSignal.Clear

    val courses = listOf(
        TripCourse(
            id = "cheongsong-juwangsan",
            title = "주왕산 & 주산지 힐링 트레킹",
            region = "청송",
            oneLine = "기암절벽과 맑은 주산지 풍경을 함께 즐기는 숲길 코스예요.",
            imageEmoji = "🌄",
            duration = "당일",
            courseTime = "2시간",
            distance = "6.2km",
            recommendedSeason = "봄~가을",
            price = "42,000원",
            host = "숲속 사슴 2417",
            hostAvatar = "🦌",
            participants = 2,
            capacity = 5,
            deadlineLabel = "마감 D-1",
            startLabel = "2026.06.06 (토)",
            meetingPoint = "청송 시외버스터미널",
            rating = 4.8,
            tags = listOf("자연", "히든명소", "추천"),
            stops = listOf("주왕산국립공원", "용연폭포", "주산지"),
            recruitmentNote = "주왕산 숲길을 천천히 걷고 주산지 물그림자까지 둘러보는 당일 모임이에요."
        ),
        TripCourse(
            id = "andong-hahoe",
            title = "안동 하회마을 하루 코스",
            region = "안동",
            oneLine = "고택 골목, 낙동강 물길, 로컬 간식까지 천천히 걷는 하루 코스",
            imageEmoji = "🏞️",
            duration = "당일",
            courseTime = "4시간",
            distance = "8.1km",
            recommendedSeason = "사계절",
            price = "39,000원",
            host = "초록 여우 5824",
            hostAvatar = "🦊",
            participants = 3,
            capacity = 6,
            deadlineLabel = "마감 D-3",
            startLabel = "2026.06.09 (화)",
            meetingPoint = "안동역",
            rating = 4.9,
            tags = listOf("고택", "산책", "로컬간식"),
            stops = listOf("하회마을 입구", "부용대 전망", "로컬 찻집", "월영교 야경"),
            recruitmentNote = "사진 찍는 속도에 맞춰 여유롭게 이동해요. 혼자 오는 분 환영!"
        ),
        TripCourse(
            id = "gyeongju-healing",
            title = "경주 감성 힐링 코스",
            region = "경주",
            oneLine = "첨성대부터 동궁과 월지까지, 노을 이후가 더 예쁜 야간 여행",
            imageEmoji = "🌅",
            duration = "1박 2일",
            courseTime = "5시간",
            distance = "7.3km",
            recommendedSeason = "봄~가을",
            price = "68,000원",
            host = "달빛 토끼 6142",
            hostAvatar = "🐰",
            participants = 4,
            capacity = 6,
            deadlineLabel = "마감 D-3",
            startLabel = "2026.06.05 (금)",
            meetingPoint = "신경주역",
            rating = 4.8,
            tags = listOf("야경", "피크닉", "역사"),
            stops = listOf("황리단길", "첨성대", "동궁과 월지", "감포 바다"),
            recruitmentNote = "야간 산책과 조용한 대화를 좋아하는 분들에게 잘 맞아요."
        ),
        TripCourse(
            id = "pohang-sea",
            title = "포항·영덕 동해 드라이브",
            region = "포항",
            oneLine = "바다 전망 카페, 스페이스워크, 죽도시장까지 산뜻하게",
            imageEmoji = "🌉",
            duration = "당일",
            courseTime = "4시간",
            distance = "9.1km",
            recommendedSeason = "여름",
            price = "35,000원",
            host = "우직한 곰 7821",
            hostAvatar = "🐻",
            participants = 6,
            capacity = 6,
            deadlineLabel = "확정 ✓",
            startLabel = "2026.06.15 (월)",
            meetingPoint = "포항역",
            rating = 4.7,
            tags = listOf("바다", "브런치", "시장"),
            stops = listOf("영일대해수욕장", "스페이스워크", "오션뷰 브런치", "죽도시장"),
            recruitmentNote = "시장 먹거리 취향을 나눠서 여러 메뉴를 같이 맛봐요."
        ),
        TripCourse(
            id = "ulleung-island",
            title = "울릉도 2박 3일 섬 여행",
            region = "울릉",
            oneLine = "바다 전망과 짧은 트레킹, 섬마을 산책을 묶은 여유로운 일정이에요.",
            imageEmoji = "🌌",
            duration = "2박 3일",
            courseTime = "2박 3일",
            distance = "12.4km",
            recommendedSeason = "봄~가을",
            price = "189,000원",
            host = "고요한 두루미 1130",
            hostAvatar = "🪽",
            participants = 3,
            capacity = 5,
            deadlineLabel = "마감 D-5",
            startLabel = "2026.07.12 (일)",
            meetingPoint = "포항여객선터미널",
            rating = 4.8,
            tags = listOf("섬", "트레킹", "바다"),
            stops = listOf("도동항", "행남해안산책로", "나리분지", "저동항"),
            recruitmentNote = "배편 확정 전까지 함께 일정을 조율하고, 확정 후 채팅방에서 준비물을 나눠요."
        ),
        TripCourse(
            id = "mungyeong-saejae",
            title = "문경 새재 단풍 트레킹",
            region = "문경",
            oneLine = "완만한 고갯길과 단풍 숲길을 천천히 걷는 가을 산책 코스",
            imageEmoji = "🍁",
            duration = "당일",
            courseTime = "3시간",
            distance = "5.6km",
            recommendedSeason = "가을",
            price = "28,000원",
            host = "달빛 토끼 6142",
            hostAvatar = "🐰",
            participants = 2,
            capacity = 5,
            deadlineLabel = "마감 D-1",
            startLabel = "2026.10.31 (토)",
            meetingPoint = "문경새재 제1주차장",
            rating = 4.8,
            tags = listOf("단풍", "트레킹", "숲길"),
            stops = listOf("제1관문", "조령원터", "오픈세트장", "새재길 쉼터"),
            recruitmentNote = "대화보다 풍경을 즐기는 조용한 속도, 쉬는 시간을 자주 가져요."
        ),
        TripCourse(
            id = "yeongju-buseoksa",
            title = "영주 부석사 눈꽃 산책",
            region = "영주",
            oneLine = "부석사의 겨울 능선과 짧은 산책길을 안전하게 둘러보는 코스",
            imageEmoji = "❄️",
            duration = "당일",
            courseTime = "2시간",
            distance = "4.2km",
            recommendedSeason = "겨울",
            price = "31,000원",
            host = "느긋한 토끼 7821",
            hostAvatar = "🐰",
            participants = 4,
            capacity = 5,
            deadlineLabel = "마감 D-2",
            startLabel = "2026.12.14 (월)",
            meetingPoint = "영주역",
            rating = 4.7,
            tags = listOf("사찰", "눈", "짧은동선"),
            stops = listOf("부석사 일주문", "무량수전", "소수서원", "풍기 카페"),
            recruitmentNote = "눈길 이동을 줄이고 따뜻한 실내 휴식 시간을 넉넉히 잡아요."
        ),
        TripCourse(
            id = "andong-dosan",
            title = "안동 도산서원 그늘 코스",
            region = "안동",
            oneLine = "더운 날에도 쉬어가기 좋은 서원과 강변 그늘 중심 코스",
            imageEmoji = "🌿",
            duration = "반나절",
            courseTime = "2시간",
            distance = "3.8km",
            recommendedSeason = "여름",
            price = "24,000원",
            host = "잔잔한 거북이 9032",
            hostAvatar = "🐢",
            participants = 2,
            capacity = 4,
            deadlineLabel = "마감 D-4",
            startLabel = "2026.07.07 (화)",
            meetingPoint = "안동역",
            rating = 4.6,
            tags = listOf("역사", "그늘", "짧은동선"),
            stops = listOf("도산서원", "낙동강 전망대", "서원 숲길", "전통찻집"),
            recruitmentNote = "더운 날씨에는 그늘과 실내 휴식 시간을 먼저 확인하며 움직여요."
        )
    )

    private val mockChatThreads = listOf(
        ChatThread(
            id = "chat-gyeongju-fall",
            title = "경주 단풍·야경",
            partner = "우직한 곰 7821",
            avatar = "🐻",
            lastMessage = "우직한 곰: 내일 오후 2시 만나요",
            time = "3:42",
            unreadCount = 3,
            countText = "4/8명",
            statusText = "마감 D-3",
            stateLabel = "진행중",
            messages = listOf(
                ChatMessage("우직한 곰 7821", "신청 감사합니다. 내일 오후 2시 만나요.", "3:22"),
                ChatMessage("나", "좋아요. 신경주역으로 가면 될까요?", "3:28", mine = true),
                ChatMessage("우직한 곰 7821", "네, 2번 출구 앞에서 모일게요.", "3:42")
            )
        ),
        ChatThread(
            id = "chat-pohang-drive",
            title = "포항·영덕 동해 드라이브",
            partner = "따스한 사슴 3492",
            avatar = "🦌",
            lastMessage = "잔잔한 거북이: 오늘 사진 올릴게요",
            time = "1:20",
            unreadCount = 0,
            countText = "6/6명",
            statusText = "확정 ✓",
            stateLabel = "확정",
            messages = listOf(
                ChatMessage("따스한 사슴 3492", "확정 인원 기준으로 식당 예약해둘게요.", "어제"),
                ChatMessage("나", "운전 동선은 공유해주시면 확인할게요.", "어제", mine = true)
            )
        ),
        ChatThread(
            id = "chat-andong-hahoe",
            title = "안동 하회마을 한옥체험",
            partner = "초록 여우 5824",
            avatar = "🦊",
            lastMessage = "고요한 두루미: 내일 출발이에요!",
            time = "어제",
            unreadCount = 1,
            countText = "3/6명",
            statusText = "확정 ✓",
            stateLabel = "진행중",
            messages = listOf(
                ChatMessage("초록 여우 5824", "한옥 체크인 시간은 오후 4시예요.", "월"),
                ChatMessage("나", "그 전에 하회마을을 먼저 보면 좋겠어요.", "월", mine = true)
            )
        ),
        ChatThread(
            id = "chat-mungyeong-fall",
            title = "문경 새재 단풍 트레킹",
            partner = "고요한 두루미 1130",
            avatar = "🪽",
            lastMessage = "시스템: 모집이 마감 임박합니다",
            time = "월",
            unreadCount = 0,
            countText = "2/5명",
            statusText = "마감 D-1",
            stateLabel = "진행중",
            messages = listOf(
                ChatMessage("고요한 두루미 1130", "최소 3명까지 한 분만 더 모이면 출발 가능해요.", "일"),
                ChatMessage("나", "일정 확정되면 알려주세요.", "일", mine = true)
            )
        ),
        ChatThread(
            id = "chat-cheongsong-juwangsan",
            title = "주왕산 & 주산지 힐링 트레킹",
            partner = "숲속여행자",
            avatar = "🌲",
            lastMessage = "숲속여행자: 주산지 물안개 시간에 맞춰 출발해요.",
            time = "09:32",
            unreadCount = 0,
            countText = "2/5명",
            statusText = "마감 D-1",
            stateLabel = "진행중",
            messages = listOf(
                ChatMessage("숲속여행자", "안녕하세요! 좋은 하루 보내세요.", "09:30"),
                ChatMessage("나", "안녕하세요! 잘 부탁드려요.", "09:31", mine = true),
                ChatMessage("숲속여행자", "주산지 물안개 시간에 맞춰 출발해요.", "09:32")
            )
        ),
        ChatThread(
            id = "chat-ulleung-island",
            title = "울릉도 2박 3일 섬 여행",
            partner = "고요한 두루미 1130",
            avatar = "🌊",
            lastMessage = "잔잔한 거북이: 배편 시간 다시 확인했어요",
            time = "방금",
            unreadCount = 0,
            countText = "3/5명",
            statusText = "확정 ✓",
            stateLabel = "확정",
            messages = listOf(
                ChatMessage("잔잔한 거북이 9032", "배편 시간 다시 확인했어요. 멀미약도 챙기면 좋아요.", "방금"),
                ChatMessage("나", "좋아요. 날씨가 바뀌면 실내 동선 먼저 볼게요.", "방금", mine = true)
            )
        ),
        ChatThread(
            id = "chat-andong-dosan",
            title = "안동 도산서원 그늘 코스",
            partner = "초록 여우 5824",
            avatar = "🌿",
            lastMessage = "초록 여우: 그늘 길 위주로 천천히 걸어요",
            time = "어제",
            unreadCount = 0,
            countText = "2/4명",
            statusText = "모집중",
            stateLabel = "진행중",
            messages = listOf(
                ChatMessage("초록 여우 5824", "더운 시간은 피하고 그늘 길 위주로 천천히 걸어요.", "어제"),
                ChatMessage("나", "전통찻집 쉬는 시간이 있으면 좋겠어요.", "어제", mine = true)
            )
        ),
        ChatThread(
            id = "chat-yeongju-buseoksa",
            title = "영주 부석사 눈꽃 산책",
            partner = "느긋한 토끼 7821",
            avatar = "❄️",
            lastMessage = "느긋한 토끼: 눈길이라 이동 시간을 조금 더 잡을게요",
            time = "어제",
            unreadCount = 0,
            countText = "4/5명",
            statusText = "마감 D-2",
            stateLabel = "진행중",
            messages = listOf(
                ChatMessage("느긋한 토끼 7821", "눈길이라 이동 시간을 조금 더 잡을게요.", "어제"),
                ChatMessage("나", "따뜻한 카페도 같이 보면 좋겠어요.", "어제", mine = true)
            )
        ),
        ChatThread(
            id = "chat-ended-andong-spring",
            title = "안동 봄날 고택 산책",
            partner = "초록 여우 5824",
            avatar = "🌿",
            lastMessage = "시스템: 여행 기록이 피드에 저장됐어요",
            time = "4월",
            unreadCount = 0,
            countText = "5/5명",
            statusText = "종료",
            stateLabel = "종료",
            messages = listOf(
                ChatMessage("초록 여우 5824", "고택 골목 사진 정리해서 피드에 올렸어요.", "4월")
            ),
            isReadOnly = true,
            closureReason = "여행이 종료됐어요",
            archiveNotice = "채팅은 14일 동안 읽기 전용으로 보관되고 이후 친구 도감 기록만 남아요.",
            archiveStatus = "보관 D-14"
        ),
        ChatThread(
            id = "chat-ended-ulleung",
            title = "울릉도 2박 3일 섬 여행",
            partner = "고요한 두루미 1130",
            avatar = "🌊",
            lastMessage = "시스템: 도감 친구 3명이 추가됐어요",
            time = "3월",
            unreadCount = 0,
            countText = "5/5명",
            statusText = "종료",
            stateLabel = "종료",
            messages = listOf(
                ChatMessage("고요한 두루미 1130", "다음에는 관음도 일몰도 같이 봐요.", "3월")
            ),
            isReadOnly = true,
            closureReason = "호스트가 모임을 종료했어요",
            archiveNotice = "채팅은 14일 동안 읽기 전용으로 보관되고 이후 친구 도감 기록만 남아요.",
            archiveStatus = "보관 D-13"
        )
    )

    private val sessionTrips = mutableStateListOf<TripRecruitment>()
    private val sessionChatThreads = mutableStateListOf<ChatThread>()
    private val tripOverrides = mutableStateMapOf<String, TripRecruitment>()
    private val chatThreadOverrides = mutableStateMapOf<String, ChatThread>()
    private val appliedTripIds = mutableStateMapOf<String, Boolean>()
    private val ownedTripIds = mutableStateListOf<String>()
    private var sessionTripSequence = 0
    private val sessionFeedPosts = mutableStateListOf<FeedPost>()
    private val feedCommentIncrements = mutableStateMapOf<String, Int>()
    private var sessionFeedPostSequence = 0
    private val authCompletedState = mutableStateOf(false)

    val trips: List<TripRecruitment>
        get() {
            val allTrips = sessionTrips.map { tripOverrides[it.id] ?: it } +
                mockTrips.map { tripOverrides[it.id] ?: it }
            val ownedIds = ownedTripIds.toSet()
            return ownedTripIds.mapNotNull { ownedId -> allTrips.firstOrNull { it.id == ownedId } } +
                allTrips.filterNot { it.id in ownedIds }
        }

    val chatThreads: List<ChatThread>
        get() = sessionChatThreads.map { chatThreadOverrides[it.id] ?: it } +
            mockChatThreads.map { chatThreadOverrides[it.id] ?: it }

    val feedPosts: List<FeedPost>
        get() = (sessionFeedPosts + mockFeedPosts).map { post ->
            val addedComments = feedCommentIncrements[post.id] ?: 0
            if (addedComments == 0) post else post.copy(comments = post.comments + addedComments)
        }

    private val mockTrips = listOf(
        TripRecruitment(
            id = "trip-cheongsong-juwangsan",
            courseId = "cheongsong-juwangsan",
            title = "주왕산 & 주산지 힐링 트레킹",
            scheduleDate = "2026.06.06 (토)",
            scheduleTime = "08:00 - 18:00",
            meetingPoint = "청송 시외버스터미널",
            joined = 2,
            capacity = 5,
            minParticipants = 3,
            ddayLabel = "D-2",
            statusLabel = "모집중",
            host = "숲속여행자",
            hostAvatar = "🌲",
            chatThreadId = "chat-cheongsong-juwangsan"
        ),
        TripRecruitment(
            id = "trip-andong-hahoe",
            courseId = "andong-hahoe",
            title = "안동 하회마을 하루여행",
            scheduleDate = "2026.06.09 (화)",
            scheduleTime = "10:00 - 17:00",
            meetingPoint = "안동터미널 대합실",
            joined = 3,
            capacity = 6,
            minParticipants = 3,
            ddayLabel = "D-5",
            statusLabel = "출발확정",
            host = "초록 여우 5824",
            hostAvatar = "🦊",
            chatThreadId = "chat-andong-hahoe"
        ),
        TripRecruitment(
            id = "trip-gyeongju-night",
            courseId = "gyeongju-healing",
            title = "경주 단풍·야경 1박 2일",
            scheduleDate = "2026.06.05 (금)",
            scheduleTime = "14:00 - 다음 날 12:00",
            meetingPoint = "경주역 2번 출구",
            joined = 5,
            capacity = 6,
            minParticipants = 3,
            ddayLabel = "D-1",
            statusLabel = "마감임박",
            host = "달빛 토끼 6142",
            hostAvatar = "🐰",
            chatThreadId = "chat-gyeongju-fall"
        ),
        TripRecruitment(
            id = "trip-pohang-drive",
            courseId = "pohang-sea",
            title = "포항·영덕 동해 드라이브",
            scheduleDate = "2026.06.15 (월)",
            scheduleTime = "09:30 - 17:30",
            meetingPoint = "포항역 1번 출구",
            joined = 6,
            capacity = 6,
            minParticipants = 3,
            ddayLabel = "D-11",
            statusLabel = "출발확정",
            host = "우직한 곰 7821",
            hostAvatar = "🐻",
            chatThreadId = "chat-pohang-drive"
        ),
        TripRecruitment(
            id = "trip-ulleung-island",
            courseId = "ulleung-island",
            title = "울릉도 2박 3일 섬 여행",
            scheduleDate = "2026.07.12 (일)",
            scheduleTime = "09:00 - 2박 3일",
            meetingPoint = "포항여객선터미널",
            joined = 3,
            capacity = 5,
            minParticipants = 3,
            ddayLabel = "D-38",
            statusLabel = "출발확정",
            host = "잔잔한 거북이 9032",
            hostAvatar = "🐢",
            chatThreadId = "chat-ulleung-island"
        ),
        TripRecruitment(
            id = "trip-mungyeong-saejae",
            courseId = "mungyeong-saejae",
            title = "문경 새재 단풍 트레킹",
            scheduleDate = "2026.10.31 (토)",
            scheduleTime = "09:00 - 16:00",
            meetingPoint = "문경새재 제1주차장",
            joined = 2,
            capacity = 5,
            minParticipants = 3,
            ddayLabel = "D-149",
            statusLabel = "모집중",
            host = "고요한 두루미 1130",
            hostAvatar = "🪽",
            chatThreadId = "chat-mungyeong-fall"
        ),
        TripRecruitment(
            id = "trip-yeongju-buseoksa",
            courseId = "yeongju-buseoksa",
            title = "영주 부석사 눈꽃 산책",
            scheduleDate = "2026.12.14 (월)",
            scheduleTime = "08:00 - 18:00",
            meetingPoint = "영주역",
            joined = 4,
            capacity = 5,
            minParticipants = 3,
            ddayLabel = "D-193",
            statusLabel = "출발확정",
            host = "느긋한 토끼 7821",
            hostAvatar = "🐰",
            chatThreadId = "chat-yeongju-buseoksa"
        ),
        TripRecruitment(
            id = "trip-andong-dosan",
            courseId = "andong-dosan",
            title = "안동 도산서원 그늘 코스",
            scheduleDate = "2026.07.07 (화)",
            scheduleTime = "10:30 - 16:30",
            meetingPoint = "도산서원 주차장",
            joined = 2,
            capacity = 4,
            minParticipants = 3,
            ddayLabel = "D-33",
            statusLabel = "모집중",
            host = "초록 여우 5824",
            hostAvatar = "🦊",
            chatThreadId = "chat-andong-dosan"
        )
    )

    private val mockFeedPosts = listOf(
        FeedPost(
            id = "feed-1",
            author = "숲속여행자",
            avatar = "🐻",
            region = "청송",
            imageEmoji = "🗺️",
            title = "주왕산 & 주산지 힐링 트레킹",
            body = "주왕산 & 주산지 힐링 트레킹 경로가 한눈에 남아서 다음 사람에게도 추천하기 좋았어요.",
            routeSummary = "주왕산 · 용연폭포 · 주산지",
            visibility = FeedVisibility.Friends,
            likes = 128,
            comments = 18
        ),
        FeedPost(
            id = "feed-2",
            author = "토끼여행자",
            avatar = "🐰",
            region = "안동",
            imageEmoji = "🏡",
            title = "안동 하회마을, 잊지 못할 하루",
            body = "고택 골목을 천천히 걷고 부용대에서 내려다본 마을 풍경이 오래 남았어요.",
            routeSummary = "하회마을 · 부용대 · 월영교",
            visibility = FeedVisibility.Friends,
            likes = 128,
            comments = 18
        ),
        FeedPost(
            id = "feed-3",
            author = "고요한 두루미 1130",
            avatar = "🪽",
            region = "경주",
            imageEmoji = "🌙",
            title = "경주 역사 감성 여행은 월정교에서 동궁과 월지로 이어지는 밤 동선이 제일 좋았어요.",
            body = "경주 역사 감성 여행은 월정교에서 동궁과 월지로 이어지는 밤 동선이 제일 좋았어요.",
            routeSummary = "첨성대 · 월정교 · 동궁과 월지",
            visibility = FeedVisibility.Public,
            likes = 56,
            comments = 12
        ),
        FeedPost(
            id = "feed-4",
            author = "달빛 토끼 6142",
            avatar = "🐰",
            region = "포항",
            imageEmoji = "🌉",
            title = "포항 바다와 시장을 한 번에",
            body = "스페이스워크에서 바다를 보고 죽도시장에서 함께 나눈 간식까지 알찼던 하루였어요.",
            routeSummary = "영일대 · 스페이스워크 · 죽도시장",
            visibility = FeedVisibility.Friends,
            likes = 73,
            comments = 9
        ),
        FeedPost(
            id = "feed-5",
            author = "초록 여우 5824",
            avatar = "🦊",
            region = "문경",
            imageEmoji = "🍁",
            title = "문경새재 길은 천천히 걸을수록 좋아요",
            body = "문경새재 길은 급하지 않게 걸을수록 단풍 사이 작은 풍경이 더 잘 보여요.",
            routeSummary = "제1관문 · 조령원터 · 새재길",
            visibility = FeedVisibility.Public,
            likes = 64,
            comments = 7
        ),
        FeedPost(
            id = "feed-6",
            author = "느긋한 토끼 7821",
            avatar = "🐰",
            region = "영주",
            imageEmoji = "❄️",
            title = "부석사 겨울 산책 기록",
            body = "부석사는 짧게 걸어도 겨울 공기와 풍경이 충분해서 무리하지 않는 코스로 좋았어요.",
            routeSummary = "부석사 · 소수서원 · 풍기 카페",
            visibility = FeedVisibility.Private,
            likes = 31,
            comments = 4
        ),
        FeedPost(
            id = "feed-7",
            author = "잔잔한 거북이 9032",
            avatar = "🐢",
            region = "울릉",
            imageEmoji = "🌊",
            title = "울릉도는 천천히 움직여야 보여요",
            body = "하루를 비워 천천히 움직일수록 섬의 속도가 더 잘 느껴졌어요.",
            routeSummary = "도동항 · 행남해안산책로 · 나리분지",
            visibility = FeedVisibility.Friends,
            likes = 89,
            comments = 16
        )
    )

    private val initialProfile = Profile(
        name = "다정한 곰 1001",
        animalBuddy = "초록 고양이 2035",
        region = "경북을 천천히 모으는 중",
        bio = "혼자 떠나도 같이 웃을 수 있는 작은 여행을 좋아해요.",
        badges = listOf("경북 새싹", "야경 수집가", "로컬맛집 탐험"),
        joinedTrips = 12,
        hostedTrips = 3,
        feedCount = 21
    )

    private val profileState = mutableStateOf(initialProfile)

    val profile: Profile
        get() = profileState.value

    val isAuthCompleted: Boolean
        get() = authCompletedState.value

    val dogamVisibility = DogamVisibility.Friends

    val dogamFriends = listOf(
        DogamFriend("dogam-01", "따스한 사슴 3492", "🦌", "2일 전", 2),
        DogamFriend("dogam-02", "우직한 곰 7821", "🐻", "2일 전", 2),
        DogamFriend("dogam-03", "잔잔한 거북이 9032", "🐢", "2일 전", 1),
        DogamFriend("dogam-04", "고요한 두루미 1130", "🪽", "3주 전", 1),
        DogamFriend("dogam-05", "엉뚱한 토끼 4821", "🐰", "3주 전", 1),
        DogamFriend("dogam-06", "호기심 너구리 2035", "🦝", "6주 전", 1),
        DogamFriend("dogam-07", "나른한 사슴 7158", "🦌", "8주 전", 1),
        DogamFriend("dogam-08", "느긋한 곰 2401", "🐻", "8주 전", 2),
        DogamFriend("dogam-09", "평온한 거북이 9032", "🐢", "12주 전", 2),
        DogamFriend("dogam-10", "청아한 두루미 6810", "🪽", "14주 전", 1),
        DogamFriend("dogam-11", "깡총 토끼 6142", "🐰", "14주 전", 1),
        DogamFriend("dogam-12", "말많은 너구리 3904", "🦝", "20주 전", 1)
    )

    fun findCourse(id: String): TripCourse = courses.firstOrNull { it.id == canonicalCourseId(id) } ?: courses.first()

    fun findThread(id: String): ChatThread =
        chatThreads.firstOrNull { it.id == canonicalChatThreadId(id) } ?: chatThreads.first()

    fun findTrip(id: String): TripRecruitment = trips.firstOrNull { it.id == id }
        ?: trips.firstOrNull { it.id == canonicalTripId(id) }
        ?: trips.firstOrNull { it.courseId == canonicalCourseId(id) }
        ?: trips.first()

    fun findTripForCourse(courseId: String): TripRecruitment =
        trips.firstOrNull { it.courseId == canonicalCourseId(courseId) } ?: trips.first()

    fun tripIdForCourse(courseId: String): String = findTripForCourse(courseId).id

    fun findCourseForTrip(trip: TripRecruitment): TripCourse = findCourse(trip.courseId)

    fun isAppliedToTrip(tripId: String): Boolean = appliedTripIds[tripId] == true

    fun applyToTrip(tripId: String) {
        val trip = findTrip(tripId)
        val wasAlreadyApplied = appliedTripIds[trip.id] == true
        appliedTripIds[trip.id] = true
        promoteOwnedTrip(trip.id)

        if (!wasAlreadyApplied) {
            val nextJoined = (trip.joined + 1).coerceAtMost(trip.capacity)
            val updatedTrip = trip.copy(
                joined = nextJoined,
                statusLabel = trip.statusLabelFor(nextJoined)
            )
            tripOverrides[trip.id] = updatedTrip
            profileState.value = profile.copy(joinedTrips = profile.joinedTrips + 1)
            updateTripChatThread(updatedTrip)
        } else {
            updateTripChatThread(trip)
        }
    }

    fun approveHostApplicant(tripId: String, applicantName: String) {
        val trip = findTrip(tripId)
        if (trip.statusLabel == "모집취소") return

        val nextJoined = (trip.joined + 1).coerceAtMost(trip.capacity)
        val updatedTrip = trip.copy(
            joined = nextJoined,
            statusLabel = trip.statusLabelFor(nextJoined)
        )
        tripOverrides[trip.id] = updatedTrip
        promoteOwnedTrip(trip.id)
        updateTripChatThreadWithNotice(
            trip = updatedTrip,
            body = "${applicantName}님이 참여 확정됐어요."
        )
    }

    fun rejectHostApplicant(tripId: String, applicantName: String) {
        val trip = findTrip(tripId)
        promoteOwnedTrip(trip.id)
        updateTripChatThreadWithNotice(
            trip = trip,
            body = "${applicantName}님의 신청을 거절했어요."
        )
    }

    fun setRecruitmentClosed(tripId: String, isClosed: Boolean) {
        val trip = findTrip(tripId)
        val updatedTrip = trip.copy(
            statusLabel = if (isClosed) "모집취소" else trip.statusLabelFor(trip.joined)
        )
        tripOverrides[trip.id] = updatedTrip
        promoteOwnedTrip(trip.id)
        updateTripChatThreadWithNotice(
            trip = updatedTrip,
            body = if (isClosed) "호스트가 모집을 취소했어요." else "호스트가 모집을 다시 열었어요."
        )
    }

    fun createFeedPost(
        courseId: String,
        title: String,
        story: String,
        visibility: FeedVisibility = FeedVisibility.Friends
    ): FeedPost {
        val course = findCourse(courseId)
        val postTitle = title.trim().ifEmpty { course.title }
        val post = FeedPost(
            id = "session-feed-${++sessionFeedPostSequence}",
            author = profile.name,
            avatar = "🐻",
            region = course.region,
            imageEmoji = course.imageEmoji,
            title = postTitle,
            body = story.trim().ifEmpty { course.recruitmentNote },
            routeSummary = course.stops.take(3).joinToString(" · "),
            visibility = visibility,
            likes = 0,
            comments = 0,
            photoCountText = "1/3"
        )
        sessionFeedPosts.add(0, post)
        profileState.value = profile.copy(feedCount = profile.feedCount + 1)
        return post
    }

    fun findFeedPost(id: String): FeedPost =
        feedPosts.firstOrNull { it.id == canonicalFeedPostId(id) } ?: feedPosts.first()

    fun addFeedComment(postId: String) {
        val canonicalPostId = canonicalFeedPostId(postId)
        feedCommentIncrements[canonicalPostId] = (feedCommentIncrements[canonicalPostId] ?: 0) + 1
    }

    fun completeAuthFlow() {
        authCompletedState.value = true
    }

    fun appendChatMessage(threadId: String, text: String): ChatMessage {
        val trimmed = text.trim()
        val message = ChatMessage("나", trimmed, "지금", mine = true)
        val canonicalThreadId = canonicalChatThreadId(threadId)
        val thread = findThread(canonicalThreadId)
        val updated = thread.copy(
            lastMessage = "나: $trimmed",
            time = "지금",
            unreadCount = 0,
            messages = thread.messages + message
        )
        chatThreadOverrides[canonicalThreadId] = updated
        return message
    }

    fun resetSessionFeedPostsForTests() {
        sessionFeedPosts.clear()
        sessionFeedPostSequence = 0
        feedCommentIncrements.clear()
        sessionTrips.clear()
        sessionChatThreads.clear()
        tripOverrides.clear()
        chatThreadOverrides.clear()
        appliedTripIds.clear()
        ownedTripIds.clear()
        sessionTripSequence = 0
        profileState.value = initialProfile
        authCompletedState.value = false
    }

    fun createRecruitment(
        courseId: String,
        scheduleDate: String? = null,
        scheduleTime: String? = null,
        meetingPoint: String? = null,
        capacity: Int? = null,
        note: String? = null
    ): TripRecruitment {
        val course = findCourse(courseId)
        val sequence = ++sessionTripSequence
        val tripId = "session-trip-$sequence"
        val chatId = "session-chat-$sequence"
        val safeCapacity = capacity
            ?.coerceAtLeast(course.minParticipants)
            ?.coerceAtMost(12)
            ?: course.capacity
        val safeScheduleDate = scheduleDate?.trim()?.takeIf { it.isNotEmpty() } ?: course.startLabel
        val safeScheduleTime = scheduleTime?.trim()?.takeIf { it.isNotEmpty() } ?: if (course.duration == "2박 3일") {
            "09:00 - 2박 3일"
        } else {
            "08:00 - 18:00"
        }
        val safeMeetingPoint = meetingPoint?.trim()?.takeIf { it.isNotEmpty() } ?: course.meetingPoint
        val safeNote = note?.trim()?.takeIf { it.isNotEmpty() } ?: course.recruitmentNote
        val trip = TripRecruitment(
            id = tripId,
            courseId = course.id,
            title = course.title,
            scheduleDate = safeScheduleDate,
            scheduleTime = safeScheduleTime,
            meetingPoint = safeMeetingPoint,
            joined = 1,
            capacity = safeCapacity,
            minParticipants = course.minParticipants,
            ddayLabel = "방금 생성",
            statusLabel = "모집중",
            host = profile.name,
            hostAvatar = "🐻",
            chatThreadId = chatId
        )
        val chat = ChatThread(
            id = chatId,
            title = course.title,
            partner = profile.name,
            avatar = "🐻",
            lastMessage = "시스템: 모집이 만들어졌어요. 함께 갈 사람을 기다려요.",
            time = "방금",
            unreadCount = 0,
            countText = "1/${safeCapacity}명",
            statusText = "모집중",
            stateLabel = "진행중",
            messages = listOf(
                ChatMessage("시스템", "모집이 만들어졌어요. 함께 갈 사람을 기다려요.", "방금"),
                ChatMessage("나", safeNote, "방금", mine = true)
            ),
            tripId = tripId
        )
        sessionTrips.add(0, trip)
        sessionChatThreads.add(0, chat)
        promoteOwnedTrip(trip.id)
        profileState.value = profile.copy(hostedTrips = profile.hostedTrips + 1)
        return trip
    }

    fun chatThreadIdForTrip(tripId: String): String = findTrip(tripId).chatThreadId

    fun chatThreadIdForCourse(courseId: String): String = findTripForCourse(courseId).chatThreadId

    private fun canonicalCourseId(id: String): String = when (val normalized = id.removePrefix("course-")) {
        "gyeongju-history" -> "gyeongju-healing"
        "pohang-drive" -> "pohang-sea"
        else -> normalized
    }

    private fun canonicalTripId(id: String): String = when (id) {
        "trip-gyeongju-history" -> "trip-gyeongju-night"
        else -> id
    }

    private fun canonicalChatThreadId(id: String): String = when (id) {
        "chat-gyeongju-night" -> "chat-gyeongju-fall"
        "chat-juwangsan" -> "chat-cheongsong-juwangsan"
        "chat-andong-hanok" -> "chat-andong-hahoe"
        else -> id
    }

    private fun canonicalFeedPostId(id: String): String = when {
        id.matches(Regex("feed-0[1-9]")) -> "feed-${id.removePrefix("feed-0")}"
        else -> id
    }

    private fun promoteOwnedTrip(tripId: String) {
        ownedTripIds.remove(tripId)
        ownedTripIds.add(0, tripId)
    }

    private fun updateTripChatThread(trip: TripRecruitment) {
        val thread = findThread(trip.chatThreadId)
        chatThreadOverrides[thread.id] = thread.copy(
            countText = "${trip.joined}/${trip.capacity}명",
            statusText = trip.statusLabel,
            stateLabel = "진행중"
        )
    }

    private fun updateTripChatThreadWithNotice(trip: TripRecruitment, body: String) {
        val thread = findThread(trip.chatThreadId)
        val notice = ChatMessage("시스템", body, "지금")
        chatThreadOverrides[thread.id] = thread.copy(
            lastMessage = "시스템: $body",
            time = "지금",
            unreadCount = 0,
            countText = "${trip.joined}/${trip.capacity}명",
            statusText = trip.statusLabel,
            stateLabel = "진행중",
            messages = thread.messages + notice
        )
    }

    private fun TripRecruitment.statusLabelFor(joinedCount: Int): String = when {
        joinedCount >= capacity -> "마감"
        joinedCount >= minParticipants -> "출발확정"
        else -> "모집중"
    }
}
