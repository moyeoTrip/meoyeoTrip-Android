package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.hanchae.moyeotrip.data.CourseSource
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.data.RecruitmentDraft
import kr.hanchae.moyeotrip.data.RecruitmentNotice
import kr.hanchae.moyeotrip.data.RouteStop
import kr.hanchae.moyeotrip.data.TripCourse
import kr.hanchae.moyeotrip.data.TripRecruitment
import kr.hanchae.moyeotrip.data.TripScheduleType
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

@Composable
fun RecruitmentCourseSourceScreen(
    courseId: String,
    onBack: () -> Unit,
    onOpenCustomCourse: (String) -> Unit,
    onOpenSchedule: (String) -> Unit
) {
    var draft by remember(courseId) { mutableStateOf(MockTripRepository.beginRecruitmentDraft(courseId)) }
    val selectedCourse = MockTripRepository.findCourse(draft.selectedCourseId)

    RecruitmentScaffold(
        title = "모집 만들기 (1/5)",
        onBack = onBack,
        bottom = {
            Button(
                onClick = {
                    MockTripRepository.updateRecruitmentDraft(draft)
                    if (draft.courseSource == CourseSource.Custom) {
                        onOpenCustomCourse(draft.id)
                    } else {
                        onOpenSchedule(draft.id)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("create-source-next"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (draft.courseSource == CourseSource.Custom) "코스 만들러 가기" else "이 코스로 다음")
            }
        }
    ) {
        item { RecruitmentStepIndicator(activeStep = 0) }
        item {
            SectionIntro(
                title = "코스 선택",
                body = "등록된 코스를 그대로 써도 되고, 직접 짜도 돼요."
            )
        }
        item {
            CourseSourceChoice(
                source = CourseSource.Linked,
                selected = draft.courseSource == CourseSource.Linked,
                title = "등록된 코스로 떠나기",
                body = "TourAPI·경북나드리 기반으로 검증된 동선을 그대로 가져와요.",
                policy = "경로 수정 불가 · 집합 정보만 설정",
                onClick = {
                    draft = draft.copy(
                        courseSource = CourseSource.Linked,
                        routeStops = selectedCourse.stops.toRouteStops(selectedCourse.id)
                    )
                }
            )
        }
        item {
            CourseSourceChoice(
                source = CourseSource.Custom,
                selected = draft.courseSource == CourseSource.Custom,
                title = "코스 직접 만들기",
                body = "방문지와 시간을 직접 짜고 여행 확정 전까지 고칠 수 있어요.",
                policy = "여행 확정 전까지 수정 가능",
                onClick = { draft = draft.copy(courseSource = CourseSource.Custom) }
            )
        }
        if (draft.courseSource == CourseSource.Linked) {
            item {
                SearchLikeField("등록된 코스 검색")
            }
            items(MockTripRepository.courses.take(3), key = { it.id }) { course ->
                val selected = course.id == draft.selectedCourseId
                CompactCourseChoice(
                    title = course.title,
                    subtitle = "${course.region} · ${course.duration} ${course.distance} · 방문지 ${course.stops.size}",
                    // 화면기획은 코스 출처(여행자 코스/모여트립 추천)를 함께 보여준다
                    sourceLabel = if (course.publisher != null) "여행자 코스" else "모여트립 추천",
                    selected = selected,
                    course = course,
                    onClick = {
                        draft = draft.copy(
                            selectedCourseId = course.id,
                            travelDate = course.startLabel,
                            meetingLocation = draft.meetingLocation.copy(name = course.meetingPoint),
                            routeStops = course.stops.toRouteStops(course.id),
                            capacity = course.capacity,
                            note = course.recruitmentNote
                        )
                    }
                )
            }
            item {
                InfoBanner(
                    icon = Icons.Filled.Lock,
                    text = "등록된 코스는 방문지와 순서가 고정돼요. 일정·집합 장소·인원 조건은 마감 전까지 바꿀 수 있어요.",
                    warning = false
                )
            }
        } else {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("내 경로를 그려볼까요?", fontWeight = FontWeight.ExtraBold)
                        Text(
                            "방문지를 검색해 순서대로 담고 시간을 적어요. 최소 2개 · 최대 20개",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomCourseScreen(
    draftId: String,
    onBack: () -> Unit,
    onOpenPlaceSearch: (String) -> Unit,
    onContinue: (String) -> Unit
) {
    var draft by remember(draftId) { mutableStateOf(MockTripRepository.findRecruitmentDraft(draftId)) }

    RecruitmentScaffold(
        title = "코스 직접 만들기",
        onBack = onBack,
        bottom = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onBack, modifier = Modifier.height(48.dp), shape = RoundedCornerShape(12.dp)) {
                    Text("취소")
                }
                Button(
                    onClick = {
                        MockTripRepository.updateRecruitmentDraft(draft)
                        onContinue(draft.id)
                    },
                    enabled = draft.routeStops.size >= 2,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("custom-course-continue"),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("이 코스로 계속하기") }
            }
        }
    ) {
        item { RouteMapPreview(stopCount = draft.routeStops.size, modifier = Modifier.height(160.dp)) }
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clickable { onOpenPlaceSearch(draft.id) }
                    .testTag("custom-course-place-search")
            ) {
                SearchLikeField("방문지 검색 (TourAPI · 경북 22개 시·군)")
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Day 1", fontWeight = FontWeight.ExtraBold)
                Text(
                    "${draft.routeStops.size}개 방문지 · 최소 2개",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(draft.routeStops, key = { it.id }) { stop ->
            RouteStopRow(
                stop = stop,
                index = draft.routeStops.indexOf(stop),
                editable = true,
                onRemove = {
                    if (draft.routeStops.size > 2) {
                        draft = draft.copy(routeStops = draft.routeStops.filterNot { it.id == stop.id })
                    }
                }
            )
        }
        item {
            OutlinedButton(
                onClick = {
                    if (draft.routeStops.size < 20) {
                        val next = draft.routeStops.size + 1
                        draft = draft.copy(
                            routeStops = draft.routeStops + RouteStop(
                                id = "${draft.id}-custom-$next",
                                time = if (next == 4) "16:30" else "17:30",
                                name = if (next == 4) "달기약수탕" else "청송 객주문학관",
                                memo = if (next == 4) "늦은 점심" else "실내 대체 코스"
                            )
                        )
                    }
                },
                enabled = draft.routeStops.size < 20,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("custom-course-add-stop"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("방문지 추가", modifier = Modifier.padding(start = 6.dp))
            }
        }
        item {
            OutlinedButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) { Text("+ 다음 날 추가 (1박 이상일 때)") } // 글자에 '+'가 있으니 아이콘은 두지 않는다
        }
        item {
            InfoBanner(
                icon = Icons.Filled.EditNote,
                text = "직접 만든 코스는 여행이 확정되기 전까지 호스트가 언제든 고칠 수 있어요. 수정하면 채팅방 멤버 모두에게 알림이 가요."
            )
        }
    }
}

@Composable
fun CreateScheduleScreen(draftId: String, onBack: () -> Unit, onContinue: (String) -> Unit) {
    var draft by remember(draftId) { mutableStateOf(MockTripRepository.findRecruitmentDraft(draftId)) }

    RecruitmentScaffold(
        title = "모집 만들기 (2/5)",
        onBack = onBack,
        bottom = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.height(48.dp), shape = RoundedCornerShape(12.dp)) {
                    Text("이전")
                }
                Button(
                    onClick = {
                        MockTripRepository.updateRecruitmentDraft(draft)
                        onContinue(draft.id)
                    },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("create-schedule-next"),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("다음") }
            }
        }
    ) {
        item { RecruitmentStepIndicator(activeStep = 1) }
        item { SectionIntro("일정 정하기", "당일치기인지 먼저 골라주세요. 입력하는 항목이 달라져요.") }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(10.dp)
                ).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ScheduleTypeButton(
                    label = "당일치기",
                    subtitle = "시작·종료 시간",
                    selected = draft.scheduleType == TripScheduleType.DayTrip,
                    modifier = Modifier.weight(1f).testTag("schedule-day-trip")
                ) { draft = draft.copy(scheduleType = TripScheduleType.DayTrip, endDate = null) }
                ScheduleTypeButton(
                    label = "1박 이상",
                    subtitle = "시작·종료 날짜",
                    selected = draft.scheduleType == TripScheduleType.Overnight,
                    modifier = Modifier.weight(1f).testTag("schedule-overnight")
                ) {
                    draft =
                        draft.copy(
                            scheduleType = TripScheduleType.Overnight,
                            endDate =
                                draft.endDate ?: "2026.05.26 (일)"
                        )
                }
            }
        }
        item { LabeledValue("여행 날짜 *", draft.travelDate, Icons.Filled.CalendarMonth) }
        if (draft.scheduleType == TripScheduleType.DayTrip) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LabeledValue(
                        "여행 시작 시간 *",
                        draft.startTime,
                        Icons.Filled.Schedule,
                        Modifier.weight(1f)
                    )
                    LabeledValue(
                        "여행 종료 시간 *",
                        draft.endTime,
                        Icons.Filled.Schedule,
                        Modifier.weight(1f)
                    )
                }
            }
            item {
                SummaryStrip(
                    "당일치기  ${draft.travelDate.substringBefore(" ")} ${draft.startTime} - ${draft.endTime} · 10시간"
                )
            }
        } else {
            item { LabeledValue("여행 종료 날짜 *", draft.endDate.orEmpty(), Icons.Filled.CalendarMonth) }
        }
        item { HorizontalDividerLine() }
        item { LabeledValue("모집 마감일 *", draft.recruitmentDeadline, Icons.Filled.Schedule) }
        item {
            Text(
                "출발 3일 전까지만 선택할 수 있어요. " +
                    "마감일에 최소 인원을 못 채우면 자동으로 소멸해요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            // 화면기획 17-2는 장소가 첫 줄, 시간·상세·좌표가 둘째 줄이다
            LabeledValue(
                label = "집합 장소 · 집합 시간 *",
                value = "${draft.meetingLocation.name} 앞",
                detail = "${draft.meetingLocation.meetingTime} ${draft.meetingLocation.detail} · " +
                    "%.4f, %.4f".format(draft.meetingLocation.latitude, draft.meetingLocation.longitude),
                icon = Icons.Filled.Place,
                tag = "create-schedule-meeting"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePeopleScreen(draftId: String, onBack: () -> Unit, onContinue: (String) -> Unit) {
    var draft by remember(draftId) { mutableStateOf(MockTripRepository.findRecruitmentDraft(draftId)) }
    var showAgeSheet by rememberSaveable { mutableStateOf(false) }

    if (showAgeSheet) {
        ModalBottomSheet(onDismissRequest = { showAgeSheet = false }) {
            Column(
                Modifier.padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("나이대 제한", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ParticipantCounter(
                        label = "최소 나이",
                        value = draft.minimumAge,
                        suffix = "세",
                        modifier = Modifier.weight(1f),
                        decreaseEnabled = draft.minimumAge > 20,
                        increaseEnabled = draft.minimumAge < draft.maximumAge,
                        onDecrease = { draft = draft.copy(minimumAge = draft.minimumAge - 1) },
                        onIncrease = { draft = draft.copy(minimumAge = draft.minimumAge + 1) }
                    )
                    ParticipantCounter(
                        label = "최대 나이",
                        value = draft.maximumAge,
                        suffix = "세",
                        modifier = Modifier.weight(1f),
                        decreaseEnabled = draft.maximumAge > draft.minimumAge,
                        increaseEnabled = draft.maximumAge < 100,
                        onDecrease = { draft = draft.copy(maximumAge = draft.maximumAge - 1) },
                        onIncrease = { draft = draft.copy(maximumAge = draft.maximumAge + 1) }
                    )
                }
                Button(
                    onClick = { showAgeSheet = false },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("create-people-age-done"),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("완료") }
            }
        }
    }

    RecruitmentScaffold(
        title = "모집 만들기 (3/5)",
        onBack = onBack,
        bottom = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.height(48.dp), shape = RoundedCornerShape(12.dp)) {
                    Text("이전")
                }
                Button(
                    onClick = {
                        MockTripRepository.updateRecruitmentDraft(draft)
                        onContinue(draft.id)
                    },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("create-people-next"),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("다음") }
            }
        }
    ) {
        item { RecruitmentStepIndicator(activeStep = 2) }
        item {
            SectionIntro(
                "몇 명이 모이면 좋을까요?",
                "최소 인원은 3명부터예요. 낯선 사람과 단둘이 되는 일은 생기지 않아요."
            )
        }
        item {
            // 최소 / 최대는 좌우로 나란히 (화면기획 기준)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ParticipantCounter(
                    label = "최소 인원",
                    value = draft.minParticipants,
                    hint = "3명 미만은 선택할 수 없어요",
                    modifier = Modifier.weight(1f),
                    decreaseEnabled = draft.minParticipants > 3,
                    increaseEnabled = draft.minParticipants < draft.capacity - 1,
                    onDecrease = { draft = draft.copy(minParticipants = draft.minParticipants - 1) },
                    onIncrease = { draft = draft.copy(minParticipants = draft.minParticipants + 1) }
                )
                ParticipantCounter(
                    label = "최대 인원",
                    value = draft.capacity,
                    hint = "최대 20명까지",
                    modifier = Modifier.weight(1f),
                    decreaseEnabled = draft.capacity > draft.minParticipants + 1,
                    increaseEnabled = draft.capacity < 20,
                    onDecrease = { draft = draft.copy(capacity = draft.capacity - 1) },
                    onIncrease = { draft = draft.copy(capacity = draft.capacity + 1) }
                )
            }
        }
        item {
            // 모집 카드 미리보기 — 인원 설정이 신청자에게 어떻게 보이는지 같은 컴포넌트로 확인한다
            RecruitmentCardPreview(minimum = draft.minParticipants, capacity = draft.capacity)
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("성별 제한", fontWeight = FontWeight.ExtraBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 모집 카드에는 "성별 무관"으로 적히는 값이라 두 표기를 같은 선택으로 읽는다
                    val noRestriction = draft.genderCondition in setOf("제한 없음", "성별 무관")
                    listOf("제한 없음", "여성만", "남성만").forEach { condition ->
                        val on = if (condition == "제한 없음") noRestriction else draft.genderCondition == condition
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { draft = draft.copy(genderCondition = condition) }
                                .testTag("create-people-gender-${condition.hashCode()}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (on) MoyeoTheme.tints.primaryTint else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Text(
                                condition,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                color = if (on) {
                                    MoyeoTheme.tints.onPrimaryTint
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                if (draft.genderCondition !in setOf("제한 없음", "성별 무관")) {
                    Text(
                        "수락되는 인원은 같은 성별로 한정돼요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // 화면기획·웹·iOS는 한 줄로 범위를 보여주고 눌러서 조정한다.
                // 스테퍼 두 개를 나란히 두면 같은 화면인데 안드로이드만 입력 칸이 두 배로 보인다.
                LabeledValue(
                    label = "나이대 제한",
                    value = "${draft.minimumAge} ~ ${draft.maximumAge}세",
                    icon = Icons.Filled.People,
                    tag = "create-people-age-range",
                    onClick = { showAgeSheet = true }
                )
                Text(
                    "최소·최대 모두 20~100세 사이에서 정할 수 있어요. " +
                        "조건에 맞지 않는 사용자에게는 신청 버튼이 비활성으로 보여요.",
                    modifier = Modifier.testTag("create-people-age-caption"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            InfoBanner(
                icon = Icons.Filled.Notifications,
                text = "최소 인원이 모이면 채팅방이 자동으로 열려요. 모집 마감 전까지 정원을 채울 수 있어요."
            )
        }
    }
}

/**
 * 모집 카드 미리보기.
 *
 * 인원 숫자만 보여주면 신청자 화면에서 어떻게 읽히는지 알 수 없어서,
 * 모집 카드와 같은 프로그레스 컴포넌트로 함께 보여준다.
 */
@Composable
private fun RecruitmentCardPreview(minimum: Int, capacity: Int) {
    val tints = MoyeoTheme.tints
    val mood = when {
        capacity <= 4 -> "말 트기 좋은 작은 그룹이에요" to false
        capacity <= 8 -> "단체 사진 예쁘게 나오는 최적 인원이에요" to false
        else -> "9명 이상은 친목이 쉽지 않을 수 있어요" to true
    }

    Surface(
        modifier = Modifier.fillMaxWidth().testTag("create-people-card-preview"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, tints.softLine)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "모집 카드에는 이렇게 보여요",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "$minimum / ${capacity}명 · 최소 충족",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LinearProgressIndicator(
                progress = { minimum / capacity.coerceAtLeast(1).toFloat() },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                mood.first,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (mood.second) tints.onWarningTint else tints.onPrimaryTint
            )
        }
    }
}

@Composable
private fun ParticipantCounter(
    label: String,
    value: Int,
    suffix: String = "명",
    hint: String? = null,
    modifier: Modifier = Modifier,
    decreaseEnabled: Boolean,
    increaseEnabled: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    // 라벨은 위, 스테퍼는 아래. 좁은 폭에 둘씩 나란히 놓아도 값이 잘리지 않는다.
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
        Surface(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onDecrease,
                    enabled = decreaseEnabled,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape
                ) { Text("−", fontSize = 16.sp) }
                Text(
                    "$value$suffix",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onIncrease,
                    enabled = increaseEnabled,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape
                ) { Text("+", fontSize = 16.sp) }
            }
        }
        if (hint != null) {
            Text(
                hint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CreateMeetPointScreen(draftId: String, onBack: () -> Unit, onSave: (String) -> Unit) {
    var draft by remember(draftId) { mutableStateOf(MockTripRepository.findRecruitmentDraft(draftId)) }
    var query by rememberSaveable { mutableStateOf(draft.meetingLocation.name) }
    var detail by rememberSaveable { mutableStateOf("터미널 정문 앞") }

    // 집합 장소는 일정 단계(2/5)에서 열리는 화면이라 다른 플랫폼처럼 단계 뷰를 함께 그린다
    RecruitmentScaffold(
        title = "모집 만들기 (2/5)",
        onBack = onBack,
        bottom = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("이전") }
                Button(
                    onClick = {
                        draft = draft.copy(
                            meetingLocation = draft.meetingLocation.copy(
                                name = query.ifBlank { "청송 시외버스터미널" },
                                detail = detail
                            )
                        )
                        MockTripRepository.updateRecruitmentDraft(draft)
                        onSave(draft.id)
                    },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("meeting-point-save"),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("이 위치로 지정") }
            }
        }
    ) {
        item { RecruitmentStepIndicator(activeStep = 1) }
        item { SectionIntro("집합 장소 정하기", "검색하거나 지도의 핀을 움직여 정확한 위치를 알려주세요.") }
        item {
            Box {
                RouteMapPreview(stopCount = 1, modifier = Modifier.fillMaxWidth().height(300.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    placeholder = { Text("장소 검색 (TourAPI)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(12.dp).background(MaterialTheme.colorScheme.surface)
                        .testTag("meeting-point-search"),
                    shape = RoundedCornerShape(10.dp)
                )
                // 화면기획 17-3의 중앙 핀과 조정 안내 말풍선
                Surface(
                    modifier = Modifier.align(Alignment.Center).size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    border = BorderStroke(3.dp, MaterialTheme.colorScheme.surface)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Surface(
                    modifier = Modifier.align(Alignment.BottomStart).padding(14.dp),
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = "핀을 끌어 위치를 조정하세요",
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("터미널 정문 앞", "2번 출구", "주차장 입구").forEach { option ->
                    OutlinedButton(onClick = { detail = option }, shape = RoundedCornerShape(20.dp)) {
                        Text(
                            option,
                            color = if (detail ==
                                option
                            ) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }
        item { LabeledValue("집합 장소 *", query.ifBlank { "청송 시외버스터미널" }, Icons.Filled.Place) }
        item { LabeledValue("상세 안내", detail, Icons.Filled.EditNote) }
        item { LabeledValue("좌표 (자동 저장)", "36.435612, 129.057214", Icons.Filled.MyLocation) }
        item { LabeledValue("집합 시간 *", draft.meetingLocation.meetingTime, Icons.Filled.Schedule) }
        item {
            InfoBanner(
                Icons.Filled.Notifications,
                "집합 시간 30분 전에 모든 멤버에게 알림이 가고, 채팅방 상단 공지에도 자동으로 올라가요."
            )
        }
    }
}

// / 17-5 신청 승인 방식 선택 카드
@Composable
private fun ApprovalModeCard(
    title: String,
    detail: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tints = MoyeoTheme.tints
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .testTag("create-detail-approval-$title"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(Modifier.size(42.dp), shape = RoundedCornerShape(8.dp), color = tints.primaryTint) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                Text(
                    detail,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                if (selected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                null,
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
fun CreateDetailScreen(draftId: String, onBack: () -> Unit, onSave: (String) -> Unit) {
    var draft by remember(draftId) { mutableStateOf(MockTripRepository.findRecruitmentDraft(draftId)) }
    var recruitmentName by rememberSaveable { mutableStateOf(draft.recruitmentName) }
    var introduction by rememberSaveable { mutableStateOf(draft.note) }
    var costText by rememberSaveable { mutableStateOf(draft.estimatedCostPerPerson.toString()) }
    var approvalMode by rememberSaveable { mutableStateOf("auto") }

    RecruitmentScaffold(
        title = "모집 만들기 (4/5)",
        onBack = onBack,
        bottom = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.height(48.dp), shape = RoundedCornerShape(12.dp)) {
                    Text("이전")
                }
                Button(
                    onClick = {
                        draft =
                            draft.copy(
                                recruitmentName = recruitmentName.trim(),
                                note = introduction.trim(),
                                estimatedCostPerPerson = costText.toIntOrNull()?.coerceAtLeast(0) ?: 0
                            )
                        MockTripRepository.updateRecruitmentDraft(draft)
                        onSave(draft.id)
                    },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("create-detail-save"),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("다음") }
            }
        }
    ) {
        item { RecruitmentStepIndicator(activeStep = 3) }
        item { SectionIntro("어떤 여행인지 알려주세요", "코스와 별개로 이 모집의 이름과 조건을 정해주세요.") }
        item {
            LabeledValue(
                label = "코스",
                value = MockTripRepository.findCourse(draft.selectedCourseId).title,
                icon = Icons.Filled.Lock,
                tag = "create-detail-course"
            )
        }
        item {
            Text(
                "Step 1에서 고른 코스 이름이며 여기서는 바꿀 수 없어요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            // 라벨은 인풋 위 (화면기획·웹과 같은 구조 — 플로팅 라벨은 값이 비면 플레이스홀더처럼 보인다)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "모집 이름 (채팅방 이름) *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                OutlinedTextField(
                    value = recruitmentName,
                    onValueChange = { recruitmentName = it.take(40) },
                    leadingIcon = { Icon(Icons.Filled.Group, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("create-detail-recruitment-name")
                )
                Text(
                    "코스 이름과 별개로, 어떤 사람들과 어떻게 가고 싶은지를 담아요. 채팅방 이름으로도 쓰여요.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            Text("소개글", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = introduction,
                onValueChange = { introduction = it.take(500) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("create-detail-introduction"),
                placeholder = { Text("어떤 분위기의 여행인지 알려주세요.") },
                minLines = 3,
                supportingText = { Text("${introduction.length}/500") }
            )
        }
        item {
            // 라벨은 인풋 위, 금액 아이콘과 참고 문구를 함께 (화면기획·웹·iOS와 같은 형태).
            // Material 플로팅 라벨만 쓰면 다른 플랫폼의 라벨-박스 구조와 달라 보인다.
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "예상 1인당 비용",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                OutlinedTextField(
                    value = costText,
                    onValueChange = { costText = it.filter(Char::isDigit).take(7) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Payments,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    suffix = { Text("원") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("create-detail-cost")
                )
                Text(
                    "TourAPI 기준 이 코스는 보통 4~5만원 내외예요. 참고용으로만 보여줘요.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            // 화면기획·웹·iOS에 있는 신청 승인 방식이 안드로이드에만 없었다
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("신청 승인 방식 *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
                ApprovalModeCard(
                    title = "자동 승인",
                    detail = "조건에 맞으면 바로 합류해요. 모임이 빨리 채워져요.",
                    icon = Icons.Filled.Bolt,
                    selected = approvalMode == "auto",
                    onClick = { approvalMode = "auto" }
                )
                ApprovalModeCard(
                    title = "수동 승인",
                    detail = "한마디와 매너 점수를 보고 호스트가 직접 수락해요.",
                    icon = Icons.Filled.PanTool,
                    selected = approvalMode == "manual",
                    onClick = { approvalMode = "manual" }
                )
            }
        }
        item { InfoBanner(Icons.Filled.Notifications, "마감 전까지 세부 조건을 바꿀 수 있고 변경 내용은 신청자에게 알려드려요.") }
    }
}

@Composable
fun CreateSummaryScreen(draftId: String, onBack: () -> Unit, onCreated: (TripRecruitment) -> Unit) {
    val draft = remember(draftId) { MockTripRepository.findRecruitmentDraft(draftId) }
    val course = MockTripRepository.findCourse(draft.selectedCourseId)

    RecruitmentScaffold(
        title = "모집 만들기 (5/5)",
        onBack = onBack,
        bottom = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.height(48.dp), shape = RoundedCornerShape(12.dp)) {
                    Text("이전")
                }
                Button(
                    onClick = { onCreated(MockTripRepository.createRecruitmentFromDraft(draft.id)) },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("create-summary-submit"),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("모집 열기") }
            }
        }
    ) {
        item { RecruitmentStepIndicator(activeStep = 4) }
        item { SectionIntro("이대로 모집을 열까요?", "등록 후에도 마감 전까지는 대부분 고칠 수 있어요.") }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        draft.recruitmentName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(course.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        SourceBadge(draft.courseSource)
                    }
                    SummaryRow(Icons.Filled.CalendarMonth, "일정", scheduleSummary(draft))
                    SummaryRow(
                        Icons.Filled.Place,
                        "집합",
                        "${draft.meetingLocation.meetingTime} ${draft.meetingLocation.name} ${draft.meetingLocation.detail}"
                    )
                    SummaryRow(
                        Icons.Filled.MyLocation,
                        "좌표",
                        "${draft.meetingLocation.latitude}, ${draft.meetingLocation.longitude}"
                    )
                    // 화면기획 17-6은 인원과 조건을 두 줄로 나눈다
                    SummaryRow(
                        Icons.Filled.Group,
                        "인원",
                        "최소 ${draft.minParticipants}명 · 최대 ${draft.capacity}명"
                    )
                    SummaryRow(
                        Icons.Filled.Person,
                        "조건",
                        "${draft.minimumAge}~${draft.maximumAge}세 · " +
                            if (draft.genderCondition == "성별 무관") "성별 제한 없음" else draft.genderCondition
                    )
                    SummaryRow(Icons.Filled.Star, "비용", "1인 ${"%,d".format(draft.estimatedCostPerPerson)}원 (예상)")
                    SummaryRow(Icons.Filled.Schedule, "마감", summaryDeadlineText(draft.recruitmentDeadline))
                }
            }
        }
        item {
            InfoBanner(
                Icons.Filled.EditNote,
                if (draft.courseSource ==
                    CourseSource.Custom
                ) {
                    "호스트가 직접 만든 코스예요. 여행이 확정되기 전까지 방문지·시간·순서를 자유롭게 고칠 수 있고, 수정하면 멤버 모두에게 알림이 가요."
                } else {
                    "서비스에 등록된 코스를 그대로 가져왔어요. 경로(방문지·순서)는 수정할 수 없고, 일정·집합 장소·인원 조건은 마감 전까지 바꿀 수 있어요."
                },
                neutral = draft.courseSource == CourseSource.Linked
            )
        }
        item {
            InfoBanner(
                Icons.Filled.Notifications,
                "최소 ${draft.minParticipants}명이 모이면 채팅방이 자동으로 열리고, 마감일까지 못 채우면 자연스럽게 소멸돼요."
            )
        }
    }
}

/** 화면기획 17-6의 마감 표기: "5/22(금) 23:59 · D-3" */
private fun summaryDeadlineText(deadline: String): String {
    val match = Regex("""\d{4}\.(\d{2})\.(\d{2}) \(([^)]+)\) (\d{2}:\d{2})""").find(deadline)
        ?: return deadline
    val (month, day, weekday, time) = match.destructured
    return "${month.toInt()}/${day.toInt()}($weekday) $time · D-3"
}

@Composable
fun CourseRouteScreen(
    tripId: String,
    onBack: () -> Unit,
    onOpenMeetingPoint: (String) -> Unit,
    onOpenNotices: (String) -> Unit
) {
    var trip by remember(tripId) { mutableStateOf(MockTripRepository.findTrip(tripId)) }
    var stops by remember(tripId) {
        mutableStateOf(
            trip.routeStops.ifEmpty {
                MockTripRepository.findCourseForTrip(trip).stops.toRouteStops(trip.id)
            }
        )
    }
    val confirmed = trip.statusLabel in setOf("출발확정", "마감", "종료")
    val routeEditable = trip.courseSource == CourseSource.Custom && !confirmed

    RecruitmentScaffold(
        title = "여행 경로",
        onBack = onBack,
        action = if (routeEditable) "저장" else null,
        onAction = {
            MockTripRepository.updateCustomRoute(trip.id, stops)
            trip = MockTripRepository.findTrip(trip.id)
        },
        bottom = {
            if (confirmed) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            onOpenNotices(trip.id)
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("공지로 알리기") }
                    Button(
                        onClick = {
                        },
                        enabled = false,
                        modifier = Modifier.weight(.7f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("경로 수정") }
                }
            } else if (routeEditable) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("취소")
                    }
                    Button(
                        onClick = {
                            MockTripRepository.updateCustomRoute(trip.id, stops)
                            onBack()
                        },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("course-route-save"),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("저장하고 멤버에게 알리기") }
                }
            } else {
                // 등록 코스는 경로를 못 바꾸지만 코스 교체와 집합 정보 수정은 열려 있다 (화면기획)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.height(48.dp).testTag("course-route-change-course"),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("코스 바꾸기") }
                    Button(
                        onClick = { onOpenMeetingPoint(trip.id) },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("course-route-edit-meeting"),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("집합 정보 수정") }
                }
            }
        }
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(trip.title, fontWeight = FontWeight.ExtraBold)
                    Text(
                        "${trip.scheduleDate} · ${trip.scheduleType.label} · 방문지 ${stops.size}개 · ${trip.joined}/${trip.capacity}명",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                SourceBadge(trip.courseSource)
            }
        }
        item {
            InfoBanner(
                // 경로를 못 바꾸는 상태는 모두 자물쇠로 (화면기획)
                icon = if (routeEditable) Icons.Filled.EditNote else Icons.Filled.Lock,
                text = when {
                    confirmed -> "여행이 확정돼 경로가 잠겼어요. 변경이 필요하면 채팅방 공지로 알려주세요."
                    routeEditable -> "마감 전까지 경로를 바꿀 수 있어요. 저장하면 채팅방에 변경 내역이 공지로 남아요."
                    else -> "등록된 코스의 경로는 고정돼요. 집합 정보와 모집 조건만 바꿀 수 있어요."
                },
                warning = confirmed
            )
        }
        item { RouteMapPreview(stops.size, Modifier.height(150.dp)) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Day 1", fontWeight = FontWeight.ExtraBold)
                if (!routeEditable) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "수정 불가",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        items(stops, key = { it.id }) { stop ->
            RouteStopRow(stop, stops.indexOf(stop), routeEditable) {
                if (stops.size > 2) stops = stops.filterNot { item -> item.id == stop.id }
            }
        }
        if (routeEditable) {
            item {
                OutlinedButton(onClick = {
                }, modifier = Modifier.fillMaxWidth().height(46.dp), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text("방문지 추가", Modifier.padding(start = 6.dp))
                }
            }
        }
        item {
            Text("집합 정보", fontWeight = FontWeight.ExtraBold)
        }
        item {
            LabeledValue(
                "집합 장소 · 시간",
                "${trip.meetingLocation.meetingTime} · ${trip.meetingLocation.name} ${trip.meetingLocation.detail}",
                Icons.Filled.Place,
                onClick = if (confirmed) {
                    null
                } else {
                    { onOpenMeetingPoint(trip.id) }
                }
            )
        }
    }
}

@Composable
fun NoticeHistoryScreen(tripId: String, onBack: () -> Unit) {
    var notices by remember(tripId) { mutableStateOf(MockTripRepository.noticesForTrip(tripId)) }
    val trip = remember(tripId) { MockTripRepository.findTrip(tripId) }
    val pinned = notices.filter { it.isPinned }
    val past = notices.filterNot { it.isPinned }
    val toggle: (RecruitmentNotice) -> Unit = { notice ->
        MockTripRepository.toggleNoticePinned(notice.id)
        notices = MockTripRepository.noticesForTrip(tripId).toList()
    }

    // 화면기획은 코스 이름·공지 개수 → "상단 고정 중" / "지난 공지" → 하단 CTA 순서다
    RecruitmentScaffold(
        title = "공지 이력",
        onBack = onBack,
        bottom = {
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("notice-history-create"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Add, null)
                Text("새 공지 작성 (호스트)", Modifier.padding(start = 6.dp))
            }
        }
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(trip.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                Text(
                    "공지 ${notices.size}개 · 고정 ${pinned.size} / 최대 3",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item { NoticeSectionTitle("상단 고정 중") }
        items(pinned, key = { it.id }) { notice -> NoticeCard(notice) { toggle(notice) } }
        item { NoticeSectionTitle("지난 공지") }
        items(past, key = { it.id }) { notice -> NoticeCard(notice) { toggle(notice) } }
        item {
            Text(
                "공지는 호스트만 올릴 수 있고, 고정은 최대 3개까지예요. 고정을 해제해도 이력에는 그대로 남아요.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoticeSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
private fun RecruitmentScaffold(
    title: String,
    onBack: () -> Unit,
    action: String? = null,
    onAction: () -> Unit = {},
    bottom: (@Composable () -> Unit)? = null,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로")
            }
            Text(
                title,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontWeight = FontWeight.ExtraBold
            )
            if (action ==
                null
            ) {
                Spacer(Modifier.size(40.dp))
            } else {
                Text(
                    action,
                    modifier = Modifier.clickable(onClick = onAction).padding(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
        if (bottom != null) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .55f))
            ) {
                Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp)) {
                    bottom()
                }
            }
        }
    }
}

@Composable
private fun RecruitmentStepIndicator(activeStep: Int) {
    val labels = listOf("코스", "일정", "인원", "세부", "리뷰")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        labels.forEachIndexed { index, label ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = if (index == activeStep) MaterialTheme.colorScheme.onPrimary else Color.Transparent,
                    border = BorderStroke(
                        1.dp,
                        if (index <=
                            activeStep
                        ) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (index <
                                activeStep
                            ) {
                                Icons.Filled.Check
                            } else {
                                stepIcon(index)
                            },
                            null,
                            Modifier.size(17.dp),
                            tint = if (index ==
                                activeStep
                            ) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index ==
                        activeStep
                    ) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

private fun stepIcon(index: Int): ImageVector = listOf(
    Icons.Filled.Route,
    Icons.Filled.CalendarMonth,
    Icons.Filled.Group,
    Icons.Filled.EditNote,
    Icons.Filled.Star
)[index]

@Composable
private fun SectionIntro(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
        Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CourseSourceChoice(
    source: CourseSource,
    selected: Boolean,
    title: String,
    body: String,
    policy: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(
            onClick = onClick
        ).testTag("course-source-${source.name.lowercase()}"),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (source ==
                    CourseSource.Linked
                ) {
                    Icons.Filled.Map
                } else {
                    Icons.Filled.Route
                },
                null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(Modifier.padding(start = 12.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontWeight = FontWeight.ExtraBold)
                Text(
                    body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    policy,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            if (selected) Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable private fun SearchLikeField(text: String) {
    Surface(
        Modifier.fillMaxWidth(),
        RoundedCornerShape(10.dp),
        MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(Modifier.height(44.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Search, null, Modifier.size(18.dp))
            Text(
                text,
                Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CompactCourseChoice(
    title: String,
    subtitle: String,
    selected: Boolean,
    course: TripCourse? = null,
    sourceLabel: String? = null,
    onClick: () -> Unit
) {
    Surface(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        RoundedCornerShape(12.dp),
        if (selected) MoyeoTheme.tints.primaryTint else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (course != null) {
                CourseScenicPanel(
                    course = course,
                    modifier = Modifier.size(54.dp),
                    cornerRadius = 9.dp
                )
            } else {
                Icon(Icons.Filled.Map, null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(Modifier.padding(start = 10.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (sourceLabel != null) {
                    Text(
                        text = sourceLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Icon(
                if (selected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                contentDescription = null,
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        }
    }
}

@Composable
private fun InfoBanner(icon: ImageVector, text: String, warning: Boolean = false, neutral: Boolean = false) {
    val tints = MoyeoTheme.tints
    val container = when {
        warning -> tints.warningTint
        neutral -> MaterialTheme.colorScheme.surfaceVariant
        else -> tints.primaryTint
    }
    val content = when {
        warning -> tints.onWarningTint
        neutral -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> tints.onPrimaryTint
    }
    Surface(
        Modifier.fillMaxWidth(),
        RoundedCornerShape(10.dp),
        container,
        border = if (neutral) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, null, Modifier.size(18.dp), tint = content)
            Text(
                text,
                Modifier.padding(start = 10.dp),
                style = MaterialTheme.typography.bodySmall,
                color = content,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun RouteMapPreview(stopCount: Int, modifier: Modifier = Modifier) {
    val tints = MoyeoTheme.tints
    Canvas(modifier.fillMaxWidth().background(tints.mapGreen, RoundedCornerShape(12.dp)).padding(20.dp)) {
        val count = stopCount.coerceAtLeast(1)
        val points = (0 until count).map { i ->
            Offset(
                size.width * (.12f + .76f * i / count.coerceAtLeast(2)),
                size.height * (.78f - .55f * i / count.coerceAtLeast(2))
            )
        }
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(path, Color(0xFF4E9B6B), style = Stroke(width = 8f, cap = StrokeCap.Round))
        points.forEachIndexed { index, point ->
            drawCircle(Color(0xFF4E9B6B), 15.dp.toPx(), point)
            drawCircle(Color(0xFFF4F8F5), 3.dp.toPx(), point)
        }
    }
}

@Composable
private fun RouteStopRow(stop: RouteStop, index: Int, editable: Boolean, onRemove: () -> Unit) {
    Surface(
        Modifier.fillMaxWidth(),
        RoundedCornerShape(10.dp),
        MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .6f))
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (editable) Icons.Filled.DragHandle else Icons.Filled.Lock,
                null,
                Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(Modifier.padding(start = 10.dp).size(28.dp), CircleShape, MaterialTheme.colorScheme.primary) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "${index + 1}",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            Column(Modifier.padding(start = 10.dp).weight(1f)) {
                Row {
                    Text(
                        stop.time,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        stop.name,
                        Modifier.padding(start = 8.dp),
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    stop.memo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (editable) {
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Close, "삭제", Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ScheduleTypeButton(
    label: String,
    subtitle: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier.clickable(onClick = onClick),
        RoundedCornerShape(8.dp),
        if (selected) MaterialTheme.colorScheme.background else Color.Transparent,
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontWeight = FontWeight.ExtraBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LabeledValue(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    detail: String? = null,
    onClick: (() -> Unit)? = null,
    tag: String? = null
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Surface(
            Modifier.fillMaxWidth().then(
                if (tag ==
                    null
                ) {
                    Modifier
                } else {
                    Modifier.testTag(tag)
                }
            ).then(
                if (onClick ==
                    null
                ) {
                    Modifier
                } else {
                    Modifier.clickable(onClick = onClick)
                }
            ),
            RoundedCornerShape(10.dp),
            MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                Modifier.heightIn(min = 52.dp).padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(Modifier.padding(start = 10.dp).weight(1f)) {
                    Text(
                        value,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (detail != null) {
                        Text(
                            detail,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (onClick !=
                    null
                ) {
                    Icon(Icons.Filled.ChevronRight, null, Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable private fun SummaryStrip(text: String) {
    Surface(
        Modifier.fillMaxWidth(),
        RoundedCornerShape(10.dp),
        MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(text, Modifier.padding(14.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable private fun HorizontalDividerLine() {
    androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .5f))
}

@Composable private fun SourceBadge(source: CourseSource) {
    val tints = MoyeoTheme.tints
    val linked = source == CourseSource.Linked
    Surface(
        shape = RoundedCornerShape(50),
        color = if (linked) MaterialTheme.colorScheme.surfaceVariant else tints.primaryTint,
        border = BorderStroke(
            1.dp,
            if (linked) tints.softLine else MaterialTheme.colorScheme.primary.copy(alpha = .5f)
        )
    ) {
        Row(
            Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (linked) Icons.Filled.Lock else Icons.Filled.EditNote,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = if (linked) MaterialTheme.colorScheme.onSurfaceVariant else tints.onPrimaryTint
            )
            Text(
                source.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (linked) MaterialTheme.colorScheme.onSurfaceVariant else tints.onPrimaryTint,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable private fun SummaryRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, Modifier.size(17.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            label,
            Modifier.padding(start = 10.dp).width(48.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun NoticeCard(notice: RecruitmentNotice, onTogglePin: () -> Unit) {
    Surface(
        Modifier.fillMaxWidth(),
        RoundedCornerShape(10.dp),
        MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Description,
                    null,
                    Modifier.size(14.dp),
                    tint = if (notice.isPinned) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    notice.title,
                    Modifier.weight(1f).padding(start = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (notice.isPinned) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = FontWeight.ExtraBold
                )
                // 고정 여부는 배지로, 켜고 끄기는 아래 링크로 (화면기획과 같은 표기)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (notice.isPinned) {
                        MoyeoTheme.tints.primaryTint
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ) {
                    Text(
                        if (notice.isPinned) "📌 고정" else "고정 해제됨",
                        Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (notice.isPinned) {
                            MoyeoTheme.tints.onPrimaryTint
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(notice.body, style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${notice.author} · ${notice.createdAt}",
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    if (notice.isPinned) "수정" else "다시 고정",
                    Modifier.clickable(onClick = onTogglePin).padding(4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

private fun List<String>.toRouteStops(prefix: String): List<RouteStop> = mapIndexed { index, name ->
    RouteStop(
        "$prefix-stop-${index + 1}",
        time = listOf("09:00", "10:30", "14:00", "16:30").getOrElse(index) {
            "17:${index}0"
        },
        name = name,
        memo = listOf("집합 장소", "대전사 - 제3폭포", "왕버들 산책로", "늦은 점심").getOrElse(index) { "자유 관람" }
    )
}
private fun scheduleSummary(draft: RecruitmentDraft): String = if (draft.scheduleType ==
    TripScheduleType.DayTrip
) {
    "${compactDate(draft.travelDate)} 당일치기 · ${draft.startTime} - ${draft.endTime}"
} else {
    "${compactDate(draft.travelDate)} - ${compactDate(draft.endDate.orEmpty())}"
}

private fun compactDate(value: String): String {
    val match = Regex("""\d{4}\.\s*(\d{1,2})\.\s*(\d{1,2})\s*(\([^)]+\))""").find(value)
        ?: return value
    return "${match.groupValues[1].toInt()}/${match.groupValues[2].toInt()}${match.groupValues[3]}"
}
