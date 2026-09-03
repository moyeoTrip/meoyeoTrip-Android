package kr.hanchae.moyeotrip.ui.state

import kr.hanchae.moyeotrip.ui.components.ServerListState
import kr.hanchae.moyeotrip.ui.screens.FeedTimelineTab
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * "재진입 시 다시 부르지 않는다"는 **성공해서 보여줄 게 있을 때** 이야기다(정본 R3-1).
 * 실패까지 받아 본 것으로 치면 탭을 몇 번을 오가도 같은 오류 화면만 보게 된다.
 */
class TabDataStoreTest {
    @Test
    fun failedFeedTabIsNotTreatedAsLoaded() {
        val state = FeedTabState()

        state.putFromServer(FeedTimelineTab.Discover, ServerListState.Failed)

        assertFalse(state.isLoaded(FeedTimelineTab.Discover))
    }

    @Test
    fun successfulFeedTabIsLoadedEvenWhenEmpty() {
        val state = FeedTabState()

        state.putFromServer(FeedTimelineTab.Discover, ServerListState.Loaded(emptyList()))

        assertTrue(state.isLoaded(FeedTimelineTab.Discover))
    }

    /** 미로그인 상태로 그린 빈 목록은 캐시가 아니다 — 로그인한 뒤 다시 불러야 한다. */
    @Test
    fun displayOnlyStateDoesNotCountAsLoaded() {
        val state = FeedTabState()

        state.show(FeedTimelineTab.Discover, ServerListState.Loaded(emptyList()))

        assertFalse(state.isLoaded(FeedTimelineTab.Discover))
    }

    @Test
    fun laterFailureClearsAnEarlierSuccess() {
        val state = FeedTabState()
        state.putFromServer(FeedTimelineTab.Following, ServerListState.Loaded(emptyList()))

        state.putFromServer(FeedTimelineTab.Following, ServerListState.Failed)

        assertFalse(state.isLoaded(FeedTimelineTab.Following))
    }

    @Test
    fun clearingStoreDropsEveryTabCache() {
        val store = TabDataStore()
        store.feed.putFromServer(FeedTimelineTab.Discover, ServerListState.Loaded(emptyList()))
        store.explore.markLoaded()
        store.meetings.markLoaded()

        store.clear()

        assertFalse(store.feed.isLoaded(FeedTimelineTab.Discover))
        assertFalse(store.explore.loaded)
        assertFalse(store.meetings.loaded)
    }
}
