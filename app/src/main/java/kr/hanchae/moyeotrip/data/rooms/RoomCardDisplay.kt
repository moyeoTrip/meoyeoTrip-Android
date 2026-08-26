// 모집 카드(화면기획 10 탐색 · 11 탐색 지도)의 서버값 표기 규칙.
//
// 화면 코드가 아니라 이 파일의 순수 함수에 모아 둔 이유는 두 가지다.
// - 서버가 주지 않는 값을 지어내지 않는다는 규칙을 한 곳에서 지킨다(null 이면 표기를 숨긴다).
// - 시각 포맷(`HH:mm:ss` / `HH:mm`)·좌표 유효성 같은 판정을 단위 테스트로 고정한다.
package kr.hanchae.moyeotrip.data.rooms

/** 지도에 올릴 집합 좌표. 위·경도가 **둘 다** 있는 항목만 만들어진다. */
data class RoomMeetingPoint(val latitude: Double, val longitude: Double)

/** 같은 자리(≈10km)에 겹치는 모집을 하나로 묶은 지역 마커 — 화면기획 11의 초록 원 + 개수. */
data class RoomMeetingCluster(val point: RoomMeetingPoint, val roomIds: List<Long>)

/**
 * 서버 시각 값을 화면 표기용 `HH:mm` 으로 정규화한다.
 * 문서는 `HH:mm` 이지만 실응답은 `HH:mm:ss` 라 **두 포맷을 모두** 받는다.
 * 그 밖의 값(빈 문자열·형식 불일치)은 null 이다 — 반쪽 값을 지어내지 않는다.
 */
fun roomClockText(value: String?): String? {
    val matched = CLOCK_PATTERN.matchEntire(value?.trim().orEmpty()) ?: return null
    val (hour, minute) = matched.destructured
    return "$hour:$minute"
}

/** `2026-09-12T08:30:00` 같은 집합 일시에서 시각만 뽑는다. */
fun roomDateTimeClockText(value: String?): String? =
    roomClockText(value?.substringAfter('T', "")?.takeIf(String::isNotBlank))

/** 당일 여행 시간. 숙박이면 서버가 둘 다 null 로 주므로 표기 자체가 사라진다. */
fun ChatRoomSearchResult.dayTripHoursText(): String? {
    val start = roomClockText(dayTripStartTime) ?: return null
    val end = roomClockText(dayTripEndTime) ?: return null
    return "$start - $end"
}

/** 집합 안내. 장소가 미정이면 서버가 null 을 주므로 시각만 남고 "미정" 같은 문구는 만들지 않는다. */
fun ChatRoomSearchResult.meetingText(): String? = listOfNotNull(
    roomDateTimeClockText(meetingDateTime),
    meetingDetails?.takeIf(String::isNotBlank)
).joinToString(" · ").takeIf(String::isNotBlank)

/**
 * 모집 마감 D-day(여행 시작 D-day가 아니다). 이미 지난 마감은 화면기획에 문구가 없어 숨긴다.
 * 표기는 화면기획의 "마감 D-3" 을 그대로 쓴다.
 */
fun ChatRoomSearchResult.recruitmentDeadlineText(): String? =
    recruitmentDDay?.takeIf { it >= 0 }?.let { days -> "마감 D-$days" }

/**
 * 모집 마감 D-day 표기. 마감이 지난 방은 서버가 음수(예: -95)를 주는데 화면기획에 `D--95` 같은
 * 표기가 없어 숨긴다. 15 모집 상세·19 모임 목록·20 채팅방·26 내 여행이 같은 규칙을 공유한다.
 */
fun recruitmentDDayText(days: Int?): String? = days?.takeIf { it >= 0 }?.let { "D-$it" }

/** 상태 배지. 화면기획 10의 카드 배지 문구(진행중·확정)와 26·15의 모집취소를 그대로 쓴다. */
fun ChatRoomSearchResult.statusLabel(): String? = when (status) {
    "RECRUITING" -> "진행중"
    "CONFIRMED" -> "확정"
    "CANCELLED" -> "모집취소"
    else -> null
}

/** 집합 좌표 — 한쪽만 오면 지도에 올릴 근거가 없으므로 null 이다. */
fun ChatRoomSearchResult.meetingPoint(): RoomMeetingPoint? {
    val latitude = meetingLatitude ?: return null
    val longitude = meetingLongitude ?: return null
    return RoomMeetingPoint(latitude, longitude)
}

/**
 * 화면기획 11의 지역 묶음 마커. 좌표가 온전한 모집만 남기고 소수점 첫째 자리(≈10km)로 묶는다.
 * 서버 검색 응답에 지역 필드가 없어 좌표로 묶는다 — 지역명을 지어내지 않는다.
 */
fun List<ChatRoomSearchResult>.meetingClusters(): List<RoomMeetingCluster> = this
    .mapNotNull { room -> room.meetingPoint()?.let { room.roomId to it } }
    .groupBy { (_, point) ->
        "%.1f,%.1f".format(point.latitude, point.longitude)
    }
    .map { (_, group) ->
        RoomMeetingCluster(
            point = RoomMeetingPoint(
                latitude = group.sumOf { it.second.latitude } / group.size,
                longitude = group.sumOf { it.second.longitude } / group.size
            ),
            roomIds = group.map { it.first }
        )
    }

private val CLOCK_PATTERN = Regex("""^(\d{2}):(\d{2})(?::\d{2}(?:\.\d+)?)?$""")
