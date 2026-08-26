package kr.hanchae.moyeotrip.data.rooms

import kr.hanchae.moyeotrip.data.CourseSource
import kr.hanchae.moyeotrip.data.RecruitmentDraft
import kr.hanchae.moyeotrip.data.TripScheduleType

/**
 * 화면기획 17 모집 만들기 초안 → POST chat-rooms 요청값.
 *
 * 초안은 화면기획 목데이터 표기(`2026.05.25 (토)` 처럼)를 그대로 들고 있어서 서버가 받는
 * ISO 값으로 옮겨야 한다. 옮길 수 없는 값(날짜 형식 불일치, 서버 공개 코스를 고르지 않음)이면
 * **null 을 돌려주고 화면은 서버 전송을 하지 않는다** — 없는 값을 지어내지 않는다.
 */
fun RecruitmentDraft.toNewChatRoom(): NewChatRoom? {
    // 직접 만든 코스는 서버가 방문지 contentId 와 태그를 요구한다(17-1a 흐름이 아직 담지 않는 값) — 보내지 않는다
    if (courseSource != CourseSource.Linked) return null
    val courseId = serverCourseId ?: return null
    val startDate = isoDate(travelDate) ?: return null
    val deadline = isoDate(recruitmentDeadline) ?: return null
    val meetingTime = roomClockText(meetingLocation.meetingTime) ?: return null
    val dayTrip = scheduleType == TripScheduleType.DayTrip
    val endDate = endDate?.let(::isoDate)
    if (!dayTrip && endDate == null) return null
    val startTime = roomClockText(startTime)
    val endTime = roomClockText(endTime)
    if (dayTrip && (startTime == null || endTime == null)) return null

    return NewChatRoom(
        title = recruitmentName,
        description = note,
        maxParticipants = capacity,
        dayTrip = dayTrip,
        startDate = startDate,
        endDate = endDate,
        dayTripStartTime = startTime,
        dayTripEndTime = endTime,
        recruitmentDeadlineDate = deadline,
        meetingLatitude = meetingLocation.latitude,
        meetingLongitude = meetingLocation.longitude,
        meetingDetails = meetingLocation.detail,
        meetingDateTime = "${startDate}T$meetingTime:00",
        participationFee = estimatedCostPerPerson,
        genderRestriction = genderRestriction(genderCondition),
        minimumAge = minimumAge,
        maximumAge = maximumAge,
        joinApprovalMode = if (autoApproval) "AUTO" else "MANUAL",
        publicCourseId = courseId
    )
}

/** `2026.05.25 (토) 23:59` · `2026.05.25 (토)` → `2026-05-25`. 형식이 다르면 null 이다. */
internal fun isoDate(value: String): String? {
    val matched = DATE_PATTERN.find(value) ?: return null
    val (year, month, day) = matched.destructured
    return "$year-$month-$day"
}

/** 화면기획 문구 → 서버 enum. 서버 enum 은 `NONE`·`FEMALE_ONLY`·`MALE_ONLY` 다. */
internal fun genderRestriction(condition: String): String = when (condition) {
    "여성만" -> "FEMALE_ONLY"
    "남성만" -> "MALE_ONLY"
    else -> "NONE"
}

private val DATE_PATTERN = Regex("""(\d{4})\.(\d{2})\.(\d{2})""")
