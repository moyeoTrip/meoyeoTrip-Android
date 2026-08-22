package kr.hanchae.moyeotrip.data

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecruitmentChangeLogTest {
    @Before
    fun setUp() = MockTripRepository.resetSessionFeedPostsForTests()

    @After
    fun tearDown() = MockTripRepository.resetSessionFeedPostsForTests()

    @Test
    fun customDraftPersistsSourceScheduleMeetingCoordinatesAndRoute() {
        val original = MockTripRepository.beginRecruitmentDraft("cheongsong-juwangsan")
        val location = original.meetingLocation.copy(
            name = "청송 시외버스터미널",
            detail = "정문 앞",
            latitude = 36.435612,
            longitude = 129.057214,
            meetingTime = "07:50"
        )
        val draft = original.copy(
            courseSource = CourseSource.Custom,
            scheduleType = TripScheduleType.DayTrip,
            meetingLocation = location
        )

        MockTripRepository.updateRecruitmentDraft(draft)
        val trip = MockTripRepository.createRecruitmentFromDraft(draft.id)

        assertEquals(CourseSource.Custom, trip.courseSource)
        assertEquals(TripScheduleType.DayTrip, trip.scheduleType)
        assertEquals(location, trip.meetingLocation)
        assertEquals(draft.routeStops, trip.routeStops)
    }

    @Test
    fun routeRequiresTwoToTwentyStops() {
        val draft = MockTripRepository.beginRecruitmentDraft("cheongsong-juwangsan")

        assertThrows(IllegalArgumentException::class.java) {
            MockTripRepository.updateRecruitmentDraft(draft.copy(routeStops = draft.routeStops.take(1)))
        }
        assertThrows(IllegalArgumentException::class.java) {
            MockTripRepository.updateRecruitmentDraft(
                draft.copy(
                    routeStops = List(21) { index ->
                        RouteStop("s$index", time = "09:00", name = "방문지 $index", memo = "")
                    }
                )
            )
        }
    }

    @Test
    fun recruitmentNameAndConditionsStaySeparateFromCourse() {
        val draft = MockTripRepository.beginRecruitmentDraft("cheongsong-juwangsan").copy(
            recruitmentName = "30대끼리 느긋하게 힐링 여행가요~",
            estimatedCostPerPerson = 62_000,
            minimumAge = 30,
            maximumAge = 39,
            genderCondition = "성별 무관"
        )
        MockTripRepository.updateRecruitmentDraft(draft)

        val trip = MockTripRepository.createRecruitmentFromDraft(draft.id)

        assertEquals("30대끼리 느긋하게 힐링 여행가요~", trip.recruitmentName)
        assertEquals("주왕산 & 주산지 힐링 트레킹", MockTripRepository.findCourse(trip.courseId).title)
        assertEquals(62_000, trip.estimatedCostPerPerson)
        assertEquals(30, trip.minimumAge)
        assertEquals(39, trip.maximumAge)
        assertEquals("30대끼리 느긋하게 힐링 여행가요~", MockTripRepository.findThread(trip.chatThreadId).title)
    }

    @Test
    fun ageConditionsMustStayInsideTwentyToOneHundred() {
        val draft = MockTripRepository.beginRecruitmentDraft("cheongsong-juwangsan")

        assertThrows(IllegalArgumentException::class.java) {
            MockTripRepository.updateRecruitmentDraft(draft.copy(minimumAge = 19))
        }
        assertThrows(IllegalArgumentException::class.java) {
            MockTripRepository.updateRecruitmentDraft(draft.copy(maximumAge = 101))
        }
        assertThrows(IllegalArgumentException::class.java) {
            MockTripRepository.updateRecruitmentDraft(draft.copy(minimumAge = 50, maximumAge = 40))
        }
    }

    @Test
    fun onlyUnconfirmedCustomRouteCanChangeAndChangeIsRecordedInChat() {
        val custom = MockTripRepository.findTrip("trip-cheongsong-juwangsan")
        val nextStops = custom.routeStops + RouteStop("new-stop", time = "16:30", name = "달기약수탕", memo = "늦은 점심")

        MockTripRepository.updateCustomRoute(custom.id, nextStops)

        assertEquals(nextStops, MockTripRepository.findTrip(custom.id).routeStops)
        assertTrue(MockTripRepository.findThread(custom.chatThreadId).messages.last().text.contains("경로를 수정"))
        assertThrows(IllegalArgumentException::class.java) {
            MockTripRepository.updateCustomRoute("trip-andong-dosan", nextStops)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MockTripRepository.updateCustomRoute("trip-andong-hahoe", nextStops)
        }
    }

    @Test
    fun noticesPersistAndOnlyThreeCanBePinned() {
        val tripId = "trip-cheongsong-juwangsan"
        val seeded = MockTripRepository.noticesForTrip(tripId)
        repeat(4) { index -> MockTripRepository.addNotice(tripId, "추가 공지 $index", "내용 $index") }

        val notices = MockTripRepository.noticesForTrip(tripId)
        assertTrue(notices.size > seeded.size)
        assertTrue(notices.count { it.isPinned } <= 3)
        assertTrue(MockTripRepository.noticesForTrip(tripId).any { it.title == "추가 공지 3" })
    }
}
