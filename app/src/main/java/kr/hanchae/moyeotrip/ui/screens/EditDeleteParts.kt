package kr.hanchae.moyeotrip.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.data.courses.TravelCourse
import kr.hanchae.moyeotrip.data.feed.FeedComment
import kr.hanchae.moyeotrip.data.rooms.ChatRoomDetail
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyState
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyText
import kr.hanchae.moyeotrip.ui.components.MoyeoPlaceholderShape
import kr.hanchae.moyeotrip.ui.components.moyeoRelativeTime
import kr.hanchae.moyeotrip.ui.components.moyeoTripDateText

/*
 * `EditDeleteScreens.kt` 가 쓰는 부품과 18-7 코스 이름 수정.
 *
 * 파일을 나눈 이유는 길이뿐이다 — 서버 계약과 문구 근거는 그쪽 헤더에 있다.
 */

// ───────── 23-1a 댓글 한 줄 ─────────

/**
 * 댓글 한 줄. 답글은 자기 자신을 한 단 들여써 그린다.
 *
 * 내 댓글에만 `수정 · 삭제` 진입점을 낸다 — 남의 댓글은 서버가 403 으로 막는다.
 */
@Composable
internal fun CommentEditRow(
    comment: FeedComment,
    depth: Int,
    myId: Long?,
    state: CommentEditRowState,
    actions: CommentEditRowActions
) {
    val colors = MaterialTheme.colorScheme
    val isMine = myId != null && comment.author.userId == myId
    val isEditing = state.editingId == comment.commentId

    Column(Modifier.fillMaxWidth().padding(start = (depth * 24).dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            // 아바타는 기획·웹·iOS 에 다 있다. 안드로이드에만 없어 4열 비교에서 눈에 걸렸다.
            // 23-1 피드 상세 댓글과 **같은 부품·같은 크기**를 쓴다(새 관례를 만들지 않는다).
            UserAvatar(
                imageUrl = comment.author.profileImageUrl,
                nickname = comment.author.nickname,
                modifier = Modifier.size(if (depth > 0) 30.dp else 34.dp),
                fallbackFontSize = 14.sp
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        comment.author.nickname,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        moyeoRelativeTime(comment.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                    if (isEditing) {
                        Text(
                            "수정 중",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }
                }

                if (isEditing) {
                    CommentEditEditor(comment, depth, state, actions)
                } else {
                    Text(comment.content, style = MaterialTheme.typography.bodyMedium)
                    // 「수정 · 삭제」를 회색 한 덩어리로 두면 **버튼으로 안 보인다** —
                    // 사용자가 「댓글 수정 버튼이 안 보인다」고 했다 (23-1a, 2026-09-09).
                    // 두 낱말로 갈라 각자의 색(수정=브랜드 초록 · 삭제=위험 빨강)을 준다.
                    if (isMine) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                onClick = actions.onOpen,
                                modifier = Modifier.testTag("comment-edit-open")
                            ) {
                                Text(
                                    "수정",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.primary
                                )
                            }
                            TextButton(
                                onClick = actions.onOpen,
                                modifier = Modifier.testTag("comment-delete-open")
                            ) {
                                Text(
                                    "삭제",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.error
                                )
                            }
                        }
                    }
                }
            }
        }

        comment.replies.forEach { reply ->
            CommentEditRow(
                comment = reply,
                depth = depth + 1,
                myId = myId,
                state = state,
                actions = actions.forComment(reply)
            )
        }
    }
}

