package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.hanchae.moyeotrip.data.FeedPost
import kr.hanchae.moyeotrip.data.FeedVisibility
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.data.TripCourse
import kr.hanchae.moyeotrip.data.TripRecruitment
import kr.hanchae.moyeotrip.ui.theme.ForestGreen

@Composable
fun NotificationCenterScreen(
    onBack: () -> Unit,
    onOpenTrip: (String) -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenCourse: (String) -> Unit,
    onOpenTripConfirmed: () -> Unit = {},
    onOpenTripMessage: () -> Unit = {}
) {
    val notifications = listOf(
        NotificationItem(
            "여행이 확정됐어요 🎉",
            "주왕산 & 주산지 힐링 트레킹에 5명이 모였어요",
            "방금",
            "confirmed",
            "trip-cheongsong-juwangsan"
        ),
        NotificationItem(
            "출발 확정까지 1명 남았어요",
            "주왕산 & 주산지 힐링 트레킹",
            "방금",
            "trip",
            "trip-cheongsong-juwangsan"
        ),
        NotificationItem(
            "새 댓글이 달렸어요",
            "경주 단풍·야경 기록에 반응이 왔어요",
            "12분 전",
            "feed",
            "feed-3"
        ),
        NotificationItem(
            "날씨 추천이 바뀌었어요",
            "맑음 예보에 맞춰 경주 첨성대 코스를 추천해요",
            "오늘",
            "course",
            "gyeongju-healing"
        ),
        NotificationItem(
            "하회마을 모임이 확정됐어요",
            "모임 채팅방에서 준비물을 확인해보세요",
            "어제",
            "trip",
            "trip-andong-hahoe"
        ),
        NotificationItem(
            "여행의 한 줄을 남겨주세요",
            "함께 걸은 친구의 도감 카드가 기다리고 있어요",
            "3일 전",
            "message",
            "trip-gyeongju-night"
        )
    )

    SupportScaffold(title = "알림", onBack = onBack) {
        items(notifications) { item ->
            SupportCard(
                modifier = Modifier
                    .clickable {
                        when (item.type) {
                            "feed" -> onOpenPost(item.targetId)
                            "course" -> onOpenCourse(item.targetId)
                            "confirmed" -> onOpenTripConfirmed()
                            "message" -> onOpenTripMessage()
                            else -> onOpenTrip(item.targetId)
                        }
                    }
                    .testTag("notification-${item.type}-${item.targetId}")
            ) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconBubble {
                        Icon(
                            imageVector = Icons.Filled.NotificationsNone,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = item.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = item.time,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateRecruitmentScreen(
    courseId: String,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenManage: (String) -> Unit
) {
    val course = remember(courseId) { MockTripRepository.findCourse(courseId) }
    var createdTripId by remember { mutableStateOf<String?>(null) }
    var scheduleDate by rememberSaveable(courseId) { mutableStateOf(course.startLabel) }
    var scheduleTime by rememberSaveable(courseId) {
        mutableStateOf(if (course.duration == "2박 3일") "09:00 - 2박 3일" else "08:00 - 18:00")
    }
    var meetingPoint by rememberSaveable(courseId) { mutableStateOf(course.meetingPoint) }
    var capacityText by rememberSaveable(courseId) { mutableStateOf(course.capacity.toString()) }
    var recruitmentNote by rememberSaveable(courseId) { mutableStateOf(course.recruitmentNote) }
    val created = createdTripId != null
    val capacity = capacityText.toIntOrNull()?.coerceIn(course.minParticipants, 12) ?: course.capacity

    SupportScaffold(title = "모집 만들기", onBack = onBack) {
        item {
            CourseSummaryCard(course = course)
        }
        item {
            SupportCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "모집 정보",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    OutlinedTextField(
                        value = scheduleDate,
                        onValueChange = { scheduleDate = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create-recruitment-date"),
                        label = { Text("일정") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = scheduleTime,
                        onValueChange = { scheduleTime = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create-recruitment-time"),
                        label = { Text("시간") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = meetingPoint,
                        onValueChange = { meetingPoint = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create-recruitment-place"),
                        label = { Text("모이는 곳") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = capacityText,
                        onValueChange = { value -> capacityText = value.filter { it.isDigit() }.take(2) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create-recruitment-capacity"),
                        label = { Text("모집 정원") },
                        supportingText = { Text("최소 ${course.minParticipants}명, 최대 12명") },
                        singleLine = true
                    )
                    SupportField(label = "참가비", value = course.price)
                }
            }
        }
        item {
            SupportCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "소개글",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    OutlinedTextField(
                        value = recruitmentNote,
                        onValueChange = { recruitmentNote = it.take(160) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create-recruitment-note"),
                        minLines = 3,
                        label = { Text("함께 갈 사람들에게 보여줄 안내") },
                        supportingText = { Text("${recruitmentNote.length}/160자") }
                    )
                    SupportField(
                        label = "미리보기",
                        value = "${scheduleDate.trim()} · $meetingPoint · 1/${capacity}명 모집"
                    )
                }
            }
        }
        item {
            if (created) {
                SupportCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = ForestGreen)
                            Text(
                                text = "모집이 준비됐어요",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Button(
                            onClick = {
                                val tripId = createdTripId ?: MockTripRepository.tripIdForCourse(course.id)
                                onOpenManage(tripId)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create-recruitment-open-manage")
                        ) {
                            Text("모집 관리")
                        }
                        Button(
                            onClick = {
                                val tripId = createdTripId ?: MockTripRepository.tripIdForCourse(course.id)
                                onOpenChat(MockTripRepository.chatThreadIdForTrip(tripId))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("채팅방 미리보기")
                        }
                    }
                }
            } else {
                Button(
                    onClick = {
                        createdTripId = MockTripRepository.createRecruitment(
                            courseId = course.id,
                            scheduleDate = scheduleDate,
                            scheduleTime = scheduleTime,
                            meetingPoint = meetingPoint,
                            capacity = capacity,
                            note = recruitmentNote
                        ).id
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("create-recruitment-submit")
                ) {
                    Icon(imageVector = Icons.Filled.GroupAdd, contentDescription = null)
                    Text(
                        text = "모집 만들기",
                        modifier = Modifier.padding(start = 8.dp),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun HostManageScreen(
    tripId: String,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenRoute: (String) -> Unit = {}
) {
    val trip = MockTripRepository.findTrip(tripId)
    var pendingApplicants by remember {
        mutableStateOf(
            listOf(
                HostApplicant("applicant-deer", "따스한 사슴 3492", "🦌", "사진 찍는 속도에 맞춰 천천히 걷고 싶어요."),
                HostApplicant("applicant-turtle", "잔잔한 거북이 9032", "🐢", "초행이라 모이는 장소와 준비물을 미리 확인하고 싶어요.")
            )
        )
    }
    var approvedApplicants by remember {
        mutableStateOf(listOf(HostApplicant("approved-bear", "우직한 곰 7821", "🐻", "기존 참여자")))
    }
    var rejectedApplicants by remember { mutableStateOf(emptyList<HostApplicant>()) }
    var isRecruitmentClosed by remember(tripId) { mutableStateOf(trip.statusLabel == "모집취소") }

    SupportScaffold(title = "모집 관리", onBack = onBack) {
        item {
            HostManageSummaryCard(
                trip = trip,
                approvedCount = approvedApplicants.size + 1,
                pendingCount = pendingApplicants.size,
                isRecruitmentClosed = isRecruitmentClosed
            )
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { onOpenRoute(trip.id) }.testTag("host-manage-route"),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Route, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.padding(start = 12.dp).weight(1f)) {
                        Text(
                            "여행 경로 · 방문지 ${trip.routeStops.size.takeIf {
                                it > 0
                            } ?: MockTripRepository.findCourseForTrip(trip).stops.size}곳",
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            if (trip.courseSource ==
                                kr.hanchae.moyeotrip.data.CourseSource.Custom
                            ) {
                                "여행 확정 전까지 수정 가능 · 저장 시 멤버 알림"
                            } else {
                                "등록된 코스 · 방문지와 순서 수정 불가"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        item {
            HostManageSectionTitle(title = "승인 대기", count = pendingApplicants.size)
        }
        if (pendingApplicants.isEmpty()) {
            item {
                SupportCard {
                    Text(
                        text = "새 신청이 오면 이곳에서 승인하거나 거절할 수 있어요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(pendingApplicants, key = { it.id }) { applicant ->
                HostApplicantCard(
                    applicant = applicant,
                    primaryLabel = "승인",
                    secondaryLabel = "거절",
                    onPrimary = {
                        pendingApplicants = pendingApplicants.filterNot { it.id == applicant.id }
                        approvedApplicants = approvedApplicants + applicant
                        MockTripRepository.approveHostApplicant(
                            tripId = trip.id,
                            applicantName = applicant.name
                        )
                    },
                    onSecondary = {
                        pendingApplicants = pendingApplicants.filterNot { it.id == applicant.id }
                        rejectedApplicants = rejectedApplicants + applicant
                        MockTripRepository.rejectHostApplicant(
                            tripId = trip.id,
                            applicantName = applicant.name
                        )
                    }
                )
            }
        }
        item {
            HostManageSectionTitle(title = "승인된 동행자", count = approvedApplicants.size)
        }
        items(approvedApplicants, key = { it.id }) { applicant ->
            SupportCard {
                HostApplicantHeader(applicant = applicant)
                Text(
                    text = "집결지와 쉬는 시간을 채팅방에서 함께 확인해요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (rejectedApplicants.isNotEmpty()) {
            item {
                HostManageSectionTitle(title = "거절 기록", count = rejectedApplicants.size)
            }
            items(rejectedApplicants, key = { it.id }) { applicant ->
                SupportCard {
                    HostApplicantHeader(applicant = applicant)
                    Text(
                        text = "거절 사유: 일정과 동선 조건이 맞지 않아요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        item {
            SupportCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (isRecruitmentClosed) "모집 취소됨" else "모집 진행중",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isRecruitmentClosed) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.testTag("host-manage-close-state")
                    )
                    Button(
                        onClick = { onOpenChat(MockTripRepository.chatThreadIdForTrip(trip.id)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("host-manage-open-chat")
                    ) {
                        Text("모임 채팅으로 이동")
                    }
                    Button(
                        onClick = {
                            isRecruitmentClosed = !isRecruitmentClosed
                            MockTripRepository.setRecruitmentClosed(trip.id, isRecruitmentClosed)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("host-manage-toggle-close"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text(if (isRecruitmentClosed) "모집 다시 열기" else "모집 취소")
                    }
                }
            }
        }
    }
}

@Composable
private fun HostManageSummaryCard(
    trip: TripRecruitment,
    approvedCount: Int,
    pendingCount: Int,
    isRecruitmentClosed: Boolean
) {
    SupportCard {
        Text(
            text = trip.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold
        )
        SupportField(label = "일정", value = "${trip.scheduleDate} ${trip.scheduleTime}")
        SupportField(label = "모이는 곳", value = trip.meetingPoint)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SupportChip(text = "$approvedCount/${trip.capacity}명")
            SupportChip(text = "대기 $pendingCount")
            SupportChip(text = if (isRecruitmentClosed) "모집 취소됨" else trip.statusLabel)
        }
        Text(
            text = if (isRecruitmentClosed) {
                "모집이 취소되어 새 신청을 받지 않아요. 채팅방에서는 기존 안내를 확인할 수 있어요."
            } else {
                "최소 ${trip.minParticipants}명 이상이면 출발 확정 상태로 전환돼요."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HostManageSectionTitle(title: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "${count}명",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun HostApplicantCard(
    applicant: HostApplicant,
    primaryLabel: String,
    secondaryLabel: String,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit
) {
    SupportCard(modifier = Modifier.testTag("host-applicant-${applicant.id}")) {
        HostApplicantHeader(applicant = applicant)
        Text(
            text = applicant.note,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onSecondary,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("host-applicant-${applicant.id}-reject"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(secondaryLabel)
            }
            Button(
                onClick = onPrimary,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("host-applicant-${applicant.id}-approve")
            ) {
                Text(primaryLabel)
            }
        }
    }
}

@Composable
private fun HostApplicantHeader(applicant: HostApplicant) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(text = applicant.avatar, fontSize = 24.sp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = applicant.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "매너 4.8 · 최근 동행 2회",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class HostApplicant(val id: String, val name: String, val avatar: String, val note: String)

@Composable
fun FeedWriteScreen(onBack: () -> Unit, onPostCreated: (String) -> Unit) {
    var currentStep by rememberSaveable { mutableStateOf(1) }
    var title by rememberSaveable { mutableStateOf("첫 반패키지 단풍 여행") }
    var story by rememberSaveable {
        mutableStateOf("처음 반패키지 여행이었는데 동행분들이 너무 좋으셨어요.\n첨성대 야경이 진짜 인생샷...")
    }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var createdPostId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedVisibility by rememberSaveable { mutableStateOf(FeedVisibility.Friends) }
    var selectedCourseId by rememberSaveable { mutableStateOf("cheongsong-juwangsan") }
    val colorScheme = MaterialTheme.colorScheme
    val course = remember(selectedCourseId) { MockTripRepository.findCourse(selectedCourseId) }
    val post = MockTripRepository.findFeedPost("feed-3")

    fun submitPost() {
        val existingPostId = createdPostId
        if (existingPostId != null) {
            onPostCreated(existingPostId)
            return
        }

        val createdPost = MockTripRepository.createFeedPost(
            courseId = course.id,
            title = title,
            story = story,
            visibility = selectedVisibility
        )
        createdPostId = createdPost.id
        submitted = true
        onPostCreated(createdPost.id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .testTag("feed-write-screen")
    ) {
        FeedWriteHeader(
            currentStep = currentStep,
            submitted = submitted,
            onBack = onBack,
            onSubmit = { submitPost() }
        )
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .testTag("feed-write-scroll"),
            contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                FeedWriteStepIntro(currentStep = currentStep, submitted = submitted)
            }
            when (currentStep) {
                1 -> {
                    item {
                        FeedWriteCourseSelector(
                            courses = MockTripRepository.courses,
                            selectedCourseId = selectedCourseId,
                            onCourseSelected = { selectedCourseId = it }
                        )
                    }
                    item { FeedWriteRouteCard(course = course) }
                    item { FeedWriteMemberMeta(course = course, visibility = selectedVisibility) }
                }

                2 -> {
                    item { FeedWritePhotoGrid(post = post) }
                }

                3 -> {
                    item {
                        FeedWriteMemoCard(
                            title = title,
                            story = story,
                            onTitleChange = { title = it },
                            onStoryChange = { story = it }
                        )
                    }
                }

                4 -> {
                    item {
                        FeedWriteVisibilityCard(
                            selectedVisibility = selectedVisibility,
                            onVisibilitySelected = { selectedVisibility = it }
                        )
                    }
                    item { FeedWriteMemberMeta(course = course, visibility = selectedVisibility) }
                }

                else -> {
                    item {
                        FeedWritePreviewCard(
                            course = course,
                            title = title,
                            story = story,
                            visibility = selectedVisibility,
                            submitted = submitted
                        )
                    }
                }
            }
        }
        FeedWriteBottomActions(
            currentStep = currentStep,
            submitted = submitted,
            onPrevious = {
                if (currentStep > 1) {
                    currentStep -= 1
                } else {
                    onBack()
                }
            },
            onNext = {
                if (currentStep < 5) {
                    currentStep += 1
                } else {
                    submitPost()
                }
            }
        )
    }
}

@Composable
private fun FeedWriteHeader(currentStep: Int, submitted: Boolean, onBack: () -> Unit, onSubmit: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val canSubmit = currentStep >= 5 && !submitted

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 12.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "닫기",
                    tint = colorScheme.onBackground
                )
            }
            Text(
                text = "피드 글쓰기",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleSmall,
                color = colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = if (submitted) "완료" else "게시",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clip(RoundedCornerShape(8.dp))
                    .testTag("feed-write-submit")
                    .clickable(enabled = canSubmit, onClick = onSubmit)
                    .padding(horizontal = 9.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = if (canSubmit) colorScheme.primary else colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.ExtraBold
            )
        }
        FeedWriteProgress(currentStep = currentStep)
        HorizontalDivider(
            modifier = Modifier.padding(top = 14.dp),
            color = colorScheme.outline.copy(alpha = 0.45f)
        )
    }
}

@Composable
private fun FeedWriteProgress(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(5) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (index < currentStep) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.48f)
                        }
                    )
            )
        }
    }
}

@Composable
private fun FeedWriteStepIntro(currentStep: Int, submitted: Boolean) {
    val colorScheme = MaterialTheme.colorScheme
    val stepLabel = when {
        submitted -> "STEP 5 · 게시 완료"
        currentStep == 1 -> "STEP 1 · 코스 확인"
        currentStep == 2 -> "STEP 2 · 사진 선택"
        currentStep == 3 -> "STEP 3 · 사진 & 메모"
        currentStep == 4 -> "STEP 4 · 공개 설정"
        else -> "STEP 5 · 최종 확인"
    }
    val title = when {
        submitted -> "기록이 저장됐어요"
        currentStep == 1 -> "어떤 여행을 기록할까요?"
        currentStep == 2 -> "대표 사진을 골라요"
        currentStep == 3 -> "여행 어땠어요?"
        currentStep == 4 -> "누구에게 보여줄까요?"
        else -> "게시 전 마지막 확인이에요"
    }
    val helper = when {
        submitted -> "피드에서 방금 만든 여행 기록을 확인할 수 있어요."
        currentStep == 1 -> "방문 코스와 함께한 멤버를 먼저 확인해요."
        currentStep == 2 -> "사진과 지도 조합이 피드 첫 화면에 함께 보여요."
        currentStep == 3 -> "대부분 자동으로 채워졌어요. 한 줄만 남겨주세요."
        currentStep == 4 -> "친구에게만 공개하고 경로와 멤버 정보를 함께 보여줘요."
        else -> "사진, 경로, 멤버가 한 장의 피드처럼 구성됐어요."
    }

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = stepLabel,
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Black
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = helper,
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FeedWriteMemoCard(
    title: String,
    story: String,
    onTitleChange: (String) -> Unit,
    onStoryChange: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BasicTextField(
                value = title,
                onValueChange = { value -> onTitleChange(value.take(40)) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleSmall.copy(
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                ),
                singleLine = true,
                cursorBrush = SolidColor(colorScheme.primary)
            )
            BasicTextField(
                value = story,
                onValueChange = { value -> onStoryChange(value.take(500)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = colorScheme.onSurface,
                    lineHeight = 24.sp
                ),
                cursorBrush = SolidColor(colorScheme.primary)
            )
            Text(
                text = "${story.length} / 500",
                modifier = Modifier.align(Alignment.End),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeedWritePhotoGrid(post: FeedPost) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FeedWritePhotoTile(
                post = post,
                representative = true,
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
            )
            FeedWritePhotoTile(
                post = post,
                muted = true,
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FeedWritePhotoTile(
                post = post,
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
            )
            FeedWriteAddPhotoTile(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
            )
        }
    }
}

@Composable
private fun FeedWriteCourseSelector(
    courses: List<TripCourse>,
    selectedCourseId: String,
    onCourseSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "기록할 코스",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = 4.dp)
        ) {
            items(courses.take(6), key = { it.id }) { course ->
                val selected = course.id == selectedCourseId
                Card(
                    modifier = Modifier
                        .width(188.dp)
                        .clickable { onCourseSelected(course.id) }
                        .testTag("feed-write-course-${course.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (selected) ForestGreen else MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CourseSummaryCard(course = course, compact = true)
                        Text(
                            text = if (selected) "선택됨" else "이 코스로 기록",
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (selected) {
                                        ForestGreen
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedWritePhotoTile(
    post: FeedPost,
    modifier: Modifier = Modifier,
    representative: Boolean = false,
    muted: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colorScheme.surfaceVariant)
    ) {
        FeedPhotoPanel(post = post, modifier = Modifier.fillMaxSize(), cornerRadius = 10.dp)
        if (muted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorScheme.background.copy(alpha = 0.46f))
            )
        }
        if (representative) {
            Surface(
                modifier = Modifier
                    .padding(6.dp)
                    .align(Alignment.TopStart),
                shape = RoundedCornerShape(50),
                color = colorScheme.primary
            ) {
                Text(
                    text = "대표",
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun FeedWriteAddPhotoTile(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.62f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "사진 추가",
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeedWriteRouteCard(course: TripCourse) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.20f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "경로 (자동)",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            FeedRouteMapPanel(
                stops = course.stops,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp),
                cornerRadius = 14.dp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                course.stops.take(3).forEach { tag ->
                    FeedWriteTinyPill(text = tag)
                }
            }
        }
    }
}

@Composable
private fun FeedWriteVisibilityCard(
    selectedVisibility: FeedVisibility,
    onVisibilitySelected: (FeedVisibility) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.20f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "공개 범위",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FeedVisibility.entries.forEach { visibility ->
                    FeedVisibilityOption(
                        visibility = visibility,
                        selected = visibility == selectedVisibility,
                        onClick = { onVisibilitySelected(visibility) }
                    )
                }
            }
            Text(
                text = selectedVisibility.helperText(),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeedVisibilityOption(visibility: FeedVisibility, selected: Boolean, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val background = if (selected) colorScheme.primary else colorScheme.primaryContainer
    val content = if (selected) colorScheme.onPrimary else colorScheme.primary

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .testTag("feed-write-visibility-${visibility.name.lowercase()}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = background,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = if (selected) 0.0f else 0.35f))
    ) {
        Text(
            text = visibility.label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = content,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun FeedWriteMemberMeta(course: TripCourse, visibility: FeedVisibility) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.20f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FeedWriteMetaRow(label = "코스", value = course.title)
            FeedWriteMetaRow(label = "멤버", value = "따스한 사슴, 달빛 토끼, 나")
            FeedWriteMetaRow(label = "공개", value = "${visibility.label} · 경로지도 포함")
        }
    }
}

@Composable
private fun FeedWriteMetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FeedWritePreviewCard(
    course: TripCourse,
    title: String,
    story: String,
    visibility: FeedVisibility,
    submitted: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.28f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            CourseSummaryCard(course = course, compact = true)
            Text(
                text = title.ifBlank { course.title },
                style = MaterialTheme.typography.titleSmall,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = story,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf(visibility.label, "경로지도", course.region) + course.tags.take(1)) { tag ->
                    FeedWriteTinyPill(text = tag)
                }
            }
            if (submitted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "피드에 기록이 올라갔어요",
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedWriteTinyPill(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

private fun FeedVisibility.helperText(): String = when (this) {
    FeedVisibility.Public -> "발견 탭에서도 보이고, 경북 여행자 누구나 볼 수 있어요."
    FeedVisibility.Friends -> "팔로잉 탭과 친구 도감 친구들에게 보여줘요."
    FeedVisibility.Private -> "나만 볼 수 있는 기록으로 저장돼요."
}

@Composable
private fun FeedWriteBottomActions(currentStep: Int, submitted: Boolean, onPrevious: () -> Unit, onNext: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val nextLabel = when {
        submitted -> "완료"
        currentStep < 5 -> "다음 (${currentStep + 1}/5)"
        else -> "게시하기"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colorScheme.background,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "이전",
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onPrevious)
                    .padding(horizontal = 6.dp, vertical = 12.dp),
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold
            )
            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .testTag("feed-write-next")
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(
                    text = nextLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun SearchScreen(onBack: () -> Unit, onOpenCourse: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    val trimmedQuery = query.trim()
    val courses = remember(trimmedQuery) {
        MockTripRepository.courses.filter { course ->
            trimmedQuery.isBlank() ||
                course.title.contains(trimmedQuery, ignoreCase = true) ||
                course.region.contains(trimmedQuery, ignoreCase = true) ||
                course.tags.any { it.contains(trimmedQuery, ignoreCase = true) }
        }
    }

    SupportScaffold(title = "검색", onBack = onBack) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search-query-field"),
                leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
                label = { Text("지역, 테마, 코스 검색") },
                singleLine = true
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("청송", "안동", "경주", "포항", "문경", "영주")) { keyword ->
                    SupportChip(text = keyword, onClick = { query = keyword })
                }
            }
        }
        if (courses.isEmpty()) {
            item {
                SupportCard(modifier = Modifier.testTag("search-empty-state")) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "검색 결과가 없어요",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "지역 이름이나 자연, 야경, 고택 같은 테마로 다시 찾아보세요.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf("경주", "청송", "야경")) { keyword ->
                                SupportChip(
                                    text = keyword,
                                    modifier = Modifier.testTag("search-recovery-$keyword"),
                                    onClick = { query = keyword }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            items(courses, key = { it.id }) { course ->
                SupportCard(modifier = Modifier.clickable { onOpenCourse(course.id) }) {
                    CourseSummaryCard(course = course, compact = true)
                }
            }
        }
    }
}

@Composable
private fun SupportScaffold(
    title: String,
    onBack: () -> Unit,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.size(40.dp))
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .testTag("support-list"),
            contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
private fun CourseSummaryCard(course: TripCourse, compact: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 64.dp else 82.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(text = course.imageEmoji, fontSize = if (compact) 30.sp else 42.sp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = course.title,
                style = if (compact) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${course.region} · ${course.duration} · ${course.rating}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!compact) {
                Text(
                    text = course.oneLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SupportCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun SupportField(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(imageVector = Icons.Filled.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun IconBubble(content: @Composable () -> Unit) {
    Surface(modifier = Modifier.size(42.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@Composable
private fun SupportChip(text: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Surface(
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.32f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = ForestGreen,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

private data class NotificationItem(
    val title: String,
    val body: String,
    val time: String,
    val type: String,
    val targetId: String
)
