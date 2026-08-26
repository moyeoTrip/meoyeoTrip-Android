package kr.hanchae.moyeotrip.data.rooms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoomCardDisplayTest {
    /** 문서는 `HH:mm`, 실응답은 `HH:mm:ss` 다 — 두 포맷 모두 `HH:mm` 으로 보여야 한다. */
    @Test
    fun clockTextAcceptsBothServerTimeFormats() {
        assertEquals("09:00", roomClockText("09:00:00"))
        assertEquals("09:00", roomClockText("09:00"))
        assertEquals("18:30", roomClockText("18:30:45.123456"))
        assertEquals("08:05", roomClockText(" 08:05 "))
    }

    @Test
    fun clockTextRejectsValuesItCannotTrust() {
        assertNull(roomClockText(null))
        assertNull(roomClockText(""))
        assertNull(roomClockText("오전 9시"))
        assertNull(roomClockText("9:0"))
        assertNull(roomClockText("2026-09-12T09:00:00"))
    }

    @Test
    fun dateTimeClockTextTakesTimePartOnly() {
        assertEquals("08:30", roomDateTimeClockText("2026-09-12T08:30:00"))
        assertNull(roomDateTimeClockText("2026-09-12"))
        assertNull(roomDateTimeClockText(null))
    }

    @Test
    fun dayTripHoursAreShownAsHourMinuteAndHiddenForOvernight() {
        assertEquals("09:00 - 18:00", searchResult().dayTripHoursText())
        assertNull(searchResult(dayTripStartTime = null, dayTripEndTime = null).dayTripHoursText())
        // 한쪽만 오면 범위를 만들 수 없다 — 표기를 지어내지 않는다
        assertNull(searchResult(dayTripEndTime = null).dayTripHoursText())
    }

    @Test
    fun meetingTextHidesUndecidedPlaceWithoutInventingWording() {
        assertEquals("08:30 · 안동역 1번 출구 앞", searchResult().meetingText())
        assertEquals("08:30", searchResult(meetingDetails = null).meetingText())
        assertNull(searchResult(meetingDateTime = null, meetingDetails = null).meetingText())
    }

    @Test
    fun deadlineTextUsesRecruitmentDDayAndHidesPastDeadlines() {
        assertEquals("마감 D-15", searchResult().recruitmentDeadlineText())
        assertEquals("마감 D-0", searchResult(recruitmentDDay = 0).recruitmentDeadlineText())
        assertNull(searchResult(recruitmentDDay = -38).recruitmentDeadlineText())
        assertNull(searchResult(recruitmentDDay = null).recruitmentDeadlineText())
    }

    @Test
    fun statusLabelUsesPlanningWordingAndHidesUnknownValues() {
        assertEquals("진행중", searchResult(status = "RECRUITING").statusLabel())
        assertEquals("확정", searchResult(status = "CONFIRMED").statusLabel())
        assertEquals("모집취소", searchResult(status = "CANCELLED").statusLabel())
        assertNull(searchResult(status = "").statusLabel())
        assertNull(searchResult(status = "SOMETHING_NEW").statusLabel())
    }

    @Test
    fun meetingPointNeedsBothCoordinates() {
        assertEquals(RoomMeetingPoint(36.576, 128.97), searchResult().meetingPoint())
        assertNull(searchResult(meetingLatitude = null).meetingPoint())
        assertNull(searchResult(meetingLongitude = null).meetingPoint())
    }

    /** 좌표가 반쪽인 방은 지도 마커에서 빠지고, 같은 자리의 방은 하나로 묶인다. */
    @Test
    fun clustersDropRoomsWithHalfCoordinatesAndGroupNearbyRooms() {
        val rooms = listOf(
            searchResult(roomId = 1),
            searchResult(roomId = 2, meetingLatitude = 36.58, meetingLongitude = 128.975),
            searchResult(roomId = 3, meetingLatitude = 35.1, meetingLongitude = 129.0),
            searchResult(roomId = 4, meetingLongitude = null),
            searchResult(roomId = 5, meetingLatitude = null)
        )

        val clusters = rooms.meetingClusters()

        assertEquals(2, clusters.size)
        assertEquals(listOf(1L, 2L), clusters.first().roomIds)
        assertEquals(listOf(3L), clusters.last().roomIds)
        assertEquals(5, rooms.size)
    }

    @Test
    fun clustersAreEmptyWhenNoRoomHasCoordinates() {
        assertEquals(
            emptyList<RoomMeetingCluster>(),
            listOf(searchResult(meetingLatitude = null, meetingLongitude = null)).meetingClusters()
        )
    }

    private fun searchResult(
        roomId: Long = 1,
        status: String = "RECRUITING",
        recruitmentDDay: Int? = 15,
        dayTripStartTime: String? = "09:00:00",
        dayTripEndTime: String? = "18:00:00",
        meetingLatitude: Double? = 36.576,
        meetingLongitude: Double? = 128.97,
        meetingDetails: String? = "안동역 1번 출구 앞",
        meetingDateTime: String? = "2026-09-12T08:30:00"
    ) = ChatRoomSearchResult(
        roomId = roomId,
        title = "안동 역사 여행",
        description = null,
        thumbnail = null,
        tripType = if (dayTripStartTime == null) "OVERNIGHT" else "DAY_TRIP",
        startDate = "2026-09-12",
        endDate = null,
        dayTripStartTime = dayTripStartTime,
        dayTripEndTime = dayTripEndTime,
        recruitmentDeadlineDate = "2026-09-09",
        recruitmentDDay = recruitmentDDay,
        status = status,
        favorite = false,
        meetingLatitude = meetingLatitude,
        meetingLongitude = meetingLongitude,
        meetingDetails = meetingDetails,
        meetingDateTime = meetingDateTime,
        participantCount = 1,
        maxParticipants = 5,
        courseTitle = "안동 역사·야경 코스",
        tags = emptyList()
    )
}
