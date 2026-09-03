package kr.hanchae.moyeotrip.data.rooms

import kr.hanchae.moyeotrip.data.CourseSource
import kr.hanchae.moyeotrip.data.MeetingLocation
import kr.hanchae.moyeotrip.data.RecruitmentDraft
import kr.hanchae.moyeotrip.data.TripScheduleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitmentDraftRequestTest {
    /** 서버 공개 코스를 고르지 않으면 보낼 근거(courseType=PUBLIC 의 courseId)가 없다. */
    @Test
    fun draftWithoutServerCourseIsNotSent() {
        assertNull(draft().toNewChatRoom())
    }

    @Test
    fun dayTripSendsTimesWithoutEndDate() {
        val request = draft(serverCourseId = 21).toNewChatRoom()

        assertNotNull(request)
        val json = request!!.toRequestJson()
        assertEquals("DAY_TRIP", json.getString("tripType"))
        assertEquals("2026-05-25", json.getString("startDate"))
        assertEquals("2026-05-22", json.getString("recruitmentDeadlineDate"))
        assertEquals("08:00", json.getString("dayTripStartTime"))
        assertEquals("18:00", json.getString("dayTripEndTime"))
        assertEquals("2026-05-25T07:50:00", json.getString("meetingDateTime"))
        assertEquals("PUBLIC", json.getString("courseType"))
        assertEquals(21L, json.getLong("courseId"))
        assertFalse(json.has("endDate"))
    }

    @Test
    fun overnightSendsEndDateWithoutTimes() {
        val request = draft(
            serverCourseId = 21,
            scheduleType = TripScheduleType.Overnight,
            endDate = "2026.05.26 (일)"
        ).toNewChatRoom()

        val json = request!!.toRequestJson()
        assertEquals("OVERNIGHT", json.getString("tripType"))
        assertEquals("2026-05-26", json.getString("endDate"))
        assertFalse(json.has("dayTripStartTime"))
        assertFalse(json.has("dayTripEndTime"))
    }

    @Test
    fun overnightWithoutEndDateIsNotSent() {
        assertNull(
            draft(serverCourseId = 21, scheduleType = TripScheduleType.Overnight, endDate = null).toNewChatRoom()
        )
    }

    @Test
    fun unparsableDatesAreNotSent() {
        assertNull(draft(serverCourseId = 21, travelDate = "다음 주 토요일").toNewChatRoom())
        assertNull(draft(serverCourseId = 21, recruitmentDeadline = "미정").toNewChatRoom())
    }

    /** 직접 만든 코스는 서버가 방문지 contentId·태그를 요구해 이번 연동에서 전송 대상이 아니다. */
    @Test
    fun customCourseDraftIsNotSent() {
        assertNull(
            draft(serverCourseId = 21).copy(courseSource = CourseSource.Custom).toNewChatRoom()
        )
    }

    @Test
    fun genderConditionsMapToServerEnums() {
        assertEquals("NONE", genderRestriction("제한 없음"))
        assertEquals("NONE", genderRestriction("성별 무관"))
        assertEquals("FEMALE_ONLY", genderRestriction("여성만"))
        assertEquals("MALE_ONLY", genderRestriction("남성만"))
    }

    @Test
    fun blankOptionalValuesAreOmittedInsteadOfSentEmpty() {
        val request = draft(serverCourseId = 21).toNewChatRoom()!!.copy(
            description = "  ",
            meetingDetails = "",
            minimumAge = null,
            maximumAge = null,
            participationFee = null
        )

        val json = request.toRequestJson()
        assertFalse(json.has("description"))
        assertFalse(json.has("meetingDetails"))
        assertFalse(json.has("minimumAge"))
        assertFalse(json.has("maximumAge"))
        assertFalse(json.has("participationFee"))
        assertTrue(json.has("meetingLatitude"))
    }

    @Test
    fun approvalModeFollowsTheDraftChoice() {
        assertEquals(
            "MANUAL",
            draft(serverCourseId = 21, autoApproval = false).toNewChatRoom()!!.joinApprovalMode
        )
        assertEquals("AUTO", draft(serverCourseId = 21).toNewChatRoom()!!.joinApprovalMode)
    }

    private fun draft(
        serverCourseId: Long? = null,
        scheduleType: TripScheduleType = TripScheduleType.DayTrip,
        travelDate: String = "2026.05.25 (토)",
        recruitmentDeadline: String = "2026.05.22 (금) 23:59",
        endDate: String? = null,
        autoApproval: Boolean = true
    ): RecruitmentDraft = RecruitmentDraft(
        id = "draft-test",
        meetingLocation = MeetingLocation(
            name = "청송 시외버스터미널",
            detail = "정문 앞",
            latitude = 36.435612,
            longitude = 129.057214,
            meetingTime = "07:50"
        ),
        serverCourseId = serverCourseId,
        scheduleType = scheduleType,
        travelDate = travelDate,
        recruitmentDeadline = recruitmentDeadline,
        endDate = endDate,
        startTime = "08:00",
        endTime = "18:00",
        autoApproval = autoApproval
    )
}