/** 편집 중인 댓글의 입력창과 버튼들. */
@Composable
private fun CommentEditEditor(
    comment: FeedComment,
    depth: Int,
    state: CommentEditRowState,
    actions: CommentEditRowActions
) {
    val colors = MaterialTheme.colorScheme
    OutlinedTextField(
        value = state.draft,
        onValueChange = { actions.onDraft(it.take(state.limit)) },
        modifier = Modifier.fillMaxWidth().testTag("comment-edit-input"),
        enabled = !state.busy,
        supportingText = { Text("${state.draft.length}/${state.limit}") }
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { actions.onSave(comment.commentId) },
            enabled = !state.busy && state.draft.isNotBlank(),
            modifier = Modifier.height(36.dp).testTag("comment-edit-save"),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("수정 저장", style = MaterialTheme.typography.labelMedium)
        }
        OutlinedButton(
            onClick = actions.onCancel,
            modifier = Modifier.height(36.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("취소", style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.weight(1f))
        TextButton(
            onClick = { actions.onDelete(comment.commentId) },
            enabled = !state.busy,
            modifier = Modifier.testTag("comment-edit-delete")
        ) {
            Text(
                "삭제",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = colors.error
            )
        }
    }
    // 답글이 딸린 최상위 댓글은 **그 답글도 함께 사라진다**는 것을 먼저 알린다 (BE 회신 §3).
    val replies = comment.replies.size
    val warning = if (depth == 0 && replies > 0) {
        "삭제하면 바로 사라져요. 이 댓글에 달린 답글 ${replies}개도 함께 없어져요. 되돌릴 수 없어요."
    } else {
        "삭제하면 바로 사라져요. 되돌릴 수 없어요."
    }
    Text(
        warning,
        style = MaterialTheme.typography.labelSmall,
        color = colors.onSurfaceVariant
    )
}

/** 편집 상태 묶음 — 인자가 늘어 구조체로 묶었다. */
internal data class CommentEditRowState(val editingId: Long?, val draft: String, val busy: Boolean, val limit: Int)

/** 편집 동작 묶음. [forComment] 로 답글용 진입 동작을 갈아 끼운다. */
internal data class CommentEditRowActions(
    val onOpen: () -> Unit,
    val onDraft: (String) -> Unit,
    val onCancel: () -> Unit,
    val onSave: (Long) -> Unit,
    val onDelete: (Long) -> Unit,
    val onNestedOpen: (Long, String) -> Unit
) {
    fun forComment(comment: FeedComment): CommentEditRowActions =
        copy(onOpen = { onNestedOpen(comment.commentId, comment.content) })
}

// ───────── 18-6 본문 ─────────

/** 서버가 받는 항목만 둔다 (BE 회신 §4). 검증하는 값은 읽기 전용으로 이유와 함께 보여준다. */
@Composable
internal fun RecruitEditBody(
    room: ChatRoomDetail,
    form: RecruitEditForm,
    actions: RecruitEditFormActions,
    busy: Boolean,
    thumbnail: RecruitEditThumbnail
) {
    val colors = MaterialTheme.colorScheme
    // 문구는 네 표면이 **한 문단으로 같게** 쓴다 (기획 기준). 안드로이드에만 빠져 있었다.
    EditDeleteNoteBox(
        "모집 중이라 아직 고칠 수 있어요. 여행을 확정하면 더는 고칠 수 없어요 — " +
            "이미 신청한 분의 약속이 달라지니까요."
    )
    GapFieldLabel("모집 제목", required = true)
    OutlinedTextField(
        value = form.title,
        onValueChange = { actions.onTitle(it.take(24)) },
        modifier = Modifier.fillMaxWidth().testTag("recruit-edit-title"),
        enabled = !busy,
        supportingText = { Text("${form.title.length}/24") }
    )
    GapFieldLabel("소개")
    OutlinedTextField(
        value = form.description,
        onValueChange = { actions.onDescription(it.take(1000)) },
        modifier = Modifier.fillMaxWidth().height(120.dp).testTag("recruit-edit-description"),
        enabled = !busy,
        supportingText = { Text("${form.description.length}/1,000") }
    )

    // 대표 사진 — 서버는 선택 `thumbnail` 파일 파트를 받는다 (BE 회신 §4).
    // 기획·웹·iOS 와 같은 배치다: 92×68 미리보기 왼쪽, 「사진 바꾸기」 오른쪽.
    GapFieldLabel("대표 사진")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val shape = RoundedCornerShape(10.dp)
        val picked = thumbnail.picked
        if (picked != null) {
            Image(
                bitmap = remember(picked) {
                    BitmapFactory.decodeByteArray(picked.bytes, 0, picked.bytes.size).asImageBitmap()
                },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(width = 92.dp, height = 68.dp).clip(shape)
            )
        } else {
            CachedRemoteImage(
                url = room.thumbnail,
                contentDescription = null,
                modifier = Modifier.size(width = 92.dp, height = 68.dp).clip(shape),
                contentScale = ContentScale.Crop,
                fallbackShape = MoyeoPlaceholderShape.LANDSCAPE
            ) { Box(Modifier.background(colors.surfaceVariant)) }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(
                onClick = thumbnail.onPick,
                enabled = !busy,
                modifier = Modifier.height(44.dp).testTag("recruit-edit-thumbnail-pick"),
                shape = RoundedCornerShape(12.dp)
            ) { Text("사진 바꾸기", fontWeight = FontWeight.Bold) }
            Text(
                if (picked == null) "바꾸지 않으면 지금 사진이 그대로 남아요" else "저장하면 이 사진으로 바뀌어요",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant
            )
        }
    }

    GapFieldLabel("여행 유형", required = true)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(true to "당일치기", false to "1박 이상").forEach { (value, label) ->
            val selected = form.dayTrip == value
            OutlinedButton(
                onClick = { actions.onDayTrip(value) },
                enabled = !busy,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (selected) colors.primary else colors.onSurface
                )
            ) {
                Text(
                    label,
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold
                )
            }
        }
    }
    Text(
        "당일치기는 시작·종료 시각을, 1박 이상은 종료 날짜를 받아요",
        style = MaterialTheme.typography.labelSmall,
        color = colors.onSurfaceVariant
    )

    val minimum = room.minimumParticipants?.toString() ?: "-"
    EditDeleteReadOnlyRow("여행 날짜", recruitScheduleText(room, form.dayTrip), "모집 마감일보다 뒤여야 해요")
    EditDeleteReadOnlyRow(
        "모집 마감일",
        room.recruitmentDeadlineDate?.let { moyeoTripDateText(it) }.orEmpty(),
        "여행 날짜보다 앞이어야 해요"
    )
    EditDeleteReadOnlyRow(
        "인원",
        "최소 ${minimum}명 · 최대 ${room.maxParticipants}명 (지금 ${room.participantCount}명 참여 중)",
        "최대 인원을 지금 참여 중인 인원보다 적게 줄일 수 없어요"
    )
    EditDeleteReadOnlyRow(
        "참가비",
        "1인 ${"%,d".format(room.participationFee ?: 0)}원",
        "0원이면 “무료”로 보여요"
    )
    EditDeleteReadOnlyRow(
        "연결된 코스",
        room.courseTitle ?: "연결된 코스 없음",
        "코스는 여기서 못 고쳐요. 경로는 18-1, 이름과 소개는 18-7 에서 고쳐요."
    )
    Text(
        "저장하면 채팅방에 “호스트가 모집 정보를 수정했어요.”가 남고 참여자에게 알려요.",
        style = MaterialTheme.typography.labelSmall,
        color = colors.onSurfaceVariant
    )
    // 방 삭제는 없다 — 취소로 갈음한다 (BE 회신 §4). 있는 길만 안내한다.
    Spacer(Modifier.height(12.dp))
    Text("모집을 그만두려면", fontWeight = FontWeight.Bold)
    Text(
        "모집을 취소하면 참여자에게 알림이 가고 채팅방은 읽기 전용으로 남아요. " +
            "방을 아예 지우는 건 없어요.",
        style = MaterialTheme.typography.labelSmall,
        color = colors.onSurfaceVariant
    )
}

