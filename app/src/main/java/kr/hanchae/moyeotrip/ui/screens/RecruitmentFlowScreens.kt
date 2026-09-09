package kr.hanchae.moyeotrip.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.R
import kr.hanchae.moyeotrip.data.CourseSource
import kr.hanchae.moyeotrip.data.RecruitmentDraft
import kr.hanchae.moyeotrip.data.RecruitmentDraftStore
import kr.hanchae.moyeotrip.data.RouteStop
import kr.hanchae.moyeotrip.data.ServerDataDependencies
import kr.hanchae.moyeotrip.data.TripScheduleType
import kr.hanchae.moyeotrip.data.api.MultipartFile
import kr.hanchae.moyeotrip.data.courses.TravelCourse
import kr.hanchae.moyeotrip.data.rooms.ChatRoomDetail
import kr.hanchae.moyeotrip.data.rooms.RoomNotice
import kr.hanchae.moyeotrip.data.rooms.RoomNotices
import kr.hanchae.moyeotrip.data.rooms.roomDateTimeClockText
import kr.hanchae.moyeotrip.data.rooms.toNewChatRoom
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.CourseRouteMap
import kr.hanchae.moyeotrip.ui.components.CourseRoutePoint
import kr.hanchae.moyeotrip.ui.components.KakaoMapView
import kr.hanchae.moyeotrip.ui.components.MapUnavailablePlaceholder
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyState
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyText
import kr.hanchae.moyeotrip.ui.components.MoyeoLatLng
import kr.hanchae.moyeotrip.ui.components.MoyeoLinearProgress
import kr.hanchae.moyeotrip.ui.components.MoyeoPlaceholderShape
import kr.hanchae.moyeotrip.ui.components.ServerListState
import kr.hanchae.moyeotrip.ui.components.moyeoDashedOutline
import kr.hanchae.moyeotrip.ui.components.moyeoNoticeTime
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

/**
 * 17-1 코스 선택. 후보는 서버 공개 코스(GET travel-courses/public)뿐이다 —
 * 방 생성(POST chat-rooms)이 서버 `courseId` 를 요구하므로 그 밖의 코스로는 모집을 열 수 없다.
 */
