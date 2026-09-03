package kr.hanchae.moyeotrip.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import kr.hanchae.moyeotrip.data.courses.TravelCourse
import kr.hanchae.moyeotrip.data.feed.ServerFeed
import kr.hanchae.moyeotrip.data.profile.ServerPublicProfile
import kr.hanchae.moyeotrip.data.profile.ServerUserProfile
import kr.hanchae.moyeotrip.data.rooms.ChatRoomSearchResult
import kr.hanchae.moyeotrip.data.rooms.MyChatRoom
import kr.hanchae.moyeotrip.data.rooms.MyWaitingRoom
import kr.hanchae.moyeotrip.data.rooms.RoomTag
import kr.hanchae.moyeotrip.data.weather.GyeongbukWeather
import kr.hanchae.moyeotrip.ui.components.ServerListState
import kr.hanchae.moyeotrip.ui.screens.FeedTimelineTab
import kr.hanchae.moyeotrip.ui.screens.MeetingChatTab
import kr.hanchae.moyeotrip.ui.screens.MyTripTab

/**
 * 하단 탭이 보던 데이터를 **탭 화면 바깥**에 보관한다 (`docs/alignment/TAB-STATE-CANON.md` R1).
 *
 * 탭을 떠나면 그 화면의 컴포저블이 파괴돼 `remember` 로 든 상태가 사라진다. 네비게이션의
 * `saveState`/`restoreState` 는 백스택 항목만 복원할 뿐 `remember` 는 복원하지 않는다.
 * 그래서 돌아올 때마다 재조회가 나가고 `불러오는 중이에요…` 와 기본 썸네일이 다시 보였다.
 *
 * 담기는 값은 **서버에서 받은 것뿐이다.** 캐시를 지어낸 값으로 채우지 않는다(`NO-MOCK-CANON.md` R1).
 * 로그아웃·계정 전환 시 [clear] 로 비운다 — 다른 사용자의 데이터가 남으면 안 된다(R4).
 */
internal class TabDataStore {
    val home = HomeTabState()
    val explore = ExploreTabState()
    val meetings = MeetingsTabState()
    val feed = FeedTabState()
    val my = MyTabState()

    fun clear() {
        home.clear()
        explore.clear()
        meetings.clear()
        feed.clear()
        my.clear()
    }
}

/**
 * 탭이 공통으로 쓰는 조회 스위치.
 *
 * [loaded] 는 "**성공해서 보여줄 목록을 갖고 있는가**"다. 두 곳에 쓰인다 —
 * 로딩 문구를 띄울지(캐시가 없을 때만, R2), 그리고 재진입에 다시 부를지(R3).
 * [reload] 는 사용자가 **다시 시도**를 눌렀을 때만 부른다(R4).
 */
internal abstract class ReloadableTabState {
    var loaded by mutableStateOf(false)
        private set
    var reloadKey by mutableIntStateOf(0)
        private set

    /**
     * 조회를 마쳤다고 기록한다. **성공했을 때만** 부른다(정본 R3-1).
     *
     * 실패까지 기록하면 보여줄 데이터가 없는데도 재조회가 막혀, 탭을 몇 번을 오가도 같은 오류
     * 화면만 보게 된다. 탭을 다시 여는 행위 자체가 "이제 되나 보자"는 뜻이다.
     */
    fun markLoaded() {
        loaded = true
    }

    fun reload() {
        loaded = false
        reloadKey++
    }

    protected fun resetLoadState() {
        loaded = false
        reloadKey++
    }
}

/** 홈(01) — 가진 것을 그리며 뒤에서 갱신한다. 갱신 중 로딩 문구로 덮지 않는다(R3). */
internal class HomeTabState : ReloadableTabState() {
    var weather by mutableStateOf<GyeongbukWeather?>(null)
    var recommended by mutableStateOf<ServerListState<TravelCourse>>(ServerListState.Loading)
    var popular by mutableStateOf<ServerListState<TravelCourse>>(ServerListState.Loading)

    fun clear() {
        weather = null
        recommended = ServerListState.Loading
        popular = ServerListState.Loading
        resetLoadState()
    }
}

/** 탐색(11) — 목록·고른 필터·찜 상태를 보던 대로 유지한다(R3). */
internal class ExploreTabState : ReloadableTabState() {
    var rooms by mutableStateOf<ServerListState<ChatRoomSearchResult>>(ServerListState.Loading)
    var mapRooms by mutableStateOf<List<ChatRoomSearchResult>>(emptyList())