/**
 * 날짜는 「2026.10.24 (토)」 꼴이다 — 기획이 그렇게 쓴다.
 *
 * 예전에는 서버 값(`2026-10-24`)을 그대로 찍어 요일이 없고 구분자도 달랐다 (18-6, 2026-09-09).
 */
private fun recruitScheduleText(room: ChatRoomDetail, dayTrip: Boolean): String = if (dayTrip) {
    "${moyeoTripDateText(room.startDate)} · ${room.dayTripStartTime?.take(5).orEmpty()} – " +
        room.dayTripEndTime?.take(5).orEmpty()
} else {
    "${moyeoTripDateText(room.startDate)} – ${room.endDate?.let { moyeoTripDateText(it) }.orEmpty()}"
}

/** 지금 값을 보여주기만 하는 줄. iOS·웹처럼 **테두리 박스**로 그린다 — 맨 텍스트로 두면 4열 비교에서 안드로이드만 달라 보인다. */
@Composable
private fun EditDeleteReadOnlyRow(label: String, value: String, hint: String) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(top = 14.dp)) {
        GapFieldLabel(label)
        Text(
            value,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .border(BorderStroke(1.dp, colors.outline.copy(alpha = 0.55f)), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 13.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            hint,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant
        )
    }
}

internal data class RecruitEditForm(val title: String, val description: String, val dayTrip: Boolean)

internal data class RecruitEditFormActions(
    val onTitle: (String) -> Unit,
    val onDescription: (String) -> Unit,
    val onDayTrip: (Boolean) -> Unit
)

// ───────── 18-7 · 코스 이름 · 소개 수정 ─────────

/**
 * `PATCH /travel-courses/chat-rooms/{roomId}` — 기존 요청에 `title`·`description` 선택 필드가
 * 붙었다 (BE 회신 §5). 이름을 고칠 길이 없어서 `27-3 검증용 커스텀 코스 모임` 같은 이름이
 * 홈에 그대로 걸려 있었다.
 *
 * **호스트가 직접 만든 `CUSTOM` 코스이고, 여행 확정 전일 때만** 고칠 수 있다.
 */
