package kr.hanchae.moyeotrip.ui.components

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 피드·댓글의 작성 시각 표기 — **상대 시각**이다.
 *
 * 기획(`screens-additions3.jsx` 의 `2시간 전`)과 웹(`moyeo-api.js` 의 `formatRelative`)이
 * 그렇게 쓴다. 예전에는 iOS·안드로이드만 `2026.09.07` 처럼 절대 날짜를 써서
 * 4열 비교에서 두 앱만 달라 보였다 (2026-09-08 사용자 지적).
 *
 * 규칙과 경계값은 **웹 `formatRelative` 를 그대로** 옮겼다 — 표면마다 다른 반올림을 쓰면
 * 같은 시각이 「59분 전」과 「1시간 전」으로 갈린다:
 *   1분 미만 `방금 전` · 60분 미만 `N분 전` · 24시간 미만 `N시간 전` ·
 *   1일 `어제` · 7일 미만 `N일 전` · 그 이상은 `YYYY.MM.DD`.
 *
 * 공지 「작성일」처럼 **날짜 자체가 정보인 자리는 절대 날짜를 그대로 둔다** — 기획도 그렇다.
 */
fun moyeoRelativeTime(dateTime: String, now: LocalDateTime = LocalDateTime.now()): String {
    val then = parseServerDateTime(dateTime)
        // 파싱 실패는 지어내지 않고 날짜 부분만 보여준다 (예전 표기와 같다).
        ?: return dateTime.take(10).replace('-', '.')

    val minutes = Duration.between(then, now).toMinutes()
    return when {
        minutes < 1 -> "방금 전"
        minutes < 60 -> "${minutes}분 전"
        minutes < 60 * 24 -> "${minutes / 60}시간 전"
        minutes < 60 * 24 * 2 -> "어제"
        minutes < 60 * 24 * 7 -> "${minutes / (60 * 24)}일 전"
        else -> then.format(ABSOLUTE)
    }
}

/** 서버는 타임존 없는 `yyyy-MM-dd'T'HH:mm:ss[.SSSSSS]` 를 준다. */
private fun parseServerDateTime(value: String): LocalDateTime? {
    if (value.isBlank()) return null
    runCatching { return LocalDateTime.parse(value) }
    // 날짜만 온 경우(`2026-09-07`)도 받아 준다.
    runCatching { return LocalDate.parse(value).atStartOfDay() }
    return null
}

private val ABSOLUTE: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

/**
 * 공지 작성 시각 — **절대 시각** 「8월 29일 오후 1:02」.
 *
 * 20-3 공지 이력은 「언제 올린 공지인가」가 정보라서 상대 시각으로 뭉개지 않는다.
 * 기획(`5월 20일 오후 2:14`)과 iOS(`ServerDateTime.noticeTimeText`)가 이 꼴이다.
 * 예전에는 안드로이드만 `2026.08.29` 로 **시각을 아예 빼고** 찍었다 (사용자 지적 2026-09-09).
 */
fun moyeoNoticeTime(dateTime: String): String {
    val then = parseServerDateTime(dateTime) ?: return dateTime.take(10).replace('-', '.')
    return then.format(NOTICE_TIME)
}

private val NOTICE_TIME: DateTimeFormatter =
    DateTimeFormatter.ofPattern("M월 d일 a h:mm", java.util.Locale.KOREAN)

/**
 * 여행 날짜 — 「2026.10.03 (토)」.
 *
 * 요일이 붙어야 「토요일 하루 나들이」인지 바로 읽힌다. 기획·웹·iOS 가 요일을 함께 쓴다.
 * 날짜만 오는 값(`2026-10-03`)을 받아 그대로 되돌려주므로 시각과 섞이지 않는다.
 */
fun moyeoTripDateText(date: String): String {
    val parsed = runCatching { LocalDate.parse(date) }.getOrNull()
        ?: return date.replace('-', '.')
    return parsed.format(TRIP_DATE)
}

private val TRIP_DATE: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy.MM.dd (E)", java.util.Locale.KOREAN)