@Composable
fun RecruitmentCourseSourceScreen(
    courseId: String,
    onBack: () -> Unit,
    onOpenCustomCourse: (String) -> Unit,
    onOpenSchedule: (String) -> Unit
) {
    var draft by remember(courseId) { mutableStateOf(RecruitmentDraftStore.draft(courseId)) }
    val server = LocalServerData.current
    var serverCourses by remember(server) { mutableStateOf<ServerListState<TravelCourse>>(ServerListState.Loading) }
    var reloadKey by remember(server) { mutableIntStateOf(0) }
    LaunchedEffect(server, reloadKey) {
        if (server == null) {
            serverCourses = ServerListState.Loaded(emptyList())
            return@LaunchedEffect
        }
        serverCourses = runCatching { server.courses.publicCourses() }
            .fold({ ServerListState.Loaded(it) }, { ServerListState.Failed })
        // 라우트가 `srv-{id}` 로 코스를 지정해 들어왔으면 그 코스를 미리 골라 둔다
        val requested = courseId.removePrefix("srv-").toLongOrNull()
        val preselected = (serverCourses as? ServerListState.Loaded)?.items
            ?.firstOrNull { it.courseId == requested }
        if (preselected != null && draft.serverCourseId == null) {
            draft = draft.applyServerCourse(preselected)
            RecruitmentDraftStore.update(draft)
        }
    }

    RecruitmentScaffold(
        title = "모집 만들기 (1/5)",
        onBack = onBack,
        bottom = {
            Button(
                onClick = {
                    RecruitmentDraftStore.update(draft)
                    if (draft.courseSource == CourseSource.Custom) {
                        onOpenCustomCourse(draft.id)
                    } else {
                        onOpenSchedule(draft.id)
                    }
                },
                enabled = draft.courseSource == CourseSource.Custom || draft.serverCourseId != null,
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
                body = "·경북나드리 기반으로 검증된 동선을 그대로 가져와요.",
                policy = "경로 수정 불가 · 집합 정보만 설정",
                onClick = { draft = draft.copy(courseSource = CourseSource.Linked) }
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
            item { SearchLikeField("등록된 코스 검색") }
            val state = serverCourses
            when {
                server == null -> item {
                    MoyeoEmptyState(MoyeoEmptyText.SIGN_IN_EXPLORE, testTag = "create-course-signed-out")
                }

                state is ServerListState.Loading -> item { MoyeoEmptyState(MoyeoEmptyText.LOADING) }

                state is ServerListState.Failed -> item {
                    MoyeoEmptyState(MoyeoEmptyText.FAILED, onRetry = { reloadKey++ })
                }

                state is ServerListState.Loaded && state.items.isEmpty() -> item {
                    MoyeoEmptyState("아직 공개된 코스가 없어요.", testTag = "create-course-empty")
                }

                state is ServerListState.Loaded -> items(state.items, key = { it.courseId }) { course ->
                    CompactCourseChoice(
                        title = course.title,
                        subtitle = listOfNotNull(
                            course.travelTime,
                            course.distanceKm?.let { "${it}km" },
                            "방문지 ${course.places.size}"
                        ).joinToString(" · "),
                        // 서버가 코스 작성자를 주면 화면기획의 "여행자 코스"에 해당한다
                        sourceLabel = if (course.creatorNickname != null) "여행자 코스" else "모여트립 추천",
                        selected = course.courseId == draft.serverCourseId,
                        thumbnail = course.thumbnail,
                        onClick = { draft = draft.applyServerCourse(course) }
                    )
                }
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
                    modifier = Modifier
                        .fillMaxWidth()
                        // 아직 코스를 짜지 않은 자리 — 기획·웹은 점선이다.
                        .moyeoDashedOutline(MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(12.dp),
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

/** 서버 코스의 방문지 → 초안 방문지. 이름·시각·일차·좌표를 그대로 옮긴다. */
private fun TravelCourse.toRouteStops(): List<RouteStop> = places.mapIndexed { index, place ->
    RouteStop(
        id = "srv-$courseId-stop-${place.contentId}-$index",
        day = place.dayNumber,
        time = place.visitTime?.take(5).orEmpty(),
        name = place.title,
        memo = "",
        latitude = place.latitude,
        longitude = place.longitude
    )
}

/** 서버 코스를 고른 초안. 방문지는 서버 방문지(좌표 포함)를 그대로 옮긴다. */
private fun RecruitmentDraft.applyServerCourse(course: TravelCourse): RecruitmentDraft = copy(
    serverCourseId = course.courseId,
    serverCourseTitle = course.title,
    routeStops = course.toRouteStops()
)

@Composable
fun CustomCourseScreen(
    draftId: String,
    onBack: () -> Unit,
    onOpenPlaceSearch: (String) -> Unit,
    onContinue: (String) -> Unit,
    startingCourseId: Long? = null
) {
    var draft by remember(draftId) { mutableStateOf(RecruitmentDraftStore.draft(draftId)) }
    val server = LocalServerData.current

    // `?courseId=` 로 들어오면 **그 코스를 불러온 상태**로 에디터를 연다 (딥링크·QA 진입).
    // 담기는 값은 서버가 준 방문지 이름·방문 시각·일차·좌표뿐이다 — 지어낸 방문지를 담지 않는다.
    // 이미 담은 방문지가 있으면 덮어쓰지 않는다(사용자가 짜던 코스를 지우면 안 된다).
    LaunchedEffect(draftId, startingCourseId, server) {
        val courseId = startingCourseId ?: return@LaunchedEffect
        if (server == null || draft.routeStops.isNotEmpty()) return@LaunchedEffect
        val course = runCatching { server.courses.course(courseId) }.getOrNull() ?: return@LaunchedEffect
        val stops = course.toRouteStops()
        if (stops.isEmpty()) return@LaunchedEffect
        // **방문지만** 옮긴다. `serverCourseId` 까지 넣으면 초안이 「연동 코스」가 되어
        // 17-5 가 PUBLIC 으로 만들어 버린다 — 여기서 고친 코스는 새 커스텀 코스다.
        draft = draft
            .copy(routeStops = stops, dayCount = stops.maxOf { it.day })
            .also(RecruitmentDraftStore::update)
    }

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
                        RecruitmentDraftStore.update(draft)
                        onContinue(draft.id)
                    },
                    enabled = draft.routeStops.size >= RecruitmentDraftStore.MIN_ROUTE_STOPS,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("custom-course-continue"),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("이 코스로 계속하기") }
            }
        }
    ) {
        // 방문지 좌표(TourAPI)를 그대로 실지도에 올린다. 좌표가 하나도 없으면 지도 자리를 비운다.
        item { CourseRouteMap(points = draft.routeStops.toRoutePoints()) }
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clickable { onOpenPlaceSearch(draft.id) }
                    .testTag("custom-course-place-search")
            ) {
                SearchLikeField("방문지 검색")
            }
        }
        // 방문지는 담긴 날(RouteStop.day)별로 묶어 보여준다. 예전에는 "Day 1" 머리글 하나에
        // 전부 쏟아 넣고 "+ 다음 날 추가" 는 눌러도 아무 일이 없어서, 날을 나눌 방법이 없었다.
        for (day in 1..draft.dayCount) {
            val dayStops = draft.routeStops.filter { it.day == day }
            item(key = "day-header-$day") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Day $day", fontWeight = FontWeight.ExtraBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (day == 1) {
                                "${dayStops.size}개 방문지 · 최소 2개"
                            } else {
                                "${dayStops.size}개 방문지"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        // 잘못 늘린 날을 되돌릴 방법이 필요하다. 방문지가 남아 있는 날은
                        // 지우면 그 방문지가 조용히 사라지므로 마지막 빈 날만 지울 수 있다.
                        if (day == draft.dayCount && day > 1 && dayStops.isEmpty()) {
                            TextButton(
                                onClick = {
                                    draft = draft.copy(dayCount = draft.dayCount - 1)
                                    RecruitmentDraftStore.update(draft)
                                },
                                modifier = Modifier.testTag("custom-course-remove-day")
                            ) { Text("이 날 삭제", style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                }
            }
            items(dayStops, key = { it.id }) { stop ->
                RouteStopRow(
                    stop = stop,
                    // 순번은 코스 전체 기준이다 — 서버 로드맵도 전체 순서를 쓴다
                    index = draft.routeStops.indexOfFirst { it.id == stop.id },
                    editable = true,
                    onRemove = {
                        draft = draft.copy(routeStops = draft.routeStops.filterNot { it.id == stop.id })
                        RecruitmentDraftStore.update(draft)
                    }
                )
            }
        }
        item {
            // 방문지는 예시 이름을 채우는 게 아니라 방문지 검색(TourAPI)에서 고른다.
            // 담긴 방문지는 마지막 날(Day dayCount)로 들어간다.
            OutlinedButton(
                onClick = {
                    RecruitmentDraftStore.update(draft)
                    onOpenPlaceSearch(draft.id)
                },
                enabled = draft.routeStops.size < RecruitmentDraftStore.MAX_ROUTE_STOPS,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .moyeoDashedOutline(MaterialTheme.colorScheme.outlineVariant)
                    .testTag("custom-course-add-stop"),
                // 「담기」 자리는 **점선 박스**다 — 기획·웹·iOS 모두 점선이고 반지름 12 다
                // (iOS `StrokeStyle(lineWidth: 1, dash: [4])`). 안드로이드만 실선 8 이었다.
                shape = RoundedCornerShape(12.dp),
                border = null
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text(
                    if (draft.dayCount > 1) "Day ${draft.dayCount} 방문지 추가" else "방문지 추가",
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
        item {
            OutlinedButton(
                onClick = {
                    draft = draft.copy(dayCount = draft.dayCount + 1)
                    RecruitmentDraftStore.update(draft)
                },
                enabled = draft.dayCount < RecruitmentDraftStore.MAX_COURSE_DAYS,
                // 위 「방문지 추가」와 같은 보조 버튼 높이(46)다 — 값을 주지 않으면
                // Material3 기본값 40dp 이 쓰여 안드로이드만 낮았다.
                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("custom-course-add-day"),
                // 반지름은 네 표면이 같아야 한다 — 기획·웹·iOS 가 12 다.
                shape = RoundedCornerShape(12.dp)
            ) { Text("+ 다음 날 추가 (1박 이상일 때)") } // 글자에 '+'가 있으니 아이콘은 두지 않는다
        }
        item {
            InfoBanner(
                // 기획의 안내 박스 아이콘은 **반짝임**(`sparkle`)이다 — iOS 도 `sparkles` 를 쓴다.
                icon = Icons.Filled.AutoAwesome,
                text = "직접 만든 코스는 여행이 확정되기 전까지 호스트가 언제든 고칠 수 있어요. 수정하면 채팅방 멤버 모두에게 알림이 가요."
            )
        }
    }
}

@Composable
fun CreateScheduleScreen(draftId: String, onBack: () -> Unit, onContinue: (String) -> Unit) {
    var draft by remember(draftId) { mutableStateOf(RecruitmentDraftStore.draft(draftId)) }

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
                        RecruitmentDraftStore.update(draft)
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
fun CreatePeopleScreen(
    draftId: String,
    onBack: () -> Unit,
    onContinue: (String) -> Unit,
    /**
     * 최대 인원을 미리 정해 두고 열 때 쓴다 (17-4a 4명 · 17-4b 10명).
     * null 이면 초안에 저장된 값을 그대로 쓴다 — 17-4 기본 화면이 그렇다.
     */
    initialCapacity: Int? = null
) {
    var draft by remember(draftId, initialCapacity) {
        val stored = RecruitmentDraftStore.draft(draftId)
        val seeded = if (initialCapacity == null) {
            stored
        } else {
            stored.copy(
                capacity = initialCapacity,
                // 최소 인원은 최대보다 최소 1 작아야 하고, 정책상 3명 아래로는 못 내려간다
                minParticipants = stored.minParticipants.coerceAtMost((initialCapacity - 1).coerceAtLeast(3))
            )
        }
        mutableStateOf(seeded)
    }
    var showAgeSheet by rememberSaveable { mutableStateOf(false) }

    if (showAgeSheet) {
        ModalBottomSheet(onDismissRequest = { showAgeSheet = false }, containerColor = MoyeoTheme.sheetSurface) {
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
                        RecruitmentDraftStore.update(draft)
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
            MoyeoLinearProgress(
                progress = minimum / capacity.coerceAtLeast(1).toFloat(),
                modifier = Modifier.fillMaxWidth()
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
    var draft by remember(draftId) { mutableStateOf(RecruitmentDraftStore.draft(draftId)) }
    var query by rememberSaveable { mutableStateOf(draft.meetingLocation.name) }
    var detail by rememberSaveable { mutableStateOf(draft.meetingLocation.detail) }
    // 지도를 끌면 중앙 좌표가 바뀐다. 초안에 좌표가 없으면 경북 중심에서 시작한다.
    var pinned by remember(draftId) {
        mutableStateOf(
            if (draft.meetingLocation.latitude == 0.0 && draft.meetingLocation.longitude == 0.0) {
                GyeongbukMapCenter
            } else {
                MoyeoLatLng(draft.meetingLocation.latitude, draft.meetingLocation.longitude)
            }
        )
    }

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
                                name = query.trim(),
                                detail = detail.trim(),
                                latitude = pinned.latitude,
                                longitude = pinned.longitude
                            )
                        )
                        RecruitmentDraftStore.update(draft)
                        onSave(draft.id)
                    },
                    enabled = query.isNotBlank(),
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
                // 카카오 실지도 — 지도를 끌면 화면 중앙(아래 핀 위치)의 위경도가 좌표 칸에 반영된다.
                KakaoMapView(
                    center = pinned,
                    modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(12.dp)),
                    zoomLevel = 16,
                    draggablePin = true,
                    onPinMove = { pinned = it },
                    fallback = { fallbackModifier -> MapUnavailablePlaceholder(fallbackModifier) }
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    placeholder = { Text("장소 검색") },
                    singleLine = true,
                    // `background` 에 **모양을 함께 준다.** 모양 없이 칠하면 모서리 밖까지
                    // 사각으로 채워져 둥근 입력창이 각진 판으로 보였다 (사용자 지적, 2026-09-09).
                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
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
        item { LabeledValue("집합 장소 *", query, Icons.Filled.Place) }
        item {
            // changeLog15 — 상세 안내는 추천 칩이 아니라 자유 텍스트 입력이다
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("상세 안내", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    leadingIcon = { Icon(Icons.Filled.EditNote, contentDescription = null) },
                    placeholder = { Text("만나는 위치를 자세히 남겨주세요 (예: 터미널 정문 앞)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("meeting-point-detail")
                )
            }
        }
        // 좌표는 **사용자에게 보여주지 않는다** — 지도 핀으로 위치를 알 수 있고
        // 위경도 숫자는 읽을 일이 없다 (사용자 결정, 2026-09-09).
        // 값 자체는 그대로 저장돼 채팅방 지도 카드·길 찾기에 쓰인다.
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
    var draft by remember(draftId) { mutableStateOf(RecruitmentDraftStore.draft(draftId)) }
    var recruitmentName by rememberSaveable { mutableStateOf(draft.recruitmentName) }
    var introduction by rememberSaveable { mutableStateOf(draft.note) }
    var costText by rememberSaveable { mutableStateOf(draft.estimatedCostPerPerson.toString()) }
    var approvalMode by rememberSaveable { mutableStateOf(if (draft.autoApproval) "auto" else "manual") }

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
                                estimatedCostPerPerson = costText.toIntOrNull()?.coerceAtLeast(0) ?: 0,
                                autoApproval = approvalMode == "auto"
                            )
                        RecruitmentDraftStore.update(draft)
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
                value = draft.serverCourseTitle.orEmpty(),
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
                    // 화면기획 표기는 "45,000원" — 입력값도 천 단위로 끊어 보여준다
                    value = costText.toIntOrNull()?.let { "%,d".format(it) } ?: costText,
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
                    " 기준 이 코스는 보통 4~5만원 내외예요. 참고용으로만 보여줘요.",
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

/**
 * 17-7 모집 만들기 마지막 단계 — **실제로 방을 만든다**(POST chat-rooms, multipart).
 *
 * 201 응답의 `roomId` 로 15 모집 상세([onCreatedRoom])까지 이어진다.
 * 초안이 서버에 보낼 수 있는 상태가 아니면(코스 미선택·날짜 형식 등) 버튼을 막고 이유를 보여준다.
 */
@Composable
fun CreateSummaryScreen(draftId: String, onBack: () -> Unit, onCreatedRoom: (Long) -> Unit = {}) {
    val context = LocalContext.current
    val draft = remember(draftId) { RecruitmentDraftStore.draft(draftId) }
    val server = LocalServerData.current
    val newRoom = remember(draft, server) { if (server == null) null else draft.toNewChatRoom() }
    val submitScope = rememberCoroutineScope()
    var submitting by remember(draftId) { mutableStateOf(false) }
    var submitError by remember(draftId) { mutableStateOf<String?>(null) }
    val blockingReason = RecruitmentDraftStore.validationError(draft)
        ?: if (server == null) "로그인하면 모집을 열 수 있어요." else null

    RecruitmentScaffold(
        title = "모집 만들기 (5/5)",
        onBack = onBack,
        bottom = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.height(48.dp), shape = RoundedCornerShape(12.dp)) {
                    Text("이전")
                }
                Button(
                    onClick = {
                        if (server == null || newRoom == null) {
                            submitError = blockingReason ?: "모집을 열 수 없는 초안이에요. 앞 단계를 확인해 주세요."
                            return@Button
                        }
                        submitting = true
                        submitError = null
                        submitScope.launch {
                            // 2026-08-26 서버 변경: 썸네일이 필수다(없으면 400 40041).
                            // 17 모집 만들기에는 아직 사진 선택 단계가 없으므로 기본 플레이스홀더를 올린다.
                            val thumbnail = defaultRoomThumbnail(context)
                            runCatching { server.chatRooms.createRoom(newRoom, thumbnail) }.fold(
                                onSuccess = { roomId ->
                                    submitting = false
                                    RecruitmentDraftStore.remove(draft.id)
                                    onCreatedRoom(roomId)
                                },
                                onFailure = { error ->
                                    submitting = false
                                    submitError = error.message ?: "모집을 열지 못했어요. 잠시 후 다시 시도해 주세요."
                                }
                            )
                        }
                    },
                    enabled = !submitting && blockingReason == null && newRoom != null,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("create-summary-submit"),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("모집 열기") }
            }
        }
    ) {
        (submitError ?: blockingReason)?.let { message ->
            item {
                InfoBanner(icon = Icons.Filled.Notifications, text = message, warning = true)
            }
        }
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
                        Text(
                            text = draft.serverCourseTitle.orEmpty(),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        SourceBadge(draft.courseSource)
                    }
                    SummaryRow(Icons.Filled.CalendarMonth, "일정", scheduleSummary(draft))
                    SummaryRow(
                        Icons.Filled.Place,
                        "집합",
                        // 값이 비면 구분자만 남는다 — 빈 조각은 버리고 이어 붙인다.
                        listOf(
                            draft.meetingLocation.meetingTime,
                            draft.meetingLocation.name,
                            draft.meetingLocation.detail
                        ).filter { it.isNotBlank() }.joinToString(" ")
                    )
                    // 좌표 줄은 **없앤다** — 위경도는 사용자가 읽을 값이 아니다.
                    // 게다가 지도를 끌지 않은 초안에서는 `0.0, 0.0` 이 그대로 찍혔다
                    // (17-3 에서 네 표면 다 뺀 것과 같은 이유 · 17-6 사용자 지적 2026-09-09).
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
            // 화면기획 17-7 — 등록 코스는 자물쇠 아이콘, "경로(방문지·순서)는 수정할 수 없고"만 볼드
            if (draft.courseSource == CourseSource.Custom) {
                InfoBanner(
                    Icons.Filled.EditNote,
                    "호스트가 직접 만든 코스예요. 여행이 확정되기 전까지 방문지·시간·순서를 자유롭게 고칠 수 있고, 수정하면 멤버 모두에게 알림이 가요."
                )
            } else {
                InfoBanner(
                    icon = Icons.Filled.Lock,
                    text = buildAnnotatedString {
                        append("서비스에 등록된 코스를 그대로 가져왔어요. ")
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                            append("경로(방문지·순서)는 수정할 수 없고")
                        }
                        append(", 일정·집합 장소·인원 조건은 마감 전까지 바꿀 수 있어요.")
                    },
                    neutral = true
                )
            }
        }
        item {
            // 화면기획 17-7 — 두 번째 초록 카드는 아이콘 없이 텍스트만
            InfoBanner(
                icon = null,
                text = "최소 ${draft.minParticipants}명이 모이면 채팅방이 자동으로 열리고, 마감일까지 못 채우면 자연스럽게 소멸돼요."
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

/**
 * 18-2 여행 경로 — 서버 모임(`room-{roomId}`)에 연결된 코스를 실지도로 보여준다.
 *
 * 방문지 수정은 서버 API 가 없어(코스 편집 엔드포인트 없음) 읽기 전용이다(§4 BE 요청).
 * 집합 정보 수정은 PUT chat-rooms/{id}/meeting-info 로 이어진다.
 */
@Composable
fun CourseRouteScreen(
    tripId: String,
    onBack: () -> Unit,
    onOpenMeetingPoint: (String) -> Unit,
    onOpenNotices: (String) -> Unit
) {
    val server = LocalServerData.current
    val roomId = tripId.serverRoomIdOrNull()
    if (roomId == null || server == null) {
        RecruitmentScaffold(title = "여행 경로", onBack = onBack) {
            item { MoyeoEmptyState(MoyeoEmptyText.NO_JOINED_ROOMS, testTag = "course-route-empty") }
        }
        return
    }
    var room by remember(roomId) { mutableStateOf<ChatRoomDetail?>(null) }
    var course by remember(roomId) { mutableStateOf<TravelCourse?>(null) }
    var loadFailed by remember(roomId) { mutableStateOf(false) }
    var reloadKey by remember(roomId) { mutableIntStateOf(0) }
    LaunchedEffect(roomId, server, reloadKey) {
        loadFailed = false
        room = runCatching { server.chatRooms.room(roomId) }.getOrNull()
        course = runCatching { server.courses.roomCourse(roomId) }.getOrNull()
        loadFailed = room == null
    }

    val detail = room
    val confirmed = detail != null && detail.status != "RECRUITING"

    RecruitmentScaffold(
        title = "여행 경로",
        onBack = onBack,
        bottom = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onOpenNotices("room-$roomId") },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("공지로 알리기") }
                Button(
                    onClick = { onOpenMeetingPoint("room-$roomId") },
                    enabled = !confirmed,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("course-route-edit-meeting"),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("집합 정보 수정") }
            }
        }
    ) {
        if (detail == null) {
            item {
                MoyeoEmptyState(
                    if (loadFailed) MoyeoEmptyText.FAILED else MoyeoEmptyText.LOADING,
                    onRetry = if (loadFailed) ({ reloadKey++ }) else null
                )
            }
            return@RecruitmentScaffold
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(detail.title, fontWeight = FontWeight.ExtraBold)
                Text(
                    listOfNotNull(
                        detail.startDate.takeIf(String::isNotBlank)?.replace('-', '.'),
                        course?.places?.size?.let { "방문지 ${it}개" },
                        "${detail.participantCount}/${detail.maxParticipants}명"
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            InfoBanner(
                icon = Icons.Filled.Lock,
                text = if (confirmed) {
                    "여행이 확정돼 경로가 잠겼어요. 변경이 필요하면 채팅방 공지로 알려주세요."
                } else {
                    "등록된 코스의 경로는 고정돼요. 집합 정보와 모집 조건만 바꿀 수 있어요."
                },
                warning = confirmed
            )
        }
        item { CourseRouteMap(points = course?.places.orEmpty().toRoutePoints(), height = 150.dp) }
        val places = course?.places.orEmpty()
        if (places.isEmpty()) {
            item { MoyeoEmptyState("방문지 정보가 없어요.", testTag = "course-route-no-places") }
        } else {
            item { Text("Day 1", fontWeight = FontWeight.ExtraBold) }
            items(places.size) { index ->
                val place = places[index]
                RouteStopRow(
                    stop = RouteStop(
                        id = "room-$roomId-place-$index",
                        time = place.visitTime?.take(5).orEmpty(),
                        name = place.title,
                        memo = "${place.dayNumber}일차"
                    ),
                    index = index,
                    editable = false,
                    locked = confirmed,
                    onRemove = {}
                )
            }
        }
        item { Text("집합 정보", fontWeight = FontWeight.ExtraBold) }
        item {
            LabeledValue(
                "집합 장소 · 시간",
                listOfNotNull(
                    roomDateTimeClockText(detail.meetingDateTime),
                    detail.meetingDetails?.takeIf(String::isNotBlank)
                ).joinToString(" · "),
                Icons.Filled.Place,
                onClick = if (confirmed) null else ({ onOpenMeetingPoint("room-$roomId") })
            )
        }
    }
}

@Composable
fun NoticeHistoryScreen(
    tripId: String,
    onBack: () -> Unit,
    onOpenComposer: (String) -> Unit = {},
    /** 20-3a 공지 수정 · 삭제 — 카드마다 `수정` 이 있는데 갈 곳이 없었다(정본 §6-1). */
    onOpenNoticeEdit: (String, Long) -> Unit = { _, _ -> }
) {
    // "room-{id}" 는 실서버 모임이다 — GET chat-rooms/{id}/notices 를 그대로 보여준다
    val server = LocalServerData.current
    val serverRoomId = tripId.serverRoomIdOrNull()
    if (serverRoomId == null || server == null) {
        RecruitmentScaffold(title = "공지 이력", onBack = onBack) {
            item { MoyeoEmptyState(MoyeoEmptyText.NO_NOTICES, testTag = "notice-history-empty") }
        }
        return
    }
    ServerNoticeHistory(
        roomId = serverRoomId,
        server = server,
        onBack = onBack,
        onOpenComposer = { onOpenComposer(tripId) },
        onOpenNoticeEdit = { noticeId -> onOpenNoticeEdit(tripId, noticeId) }
    )
}

/**
 * 실서버 공지 이력(화면기획 20-3) — GET chat-rooms/{id}/notices.
 *
 * 고정 토글은 PUT chat-rooms/{id}/notices/{noticeId} 다. 호스트만 보이는 동작이라
 * GET {id}/members 의 `me && host` 로 판정하고, 본문(`notice`)은 보내지 않아 고정 상태만 바뀐다.
 *
 * 카드에 **제목 줄이 없다** — 서버 모델이 `notice` 하나뿐이라 본문이 카드의 주인공이다(정본 §2).
 * 하단 CTA 는 20-2f 공지 작성 화면으로 간다. 상단 고정은 **최대 1개**다(R5-1).
 */
@Composable
private fun ServerNoticeHistory(
    roomId: Long,
    server: ServerDataDependencies,
    onBack: () -> Unit,
    onOpenComposer: () -> Unit,
    onOpenNoticeEdit: (Long) -> Unit
) {
    var room by remember(roomId) { mutableStateOf<ChatRoomDetail?>(null) }
    var notices by remember(roomId) { mutableStateOf<RoomNotices?>(null) }
    var isHost by remember(roomId) { mutableStateOf(false) }
    var pinBusy by remember(roomId) { mutableStateOf(false) }
    var pinError by remember(roomId) { mutableStateOf<String?>(null) }
    val pinScope = rememberCoroutineScope()

    LaunchedEffect(roomId, server) {
        room = runCatching { server.chatRooms.room(roomId) }.getOrNull()
        notices = runCatching { server.chatRooms.notices(roomId) }.getOrNull()
        isHost = runCatching { server.chatRooms.members(roomId) }.getOrNull()
            ?.members
            ?.any { it.me && it.host } == true
    }
    val pinned = notices?.pinned.orEmpty()
    val past = notices?.unpinned.orEmpty()

    fun togglePinned(notice: RoomNotice) {
        if (pinBusy) return
        pinBusy = true
        pinError = null
        pinScope.launch {
            runCatching { server.chatRooms.updateNotice(roomId, notice.noticeId, pinned = !notice.pinned) }
                .onSuccess { notices = runCatching { server.chatRooms.notices(roomId) }.getOrNull() ?: notices }
                .onFailure { error -> pinError = error.message ?: "고정 상태를 바꾸지 못했어요." }
            pinBusy = false
        }
    }

    RecruitmentScaffold(
        title = "공지 이력",
        onBack = onBack,
        bottom = if (!isHost) {
            null
        } else {
            {
                Button(
                    onClick = onOpenComposer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("notice-create"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Add, null, Modifier.size(18.dp))
                    Text("새 공지 작성 (호스트)", Modifier.padding(start = 6.dp), fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    room?.title.orEmpty(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    noticeHeaderText(total = pinned.size + past.size, pinned = pinned.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        pinError?.let { message ->
            item {
                Text(
                    message,
                    modifier = Modifier.testTag("server-notice-pin-error"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        if (pinned.isNotEmpty()) {
            item { NoticeSectionTitle("상단 고정 중") }
            items(pinned, key = { it.noticeId }) { notice ->
                ServerNoticeCard(
                    notice = notice,
                    canPin = isHost,
                    pinBusy = pinBusy,
                    onTogglePinned = { togglePinned(notice) },
                    onEdit = { onOpenNoticeEdit(notice.noticeId) }
                )
            }
        }
        if (past.isNotEmpty()) {
            item { NoticeSectionTitle("지난 공지") }
            items(past, key = { it.noticeId }) { notice ->
                ServerNoticeCard(
                    notice = notice,
                    canPin = isHost,
                    pinBusy = pinBusy,
                    onTogglePinned = { togglePinned(notice) },
                    onEdit = { onOpenNoticeEdit(notice.noticeId) }
                )
            }
        }
        if (notices != null && pinned.isEmpty() && past.isEmpty()) {
            item { MoyeoEmptyState(MoyeoEmptyText.NO_NOTICES, testTag = "notice-history-empty") }
        }
        if (notices != null) {
            item {
                Text(
                    "공지는 호스트만 올릴 수 있고, 상단 고정은 하나만 둘 수 있어요. " +
                        "새로 고정하면 먼저 고정된 공지가 풀려요. 고정을 해제해도 이력에는 그대로 남아요.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ServerNoticeCard(
    notice: RoomNotice,
    canPin: Boolean = false,
    pinBusy: Boolean = false,
    onTogglePinned: () -> Unit = {},
    onEdit: () -> Unit = {}
) {
    Surface(
        Modifier.fillMaxWidth().testTag("server-notice-${notice.noticeId}"),
        RoundedCornerShape(10.dp),
        MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            // 아이콘과 배지가 **첫 줄**을 차지하고 본문은 그 아래 한 폭을 다 쓴다.
            // 예전에는 셋을 한 줄에 넣어 본문이 배지 옆 좁은 칸으로 밀려 두 줄로 접혔고,
            // 그래서 카드가 낮아져 안드로이드만 화면 아래가 텅 비었다 (20-3, 사용자 지적 2026-09-09).
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Description,
                    null,
                    Modifier.size(14.dp),
                    tint = if (notice.pinned) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (notice.pinned) {
                        MoyeoTheme.tints.primaryTint
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ) {
                    Text(
                        if (notice.pinned) "📌 고정" else "고정 해제됨",
                        Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (notice.pinned) {
                            MoyeoTheme.tints.onPrimaryTint
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            // 제목 줄이 없다 — 본문이 카드의 주인공이다(정본 §2). 그래서 제목체가 아니라 본문체로 쓴다.
            // 색은 고정 여부와 무관하게 본문색이다 — 초록으로 칠하면 안드로이드만 글자색이 달라진다.
            Text(
                notice.content.orEmpty(),
                Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    listOfNotNull(
                        notice.authorNickname.takeIf(String::isNotBlank),
                        moyeoNoticeTime(notice.createdAt).takeIf(String::isNotBlank)
                    ).joinToString(" · "),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // 화면기획 20-3 카드 우측 동작 자리 — 호스트에게만 고정 토글과 수정을 둔다.
                // `수정` 은 20-3a 로 간다. 예전에는 링크만 있고 목적지가 없었다(정본 §6-1).
                if (canPin) {
                    Text(
                        if (notice.pinned) "고정 해제" else "다시 고정",
                        modifier = Modifier
                            .clickable(enabled = !pinBusy, onClick = onTogglePinned)
                            .testTag("server-notice-pin-${notice.noticeId}"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "수정",
                        modifier = Modifier
                            .padding(start = 14.dp)
                            .clickable(onClick = onEdit)
                            .testTag("server-notice-edit-${notice.noticeId}"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
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
    // 화면기획 17-x 공용 진행 단계 — 완료: 초록 테두리+초록 체크, 현재: 틴트 원+초록 테두리+초록 아이콘, 미래: 회색
    val labels = listOf("코스", "일정", "인원", "세부", "리뷰")
    val colors = MaterialTheme.colorScheme
    val tints = MoyeoTheme.tints
    val isDark = MoyeoTheme.isDark
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        labels.forEachIndexed { index, label ->
            val done = index < activeStep
            val current = index == activeStep
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = when {
                        current -> tints.primaryTint
                        done -> Color.Transparent
                        else -> if (isDark) colors.surfaceVariant else colors.surface
                    },
                    border = BorderStroke(1.dp, if (done || current) colors.primary else colors.outline)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (done) Icons.Filled.Check else stepIcon(index),
                            null,
                            Modifier.size(17.dp),
                            tint = if (done || current) colors.primary else colors.onSurfaceVariant
                        )
                    }
                }
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (done || current) colors.primary else colors.onSurfaceVariant,
                    fontWeight = if (current) FontWeight.ExtraBold else FontWeight.Medium
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
            // 아이콘은 **둥근 사각형 배경**에 담는다 — 기획·웹·iOS 가 그렇고
            // 안드로이드만 맨 아이콘이었다 (사용자 지적, 2026-09-09).
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = if (selected) MaterialTheme.colorScheme.primary else MoyeoTheme.tints.primaryTint
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (source == CourseSource.Linked) Icons.Filled.Map else Icons.Filled.Route,
                        null,
                        tint = if (selected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
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
    sourceLabel: String? = null,
    /** 코스 대표 사진. **서버가 준다** — 예전에는 안 그려서 안드로이드만 사진이 없었다
     *  (사용자 지적, 2026-09-09). 없으면 공용 플레이스홀더를 쓴다. */
    thumbnail: String? = null,
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
            CachedRemoteImage(
                url = thumbnail,
                contentDescription = null,
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
                fallbackShape = MoyeoPlaceholderShape.SQUARE
            ) { Box(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) }
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
private fun InfoBanner(icon: ImageVector?, text: String, warning: Boolean = false, neutral: Boolean = false) {
    InfoBanner(icon = icon, text = AnnotatedString(text), warning = warning, neutral = neutral)
}

/** 화면기획 17-7처럼 본문 일부만 볼드로 강조하거나(icon=null) 아이콘 없이 쓰는 안내 카드. */
@Composable
private fun InfoBanner(icon: ImageVector?, text: AnnotatedString, warning: Boolean = false, neutral: Boolean = false) {
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
            if (icon != null) {
                Icon(icon, null, Modifier.size(18.dp), tint = content)
            }
            Text(
                text,
                Modifier.padding(start = if (icon != null) 10.dp else 0.dp),
                style = MaterialTheme.typography.bodySmall,
                color = content,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun RouteStopRow(
    stop: RouteStop,
    index: Int,
    editable: Boolean,
    locked: Boolean = false,
    onRemove: () -> Unit
) {
    // 화면기획 18-3 — 경로가 잠긴 화면의 순번 원은 중립 회색이다 (다크 #506157 / 라이트 #B5BCB5)
    val orderCircle = when {
        locked -> if (MoyeoTheme.isDark) Color(0xFF506157) else Color(0xFFB5BCB5)
        else -> MaterialTheme.colorScheme.primary
    }
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
            Surface(Modifier.padding(start = 10.dp).size(28.dp), CircleShape, orderCircle) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "${index + 1}",
                        color = Color.White,
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
    // 화면기획 17-2 — 선택된 세그먼트는 틴트 배경 + 초록 테두리 + 초록 텍스트
    val tints = MoyeoTheme.tints
    Surface(
        modifier.clickable(onClick = onClick),
        RoundedCornerShape(8.dp),
        if (selected) tints.primaryTint else Color.Transparent,
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label,
                fontWeight = FontWeight.ExtraBold,
                color = if (selected) tints.onPrimaryTint else MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) tints.onPrimaryTint else MaterialTheme.colorScheme.onSurfaceVariant
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
    // 값이 없으면 **줄 자체를 그리지 않는다** — 아이콘과 라벨만 남으면 정보가 빠진 것처럼 읽힌다
    // (17-6 의 「마감」이 그렇게 비어 있었다, 사용자 지적 2026-09-09).
    if (value.isBlank()) return
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

/** 초안 방문지 → 지도 순번 마커. 좌표가 없는 방문지는 지도에서 빠진다. */
private fun List<RouteStop>.toRoutePoints(): List<CourseRoutePoint> = mapNotNull { stop ->
    val latitude = stop.latitude ?: return@mapNotNull null
    val longitude = stop.longitude ?: return@mapNotNull null
    CourseRoutePoint(id = stop.id, title = stop.name, position = MoyeoLatLng(latitude, longitude))
}

/** 집합 장소를 아직 고르지 않았을 때 지도가 시작하는 지점 — 경상북도 중심 근처다. */
private val GyeongbukMapCenter = MoyeoLatLng(36.4, 128.9)

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

/**
 * 모집 만들기의 기본 썸네일.
 *
 * 2026-08-26 서버 변경으로 채팅방 생성에 `thumbnail` 파트가 필수가 됐다(없으면 400 `40041`).
 * 17 모집 만들기에는 아직 사진 선택 단계가 없어서, 사진이 없을 때 쓰는 공용 플레이스홀더를 올린다.
 * 카드형 목록에 쓰이는 16:9 쪽을 쓴다.
 */
private fun defaultRoomThumbnail(context: Context): MultipartFile {
    val bytes = context.resources.openRawResource(R.drawable.placeholder_landscape).use { it.readBytes() }
    return MultipartFile(
        partName = "thumbnail",
        fileName = "placeholder-landscape.webp",
        mimeType = "image/webp",
        bytes = bytes
    )
}
