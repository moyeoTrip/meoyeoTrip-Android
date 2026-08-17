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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.hanchae.moyeotrip.data.CourseSource
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.data.RecruitmentDraft
import kr.hanchae.moyeotrip.data.RecruitmentNotice
import kr.hanchae.moyeotrip.data.RouteStop
import kr.hanchae.moyeotrip.data.TripRecruitment
import kr.hanchae.moyeotrip.data.TripScheduleType

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
                    subtitle = "${course.region} · ${course.duration} · 방문지 ${course.stops.size}",
                    selected = selected,
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
fun CustomCourseScreen(draftId: String, onBack: () -> Unit, onContinue: (String) -> Unit) {
    var draft by remember(draftId) { mutableStateOf(MockTripRepository.findRecruitmentDraft(draftId)) }

    RecruitmentScaffold(
        title = "코스 직접 만들기",
        onBack = onBack,
        bottom = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onBack, modifier = Modifier.height(48.dp)) { Text("취소") }
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
        item { SearchLikeField("방문지 검색 (TourAPI · 경북 22개 시·군)") }
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
            ) { Text("+ 다음 날 추가 (1박 이상일 때)") }
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
                OutlinedButton(onClick = onBack, modifier = Modifier.height(48.dp)) { Text("이전") }
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
            LabeledValue(
                label = "집합 장소 · 집합 시간 *",
                value =
                    "${draft.meetingLocation.meetingTime} ${draft.meetingLocation.name} " +
                        draft.meetingLocation.detail,
                icon = Icons.Filled.Place,
                tag = "create-schedule-meeting"
            )
        }
    }
}

@Composable
fun CreatePeopleScreen(draftId: String, onBack: () -> Unit, onContinue: (String) -> Unit) {
    var draft by remember(draftId) { mutableStateOf(MockTripRepository.findRecruitmentDraft(draftId)) }

    RecruitmentScaffold(
        title = "모집 만들기 (3/5)",
        onBack = onBack,
        bottom = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.height(48.dp)) { Text("이전") }
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
        item { SectionIntro("인원 정하기", "최소 출발 인원과 최대 모집 인원을 정해주세요.") }
        item {
            ParticipantCounter(
                label = "최소 출발 인원",
                value = draft.minParticipants,
                decreaseEnabled = draft.minParticipants > 2,
                increaseEnabled = draft.minParticipants < draft.capacity,
                onDecrease = { draft = draft.copy(minParticipants = draft.minParticipants - 1) },
                onIncrease = { draft = draft.copy(minParticipants = draft.minParticipants + 1) }
            )
        }
        item {
            ParticipantCounter(
                label = "최대 모집 인원",
                value = draft.capacity,
                decreaseEnabled = draft.capacity > maxOf(3, draft.minParticipants),
                increaseEnabled = draft.capacity < 12,
                onDecrease = { draft = draft.copy(capacity = draft.capacity - 1) },
                onIncrease = { draft = draft.copy(capacity = draft.capacity + 1) }
            )
        }
        item {
            InfoBanner(
                icon = Icons.Filled.Notifications,
                text = "최소 인원이 모이면 채팅방이 자동으로 열려요. 모집 마감 전까지 정원을 채울 수 있어요."
            )
        }
    }
}

@Composable
private fun ParticipantCounter(
    label: String,
    value: Int,
    decreaseEnabled: Boolean,
    increaseEnabled: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            OutlinedButton(
                onClick = onDecrease,
                enabled = decreaseEnabled,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(40.dp),
                shape = CircleShape
            ) { Text("−", fontSize = 20.sp) }
            Text("${value}명", fontWeight = FontWeight.ExtraBold, modifier = Modifier.width(42.dp))
            OutlinedButton(
                onClick = onIncrease,
                enabled = increaseEnabled,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(40.dp),
                shape = CircleShape
            ) { Text("+", fontSize = 20.sp) }
        }
    }
}

