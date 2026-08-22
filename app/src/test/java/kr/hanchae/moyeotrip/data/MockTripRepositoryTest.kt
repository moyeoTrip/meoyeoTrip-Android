package kr.hanchae.moyeotrip.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MockTripRepositoryTest {
    @Before
    fun resetRepositorySessionState() {
        MockTripRepository.resetSessionFeedPostsForTests()
    }

    @Test
    fun coursesHaveUniqueIdsAndValidCapacity() {
        val courses = MockTripRepository.courses

        assertEquals(courses.size, courses.map { it.id }.toSet().size)
        assertTrue(courses.all { it.participants in 0..it.capacity })
        assertTrue(courses.all { it.minParticipants == 3 })
        assertTrue(courses.all { it.stops.size >= 3 })
    }

    @Test
    fun lookupReturnsRequestedCourseOrSafeFallback() {
        val requested = MockTripRepository.findCourse("gyeongju-healing")
        val fallback = MockTripRepository.findCourse("missing-course")

        assertEquals("경주 감성 힐링 코스", requested.title)
        assertEquals(MockTripRepository.courses.first(), fallback)
    }

    @Test
    fun feedAndChatMockDataAreLinkedEnoughForUiFlows() {
        assertTrue(MockTripRepository.feedPosts.isNotEmpty())
        assertTrue(MockTripRepository.trips.isNotEmpty())
        assertTrue(MockTripRepository.chatThreads.isNotEmpty())
        assertNotNull(MockTripRepository.profile)
        assertEquals("숲속여행자", MockTripRepository.feedPosts.first().author)
        assertEquals("주왕산 & 주산지 힐링 트레킹", MockTripRepository.feedPosts.first().title)
        assertEquals("가을 경주 야경 같이 봐요", MockTripRepository.chatThreads.first().title)
        assertTrue(
            MockTripRepository.trips.all { trip ->
                MockTripRepository.findCourse(trip.courseId).id == trip.courseId
            }
        )
        assertTrue(
            MockTripRepository.trips.all { trip ->
                MockTripRepository.findThread(trip.chatThreadId).id == trip.chatThreadId
            }
        )
        assertTrue(MockTripRepository.chatThreads.all { it.messages.isNotEmpty() })
        assertTrue(MockTripRepository.chatThreads.all { it.countText.contains("/") })
        assertTrue(
            MockTripRepository.chatThreads
                .filter { it.isReadOnly }
                .all { thread ->
                    thread.closureReason != null &&
                        thread.archiveNotice?.contains("14일") == true &&
                        thread.archiveNotice.contains("친구 도감")
                }
        )
        assertTrue(MockTripRepository.feedPosts.all { it.routeSummary.contains("·") })
    }

    @Test
    fun coursePublisherAndDiscoverFeedMatchPlanningContent() {
        val publisher = MockTripRepository.findCourse("cheongsong-juwangsan").publisher
        val discoverPosts = MockTripRepository.feedPosts.filter { it.visibility != FeedVisibility.Private }

        assertNotNull(publisher)
        assertEquals("숲속여행자", publisher?.name)
        assertEquals("2026.05.25 여행 후 공개", publisher?.publishedAfterTrip)
        assertEquals(3, publisher?.recruitmentCount)
        assertEquals(6, discoverPosts.size)
        assertEquals(
            listOf(
                "주왕산 & 주산지 힐링 트레킹",
                "안동 하회마을, 잊지 못할 하루",
                "경주 역사 감성 여행",
                "포항 바다와 시장을 한 번에",
                "문경새재 길은 천천히 걸을수록 좋아요",
                "울릉도는 천천히 움직여야 보여요"
            ),
            discoverPosts.map { it.title }
        )
    }

    @Test
    fun tripLookupUsesRecruitmentIdsInsteadOfCourseIds() {
        val gyeongjuTrip = MockTripRepository.findTripForCourse("gyeongju-healing")

        assertEquals("trip-gyeongju-night", gyeongjuTrip.id)
        assertEquals(gyeongjuTrip.id, MockTripRepository.tripIdForCourse("gyeongju-healing"))
        assertEquals("chat-gyeongju-fall", MockTripRepository.chatThreadIdForTrip(gyeongjuTrip.id))
        assertEquals(
            "chat-cheongsong-juwangsan",
            MockTripRepository.chatThreadIdForTrip("trip-cheongsong-juwangsan")
        )
        assertEquals("chat-andong-hahoe", MockTripRepository.chatThreadIdForTrip("trip-andong-hahoe"))
        assertEquals(
            "30대끼리 느긋하게 힐링 여행가요~",
            MockTripRepository.findThread("chat-cheongsong-juwangsan").title
        )
        assertEquals(
            "trip-cheongsong-juwangsan",
            MockTripRepository.findThread("chat-cheongsong-juwangsan").tripId
        )
        assertEquals("한옥에서 하룻밤 어때요", MockTripRepository.findThread("chat-andong-hahoe").title)
    }

    @Test
    fun platformAliasIdsResolveToCanonicalMockData() {
        assertEquals("gyeongju-healing", MockTripRepository.findCourse("course-gyeongju-history").id)
        assertEquals("pohang-sea", MockTripRepository.findCourse("course-pohang-drive").id)
        assertEquals("trip-gyeongju-night", MockTripRepository.findTrip("trip-gyeongju-history").id)
        assertEquals("chat-gyeongju-fall", MockTripRepository.findThread("chat-gyeongju-night").id)
        assertEquals("chat-cheongsong-juwangsan", MockTripRepository.findThread("chat-juwangsan").id)
        assertEquals("chat-andong-hahoe", MockTripRepository.findThread("chat-andong-hanok").id)
        assertEquals("feed-1", MockTripRepository.findFeedPost("feed-01").id)

        val initialComments = MockTripRepository.findFeedPost("feed-1").comments
        MockTripRepository.addFeedComment("feed-01")
        assertEquals(initialComments + 1, MockTripRepository.findFeedPost("feed-1").comments)

        MockTripRepository.appendChatMessage("chat-juwangsan", "주왕산 준비물 확인했어요")
        assertEquals(
            "나: 주왕산 준비물 확인했어요",
            MockTripRepository.findThread("chat-cheongsong-juwangsan").lastMessage
        )
    }

    @Test
    fun recruitmentDatesAndStatusesStayBelievableForCurrentDemoWindow() {
        val trips = MockTripRepository.trips

        assertTrue(trips.all { it.scheduleDate.contains("2026") })
        assertTrue(
            trips
                .filter { it.statusLabel == "출발확정" }
                .all { it.joined >= it.minParticipants }
        )
        assertTrue(
            trips
                .filter { it.statusLabel == "마감" }
                .all { it.joined >= it.capacity }
        )
        assertTrue(trips.none { it.scheduleDate.contains("2024") || it.scheduleDate.contains("2023") })
    }

    @Test
    fun visibleTripCountsStayAlignedWithPlanningData() {
        val hahoe = MockTripRepository.findTrip("trip-andong-hahoe")
        val dosan = MockTripRepository.findTrip("trip-andong-dosan")
        val hahoeThread = MockTripRepository.findThread(hahoe.chatThreadId)
        val dosanThread = MockTripRepository.findThread(dosan.chatThreadId)

        assertEquals("3/6명", "${hahoe.joined}/${hahoe.capacity}명")
        assertEquals("3/6명", hahoeThread.countText)
        assertEquals("2/4명", "${dosan.joined}/${dosan.capacity}명")
        assertEquals("2/4명", dosanThread.countText)
        assertEquals("모집중", dosan.statusLabel)
        assertEquals("모집중", dosanThread.statusText)
    }

    @Test
    fun creatingFeedPostPrependsSessionPostAndKeepsDetailLookupInSync() {
        MockTripRepository.resetSessionFeedPostsForTests()

        try {
            val initialFeedCount = MockTripRepository.profile.feedCount
            val post = MockTripRepository.createFeedPost(
                courseId = "cheongsong-juwangsan",
                title = "첫 반패키지 단풍 여행",
                story = "새로 남긴 여행 기록"
            )

            assertEquals(post, MockTripRepository.feedPosts.first())
            assertEquals(post, MockTripRepository.findFeedPost(post.id))
            assertEquals("첫 반패키지 단풍 여행", post.title)
            assertEquals("새로 남긴 여행 기록", post.body)
            assertEquals("주왕산국립공원 · 용연폭포 · 주산지", post.routeSummary)
            assertEquals(0, post.likes)
            assertEquals(0, post.comments)
            assertEquals(initialFeedCount + 1, MockTripRepository.profile.feedCount)
        } finally {
            MockTripRepository.resetSessionFeedPostsForTests()
        }
    }

    @Test
    fun creatingRecruitmentAddsSessionTripChatThreadAndHostedProfileState() {
        MockTripRepository.resetSessionFeedPostsForTests()

        try {
            val initialHostedCount = MockTripRepository.profile.hostedTrips
            val trip = MockTripRepository.createRecruitment("andong-hahoe")
            val secondTrip = MockTripRepository.createRecruitment("gyeongju-healing")

            assertEquals(secondTrip, MockTripRepository.trips.first())
            assertEquals(trip, MockTripRepository.trips[1])
            assertEquals(trip.id, MockTripRepository.tripIdForCourse("andong-hahoe"))
            assertEquals(trip.chatThreadId, MockTripRepository.chatThreadIdForTrip(trip.id))
            assertEquals("사진 좋아하는 분들과 하회마을 걸어요", MockTripRepository.findThread(trip.chatThreadId).title)
            assertTrue(MockTripRepository.findThread(trip.chatThreadId).messages.isNotEmpty())
            assertEquals(2, listOf(trip.id, secondTrip.id).toSet().size)
            assertEquals(2, listOf(trip.chatThreadId, secondTrip.chatThreadId).toSet().size)
            assertEquals(initialHostedCount + 2, MockTripRepository.profile.hostedTrips)
        } finally {
            MockTripRepository.resetSessionFeedPostsForTests()
        }
    }

    @Test
    fun creatingRecruitmentPreservesEditedScheduleCapacityAndNote() {
        MockTripRepository.resetSessionFeedPostsForTests()

        try {
            val trip = MockTripRepository.createRecruitment(
                courseId = "gyeongju-healing",
                scheduleDate = "2026.06.20 (토)",
                scheduleTime = "15:00 - 다음 날 11:00",
                meetingPoint = "경주역 관광안내소",
                capacity = 4,
                note = "야간 산책 후 카페에서 쉬어가요."
            )
            val thread = MockTripRepository.findThread(trip.chatThreadId)

            assertEquals("2026.06.20 (토)", trip.scheduleDate)
            assertEquals("15:00 - 다음 날 11:00", trip.scheduleTime)
            assertEquals("경주역 관광안내소", trip.meetingPoint)
            assertEquals(4, trip.capacity)
            assertEquals("1/4명", thread.countText)
            assertTrue(thread.messages.last().text.contains("야간 산책"))
        } finally {
            MockTripRepository.resetSessionFeedPostsForTests()
        }
    }

    @Test
    fun applyingToTripCreatesOnePendingApplicationWithoutOpeningChat() {
        MockTripRepository.resetSessionFeedPostsForTests()

        try {
            val trip = MockTripRepository.findTrip("trip-yeongju-buseoksa")
            MockTripRepository.applyToTrip(trip.id)
            MockTripRepository.applyToTrip(trip.id)

            val updatedTrip = MockTripRepository.findTrip(trip.id)
            val application = MockTripRepository.applicationForTrip(trip.id)

            assertEquals(trip.joined, updatedTrip.joined)
            assertEquals(trip.statusLabel, updatedTrip.statusLabel)
            assertTrue(MockTripRepository.isAppliedToTrip(trip.id))
            assertEquals(1, MockTripRepository.applications.count { it.tripId == trip.id })
            assertEquals(TripApplicationStatus.PendingApproval, application?.status)
        } finally {
            MockTripRepository.resetSessionFeedPostsForTests()
        }
    }

    @Test
    fun hostApprovalAndRecruitmentClosePropagateToTripAndChatState() {
        MockTripRepository.resetSessionFeedPostsForTests()

        try {
            val trip = MockTripRepository.findTrip("trip-cheongsong-juwangsan")
            val initialThread = MockTripRepository.findThread(trip.chatThreadId)

            MockTripRepository.approveHostApplicant(trip.id, "따스한 사슴 3492")

            val approvedTrip = MockTripRepository.findTrip(trip.id)
            val approvedThread = MockTripRepository.findThread(trip.chatThreadId)

            assertEquals(trip.joined + 1, approvedTrip.joined)
            assertEquals("출발확정", approvedTrip.statusLabel)
            assertEquals("${approvedTrip.joined}/${approvedTrip.capacity}명", approvedThread.countText)
            assertEquals("출발확정", approvedThread.statusText)
            assertEquals(initialThread.avatar, approvedThread.avatar)
            assertTrue(approvedThread.lastMessage.contains("참여 확정"))
            assertTrue(approvedThread.messages.last().text.contains("따스한 사슴 3492"))

            MockTripRepository.setRecruitmentClosed(trip.id, true)

            val closedTrip = MockTripRepository.findTrip(trip.id)
            val closedThread = MockTripRepository.findThread(trip.chatThreadId)

            assertEquals("모집취소", closedTrip.statusLabel)
            assertEquals("모집취소", closedThread.statusText)
            assertTrue(closedThread.lastMessage.contains("모집을 취소"))

            MockTripRepository.setRecruitmentClosed(trip.id, false)

            val reopenedTrip = MockTripRepository.findTrip(trip.id)
            val reopenedThread = MockTripRepository.findThread(trip.chatThreadId)

            assertEquals("출발확정", reopenedTrip.statusLabel)
            assertEquals("출발확정", reopenedThread.statusText)
            assertTrue(reopenedThread.lastMessage.contains("다시 열"))
        } finally {
            MockTripRepository.resetSessionFeedPostsForTests()
        }
    }
}