    /**
     * 지도 조회가 `400 40040 INVALID_MAP_SEARCH_AREA` 로 막혔을 때 서버가 준 문구.
     *
     * 위경도 범위 초과와 `radiusKm` 상한(200km) 초과가 같은 코드다. 이걸 삼키면 핀이 없는 지도와
     * 구분되지 않아 "모임이 없다"로 잘못 읽힌다 — 지도 위에 그대로 보여준다.
     */
    var mapAreaError by mutableStateOf<String?>(null)
    var tags by mutableStateOf<List<RoomTag>>(emptyList())
    var selectedTagId by mutableStateOf<Long?>(null)
    var favoriteRoomIds by mutableStateOf<Set<Long>>(emptySet())

    fun clear() {
        rooms = ServerListState.Loading
        mapRooms = emptyList()
        mapAreaError = null
        tags = emptyList()
        selectedTagId = null
        favoriteRoomIds = emptySet()
        resetLoadState()
    }
}

/** 모임(19) — 목록과 고른 세그먼트를 유지한다(R3). */
internal class MeetingsTabState : ReloadableTabState() {
    var selectedTab by mutableStateOf(MeetingChatTab.Active)
    var rooms by mutableStateOf<ServerListState<MyChatRoom>>(ServerListState.Loading)
    var waiting by mutableStateOf<List<MyWaitingRoom>>(emptyList())

    fun clear() {
        selectedTab = MeetingChatTab.Active
        rooms = ServerListState.Loading
        waiting = emptyList()
        resetLoadState()
    }
}

/**
 * 피드(22) — 탭(팔로잉·발견)마다 따로 담는다.
 * 탭을 오갈 때도 이미 받아 둔 목록을 다시 부르지 않는다(R3).
 */
internal class FeedTabState {
    var selectedTab by mutableStateOf(FeedTimelineTab.Discover)
    private val byTab = mutableStateMapOf<FeedTimelineTab, ServerListState<ServerFeed>>()

    // **성공해서 보여줄 목록이 있는** 탭. 미로그인 상태로 그린 빈 목록도, 실패도 여기 들어가지 않는다 —
    // 넣어 버리면 보여줄 게 없는데도 재조회가 막혀 탭을 오갈 때마다 같은 화면만 보게 된다.
    private val loadedTabs = mutableStateMapOf<FeedTimelineTab, Boolean>()
    var reloadKey by mutableIntStateOf(0)
        private set

    fun state(tab: FeedTimelineTab): ServerListState<ServerFeed> = byTab[tab] ?: ServerListState.Loading

    fun isLoaded(tab: FeedTimelineTab): Boolean = loadedTabs[tab] == true

    /** 서버 응답이 아닌 표시용 상태(미로그인 빈 목록·로딩)를 그린다. 다시 부를 여지를 남긴다. */
    fun show(tab: FeedTimelineTab, state: ServerListState<ServerFeed>) {
        byTab[tab] = state
    }

    /**
     * 서버 조회 결과를 담는다. **성공(`Loaded`)일 때만** 받아 뒀다고 기록한다 —
     * 실패는 재진입할 때 다시 부른다(정본 R3-1).
     */
    fun putFromServer(tab: FeedTimelineTab, state: ServerListState<ServerFeed>) {
        byTab[tab] = state
        if (state is ServerListState.Loaded) loadedTabs[tab] = true else loadedTabs.remove(tab)
    }

    fun reload(tab: FeedTimelineTab) {
        loadedTabs.remove(tab)
        reloadKey++
    }

    fun clear() {
        selectedTab = FeedTimelineTab.Discover
        byTab.clear()
        loadedTabs.clear()
        reloadKey++
    }
}

/** 마이(26) — 홈과 같이 가진 것을 그리며 뒤에서 갱신한다(R3). */
internal class MyTabState : ReloadableTabState() {
    var selectedTab by mutableStateOf(MyTripTab.Ongoing)
    var profile by mutableStateOf<ServerUserProfile?>(null)

    /**
     * 26 상단 3칸 지표(여행·매너·피드)의 근거 — `GET users/{myUserId}/profile`.
     * `GET users/me/profile` 응답에는 이 셋이 없다.
     */
    var publicProfile by mutableStateOf<ServerPublicProfile?>(null)
    var rooms by mutableStateOf<ServerListState<MyChatRoom>>(ServerListState.Loading)
    var dexCount by mutableStateOf<Int?>(null)

    fun clear() {
        selectedTab = MyTripTab.Ongoing
        profile = null
        publicProfile = null
        rooms = ServerListState.Loading
        dexCount = null
        resetLoadState()
    }
}

/**
 * 탭 보관소는 `NavHost` 보다 위에서 만들어 내려준다.
 * 기본값은 빈 보관소라 미리보기·캡처에서도 화면이 그대로 그려진다 — 값은 서버에서만 채워진다.
 */
internal val LocalTabDataStore = staticCompositionLocalOf { TabDataStore() }