@Composable
fun CreateMeetPointScreen(draftId: String, onBack: () -> Unit, onSave: (String) -> Unit) {
    var draft by remember(draftId) { mutableStateOf(MockTripRepository.findRecruitmentDraft(draftId)) }
    var query by rememberSaveable { mutableStateOf(draft.meetingLocation.name) }
    var detail by rememberSaveable { mutableStateOf("터미널 정문 앞") }

    RecruitmentScaffold(
        title = "모집 만들기 (4/5)",
        onBack = onBack,
        bottom = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.height(48.dp)) { Text("이전") }
                Button(
                    onClick = {
                        draft =
                            draft.copy(
                                meetingLocation =
                                    draft.meetingLocation.copy(
                                        name = query.ifBlank { "청송 시외버스터미널" },
                                        detail = detail
                                    )
                            )
                        MockTripRepository.updateRecruitmentDraft(draft)
                        onSave(draft.id)
                    },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("meeting-point-save"),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("다음") }
            }
        }
    ) {
        item { RecruitmentStepIndicator(activeStep = 3) }
        item { SectionIntro("집합 장소 정하기", "검색하거나 지도의 핀을 움직여 정확한 위치를 알려주세요.") }
        item {
            Box {
                RouteMapPreview(stopCount = 1, modifier = Modifier.fillMaxWidth().height(244.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    placeholder = { Text("장소 검색 (TourAPI)") },
                    singleLine = true,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .background(MaterialTheme.colorScheme.surface)
                            .testTag("meeting-point-search"),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("터미널 정문 앞", "2번 출구", "주차장 입구").forEach { option ->
                    OutlinedButton(
                        onClick = { detail = option },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            option,
                            color = if (detail == option) {
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

@Composable
fun CreateSummaryScreen(draftId: String, onBack: () -> Unit, onCreated: (TripRecruitment) -> Unit) {
    val draft = remember(draftId) { MockTripRepository.findRecruitmentDraft(draftId) }
    val course = MockTripRepository.findCourse(draft.selectedCourseId)

    RecruitmentScaffold(
        title = "모집 만들기 (5/5)",
        onBack = onBack,
        bottom = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.height(48.dp)) { Text("이전") }
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(course.title, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
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
                    SummaryRow(
                        Icons.Filled.Group,
                        "인원",
                        "최소 ${draft.minParticipants}명 · 최대 ${draft.capacity}명 · 성별 제한 없음"
                    )
                    SummaryRow(Icons.Filled.Schedule, "마감", draft.recruitmentDeadline)
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
                    OutlinedButton(onClick = {
                        onOpenNotices(trip.id)
                    }, modifier = Modifier.weight(1f).height(48.dp)) { Text("공지로 알리기") }
                    Button(onClick = {
                    }, enabled = false, modifier = Modifier.weight(.7f).height(48.dp)) { Text("경로 수정") }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onBack, modifier = Modifier.height(48.dp)) { Text("취소") }
                    Button(
                        onClick = {
                            if (routeEditable) MockTripRepository.updateCustomRoute(trip.id, stops)
                            onBack()
                        },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("course-route-save")
                    ) { Text(if (routeEditable) "저장하고 멤버에게 알리기" else "확인") }
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
                icon = if (confirmed) Icons.Filled.Lock else Icons.Filled.EditNote,
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
                    Text(
                        "수정 불가",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth().height(46.dp)) {
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
    RecruitmentScaffold(title = "공지 내역", onBack = onBack) {
        item { SectionIntro("채팅방 공지", "상단 고정은 최대 3개까지 선택할 수 있어요.") }
        items(notices, key = { it.id }) { notice ->
            NoticeCard(notice) {
                MockTripRepository.toggleNoticePinned(notice.id)
                notices = MockTripRepository.noticesForTrip(tripId).toList()
            }
        }
    }
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
private fun CompactCourseChoice(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        RoundedCornerShape(10.dp),
        if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Map, null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.padding(start = 10.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selected) Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun InfoBanner(icon: ImageVector, text: String, warning: Boolean = false, neutral: Boolean = false) {
    val container = when {
        warning -> Color(0xFFFFF2D5)
        neutral -> MaterialTheme.colorScheme.surfaceVariant
        else -> Color(0xFFEEF7F1)
    }
    val content = when {
        warning -> Color(0xFF745318)
        neutral -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> Color(0xFF264332)
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
    Canvas(modifier.fillMaxWidth().background(Color(0xFFE5EEDB), RoundedCornerShape(12.dp)).padding(20.dp)) {
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
            drawCircle(Color.White, 3.dp.toPx(), point)
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
            Row(Modifier.height(52.dp).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    value,
                    Modifier.padding(start = 10.dp).weight(1f),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.inverseOnSurface) {
        Text(
            source.label,
            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.inverseSurface,
            fontWeight = FontWeight.Bold
        )
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
                Text(notice.title, Modifier.weight(1f), fontWeight = FontWeight.ExtraBold)
                Text(
                    if (notice.isPinned) "고정됨" else "고정",
                    Modifier.clickable(onClick = onTogglePin).padding(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                notice.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "${notice.author} · ${notice.createdAt}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