@Composable
fun CourseTitleEditScreen(tripId: String, onBack: () -> Unit, onOpenRoute: () -> Unit) {
    val server = LocalServerData.current
    val scope = rememberCoroutineScope()
    val roomId = tripId.serverRoomIdOrNull()
    var course by remember(roomId) { mutableStateOf<TravelCourse?>(null) }
    var loaded by remember(roomId) { mutableStateOf(false) }
    var title by remember(roomId) { mutableStateOf("") }
    var description by remember(roomId) { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(roomId, server) {
        if (server == null || roomId == null) return@LaunchedEffect
        val detail = runCatching { server.courses.roomCourse(roomId) }.getOrNull()
        val room = runCatching { server.chatRooms.room(roomId) }.getOrNull()
        val myId = server.signedInUserId()
        // 직접 만든 코스 + 내 방 + 확정 전 — 세 조건이 다 맞아야 서버가 받는다.
        course = detail?.takeIf {
            room?.hostId == myId && it.type == "CUSTOM" && room?.status == "RECRUITING"
        }
        course?.let {
            title = it.title
            description = it.description.orEmpty()
        }
        loaded = true
    }

    GapScaffold(
        title = "코스 이름 수정",
        onBack = onBack,
        testTag = "course-title-edit",
        cta = if (busy) "저장 중..." else "수정 저장",
        ctaEnabled = course != null && title.isNotBlank() && !busy,
        error = error,
        onCta = {
            if (server == null || roomId == null) return@GapScaffold
            busy = true
            error = null
            scope.launch {
                runCatching {
                    // 제목은 항상 보내고(빈 값은 서버가 400), 소개는 비었으면 **보내지 않아**
                    // 기존 값을 남긴다 (PATCH 의 부분 수정 규칙).
                    server.courses.updateRoomCourse(
                        roomId = roomId,
                        title = title.trim(),
                        description = description.trim().ifEmpty { null }
                    )
                }
                    .onSuccess { onBack() }
                    .onFailure { error = it.message ?: "코스 이름을 수정하지 못했어요." }
                busy = false
            }
        }
    ) {
        val current = course
        if (current == null) {
            MoyeoEmptyState(
                if (loaded) "직접 만든 코스만, 여행 확정 전에 고칠 수 있어요." else MoyeoEmptyText.LOADING,
                testTag = "course-title-edit-empty"
            )
            return@GapScaffold
        }
        // 안내는 기획·iOS 처럼 **회색 박스**에 담는다 — 맨 텍스트로 두면 안드로이드만 달라 보인다.
        EditDeleteNoteBox(
            "직접 만든 코스라 고칠 수 있어요. 등록된 코스를 그대로 쓰는 모임은 이름을 바꿀 수 없고, " +
                "여행을 확정한 뒤에도 못 바꿔요."
        )
        GapFieldLabel("코스 이름", required = true)
        OutlinedTextField(
            value = title,
            onValueChange = { title = it.take(24) },
            modifier = Modifier.fillMaxWidth().testTag("course-title-edit-title"),
            enabled = !busy,
            supportingText = { Text("${title.length}/24 · 24자까지 · 모임 목록과 홈에 이 이름이 보여요") }
        )
        GapFieldLabel("코스 소개")
        OutlinedTextField(
            value = description,
            onValueChange = { description = it.take(1000) },
            modifier = Modifier.fillMaxWidth().height(120.dp).testTag("course-title-edit-description"),
            enabled = !busy,
            supportingText = { Text("${description.length}/1,000 · 비워 두면 지금 소개가 그대로 남아요") }
        )
        EditDeleteNoteBox(
            "방문지 ${current.places.size}곳과 순서는 여기서 안 바꿔요. 여행 경로 수정에서 고쳐요."
        )
        OutlinedButton(
            onClick = onOpenRoute,
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("course-title-edit-route"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("여행 경로 수정으로 가기", fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * 라우트 식별자(`srv-{feedId}`)에서 서버 피드 id 를 뽑는다.
 *
 * 형태가 아니거나 비었으면 0 이다 — 화면이 그때 빈 상태를 그린다
 * (23 피드 상세가 쓰는 규칙과 같다: `FeedDetailScreen.kt`).
 */
internal fun String.serverFeedIdOrZero(): Long =
    if (startsWith("srv-")) removePrefix("srv-").toLongOrNull() ?: 0L else toLongOrNull() ?: 0L

/** 회색 안내 박스. 기획의 안내 블록·iOS `AttachNoteBox` 와 같은 모양이다. */
@Composable
internal fun EditDeleteNoteBox(text: String) {
    val colors = MaterialTheme.colorScheme
    Text(
        text,
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceVariant, RoundedCornerShape(10.dp))
            .padding(horizontal = 13.dp, vertical = 11.dp),
        style = MaterialTheme.typography.labelSmall,
        color = colors.onSurfaceVariant
    )
}

/** 대표 사진 상태·동작 묶음 — 인자가 늘어 구조체로 묶었다(RecruitEditBody 인자 제한). */
internal data class RecruitEditThumbnail(val picked: PickedImage?, val onPick: () -> Unit)
