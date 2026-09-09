package kr.hanchae.moyeotrip.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.data.api.MultipartFile
import kr.hanchae.moyeotrip.data.feed.FeedComment
import kr.hanchae.moyeotrip.data.feed.ServerFeed
import kr.hanchae.moyeotrip.data.rooms.ChatRoomDetail
import kr.hanchae.moyeotrip.data.rooms.RoomContentUpdate
import kr.hanchae.moyeotrip.data.rooms.RoomMessage
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyState
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyText
import kr.hanchae.moyeotrip.ui.components.OverlayBackdrop

/*
 * 내가 만든 것을 되돌리는 화면들 — 수정 · 삭제.
 *
 * 기획 정본: `모여트립 in 경북/screens-edit-delete.jsx`
 * 서버 계약: `docs/api/BACKEND_REQUEST_CHANGES_2026-09-04.md` (2026-09-04 BE 회신)
 * 2026-09-07 실서버로 6종 전부 존재·권한·검증을 확인했다.
 *
 *   23-2  내 피드 액션 시트          PUT/DELETE /feeds/{id}
 *   23-3  피드 삭제 확인             DELETE /feeds/{id} → 204
 *   24-6  피드 수정 (**본문만**)     PUT /feeds/{id} { content }
 *   23-1a 내 댓글 수정 · 삭제        PUT/DELETE /feeds/{id}/comments/{cid}
 *   18-6  모집 내용 수정 (호스트)    PATCH /chat-rooms/{id} (multipart)
 *   18-7  코스 이름 · 소개 수정      PATCH /travel-courses/chat-rooms/{id}
 *   20-6  내 채팅 메시지 삭제        DELETE /chat-rooms/{id}/messages/{mid} → 204
 *
 * **프로필 이미지 삭제 화면은 없다** — BE 회신 §7 대로 회원가입에서 한 번 고르면 바꿀 수
 * 없는 것이 기획이고 API 도 없다. 화면을 만들면 죽은 버튼이 된다.
 *
 * 껍데기는 `GapFlowScreens.kt` 의 `GapScaffold`·`GapConfirmSheet` 를 그대로 쓴다 —
 * 화면마다 다른 껍데기를 만들면 같은 종류로 읽히지 않는다.
 */

private const val NO_FEED_HINT = "내가 쓴 기록만 수정하거나 지울 수 있어요."
private const val NO_ROOM_EDIT_HINT = "모집 중인 내 모임만 수정할 수 있어요."
private const val NO_MESSAGE_HINT = "내가 보낸 메시지만 지울 수 있어요."

// ───────── 23-2 · 내 피드 액션 시트 (수정 · 삭제) ─────────

/**
 * 남의 피드에서는 이 자리에 32 신고 시트가 열린다(정본 `REPORT-CANON.md`).
 * 내 피드에는 신고가 없다 — 자기 글은 신고할 수 없다(서버가 400 `40039` 로 막는다).
 */
@Composable
fun FeedActionsScreen(feedId: Long, onBack: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val feed = rememberMyFeed(feedId)
    val colors = MaterialTheme.colorScheme
    if (feed == null) {
        GapScaffold(title = "내 여행 기록", onBack = onBack, testTag = "feed-actions") {
            MoyeoEmptyState(NO_FEED_HINT, testTag = "feed-actions-empty")
        }
        return
    }

    // **바텀 시트다.** 기획·웹·iOS 가 그렇고, 뒤에는 피드 상세가 깔린다.
    // 예전에는 `GapScaffold` 로 전체 화면을 그려 안드로이드만 헤더가 달린 판이었다
    // (2026-09-08 4열 대조에서 잡았다). 30-2 신고 시트와 같은 방식이다.
    OverlayBackdrop(
        modifier = Modifier.testTag("feed-actions"),
        scrimAlpha = .48f,
        onScrimClick = onBack,
        background = { FeedDetailScreen(postId = "srv-$feedId", onBack = {}) }
    ) {
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = colors.surface,
            shadowElevation = 12.dp
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .background(colors.outlineVariant, RoundedCornerShape(999.dp))
                )
                Text("내가 쓴 여행 기록", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                Text(
                    "“${feed.content}” · 사진 ${feed.imageUrls.size}장",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(14.dp))
                EditDeleteActionRow(
                    label = "수정하기",
                    desc = "글을 다시 쓸 수 있어요. 사진은 바꿀 수 없어요.",
                    danger = false,
                    testTag = "feed-actions-edit",
                    onClick = onEdit
                )
                // 삭제는 한 단계 더 물어본다 — 시트에서 바로 지우면 되돌릴 방법이 없다.
                EditDeleteActionRow(
                    label = "삭제하기",
                    desc = "되돌릴 수 없어요. 한 번 더 확인해요.",
                    danger = true,
                    testTag = "feed-actions-delete",
                    onClick = onDelete
                )
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("feed-actions-close"),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("닫기", fontWeight = FontWeight.Bold) }
                Spacer(Modifier.height(22.dp))
            }
        }
    }
}

@Composable
private fun EditDeleteActionRow(label: String, desc: String, danger: Boolean, testTag: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    OutlinedButton(
        onClick = onClick,
        // 높이를 직접 준다 — Material3 기본은 40dp 라 기획·웹·iOS 의 보조 버튼(46dp)보다 낮다.
        modifier = Modifier.fillMaxWidth().height(46.dp).testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (danger) colors.error else colors.onSurface
        )
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Text(label, fontWeight = FontWeight.Bold)
            Text(
                desc,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant
            )
        }
    }
    Spacer(Modifier.height(8.dp))
}

// ───────── 23-3 · 피드 삭제 확인 ─────────

@Composable
fun FeedDeleteScreen(feedId: Long, onBack: () -> Unit, onDeleted: () -> Unit) {
    val server = LocalServerData.current
    val scope = rememberCoroutineScope()
    val feed = rememberMyFeed(feedId)
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    GapScaffold(title = "여행 기록 삭제", onBack = onBack, testTag = "feed-delete", error = error) {
        if (feed == null) {
            MoyeoEmptyState(NO_FEED_HINT, testTag = "feed-delete-empty")
        }
    }

    if (feed != null) {
        GapConfirmSheet(
            title = "이 여행 기록을 삭제할까요?",
            description = "“${feed.content}” · 사진 ${feed.imageUrls.size}장",
            // 서버가 함께 정리하는 것을 그대로 적는다 (BE 회신 §2).
            lines = listOf(
                "사진 ${feed.imageUrls.size}장과 글이 함께 사라져요.",
                "달린 댓글 ${feed.commentCount}개와 좋아요도 같이 없어져요.",
                "되돌릴 수 없어요.",
                "내가 쓴 기록만 지울 수 있어요."
            ),
            cancel = "그대로 둘게요",
            confirm = "삭제하기",
            danger = true,
            busy = busy,
            testTag = "feed-delete-confirm",
            onDismiss = onBack,
            onConfirm = {
                if (server == null) return@GapConfirmSheet
                busy = true
                error = null
                scope.launch {
                    runCatching { server.feeds.deleteFeed(feedId) }
                        .onSuccess { onDeleted() }
                        .onFailure { error = it.message ?: "기록을 지우지 못했어요." }
                    busy = false
                }
            }
        )
    }
}

// ───────── 24-6 · 피드 수정 ─────────

/**
 * **본문만 고친다.** 서버가 받는 것이 `{ content }` 하나다 (BE 회신 §2).
 *
 * 사진은 이 API 로 교체하지 않고 기존 첨부가 유지된다. 공개 범위도 못 바꾼다 —
 * 댓글 쓴 사람이 갑자기 피드를 못 보게 되는 것을 막으려는 정책이다.
 * 그래서 사진·공개범위 편집 UI 를 두지 않는다. 두면 **죽은 버튼**이 된다.
 */
@Composable
fun FeedEditScreen(feedId: Long, onBack: () -> Unit, onDelete: () -> Unit) {
    val server = LocalServerData.current
    val scope = rememberCoroutineScope()
    val feed = rememberMyFeed(feedId)
    var content by remember(feed?.feedId) { mutableStateOf(feed?.content.orEmpty()) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val limit = 1000

    LaunchedEffect(feed?.feedId) { feed?.let { content = it.content } }

    val changed = feed != null && content.trim() != feed.content.trim()
    GapScaffold(
        title = "여행 기록 수정",
        onBack = onBack,
        testTag = "feed-edit",
        cta = if (busy) "저장 중..." else "수정 저장",
        ctaEnabled = feed != null && content.isNotBlank() && changed && !busy,
        error = error ?: NO_FEED_HINT.takeIf { feed == null && server != null },
        onCta = {
            if (server == null || feed == null) return@GapScaffold
            busy = true
            error = null
            scope.launch {
                runCatching { server.feeds.updateFeed(feedId, content.trim()) }
                    .onSuccess { onBack() }
                    .onFailure { error = it.message ?: "기록을 수정하지 못했어요." }
                busy = false
            }
        }
    ) {
        if (feed == null) {
            MoyeoEmptyState(NO_FEED_HINT, testTag = "feed-edit-empty")
            return@GapScaffold
        }
        GapFieldLabel("글", required = true)
        OutlinedTextField(
            value = content,
            onValueChange = { content = it.take(limit) },
            modifier = Modifier.fillMaxWidth().height(160.dp).testTag("feed-edit-body"),
            enabled = !busy,
            supportingText = { Text("${content.length}/${"%,d".format(limit)}") }
        )

        // 못 고치는 것은 감추지 않고 이유를 적는다 — 감추면 "왜 없지?" 로 남는다.
        //
        // 머리글도 **박스 안**에 둔다. 기획·웹·iOS 셋 다 박스 첫 줄이 「여기서 못 바꾸는 것」이다 —
        // 밖에 두면 안드로이드만 카드가 하나 더 있는 것처럼 보인다.
        // 줄 사이 여백도 넉넉히 준다: 예전에는 세 줄이 붙어 「위아래가 압축돼」 보였다
        // (24-6, 사용자 지적 2026-09-09).
        Spacer(Modifier.height(6.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(horizontal = 13.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "여기서 못 바꾸는 것",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold
            )
            EditDeleteLockedRow(
                Icons.Outlined.Image,
                "사진 ${feed.imageUrls.size}장",
                "사진은 바꿀 수 없어요. 다시 올리려면 기록을 지우고 새로 써요."
            )
            EditDeleteLockedRow(
                Icons.Outlined.Lock,
                "공개 범위 · ${if (feed.visibility == "PRIVATE") "나만 보기" else "전체 공개"}",
                "댓글 쓴 분이 갑자기 못 보게 되지 않도록 처음 정한 값을 그대로 둬요."
            )
            EditDeleteLockedRow(
                Icons.Outlined.Map,
                feed.trip?.courseTitle ?: "연결된 여행",
                "어느 여행의 기록인지는 바꿀 수 없어요."
            )
        }

        // 삭제는 저장 CTA 와 멀리 떼어 놓는다 (20-3a 와 같은 규칙).
        Spacer(Modifier.height(18.dp))
        OutlinedButton(
            onClick = onDelete,
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("feed-edit-delete"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("이 기록 삭제하기", fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun EditDeleteLockedRow(icon: ImageVector, title: String, reason: String) {
    val colors = MaterialTheme.colorScheme
    Row(
        // 위아래 여백을 12dp 로 둔다 — 기획·웹·iOS 의 줄 높이(약 56pt)에 맞춘다.
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Icon(icon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(reason, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        }
        // 잠긴 항목이라는 표시 — iOS 도 줄 오른쪽에 자물쇠를 둔다.
        Icon(
            Icons.Outlined.Lock,
            contentDescription = "고칠 수 없어요",
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
    }
}

// ───────── 23-1a · 내 댓글 수정 · 삭제 ─────────

/**
 * 댓글은 **그 자리에서** 고친다 — 별 화면으로 보내면 어느 댓글을 고치는지 잃는다.
 *
 * 서버 한도는 **500자**다(신고 `details` 의 300자와 다르다).
 * 최상위 댓글을 지우면 **달린 답글도 함께** 사라진다 — 먼저 알려야 한다.
 * 내 댓글에만 진입점을 낸다 — 남의 댓글은 서버가 403 으로 막는다.
 */
@Composable
fun CommentEditScreen(feedId: Long, onBack: () -> Unit) {
    val server = LocalServerData.current
    val scope = rememberCoroutineScope()
    var threads by remember(feedId) { mutableStateOf<List<FeedComment>>(emptyList()) }
    var loaded by remember(feedId) { mutableStateOf(false) }
    var reloadToken by remember(feedId) { mutableStateOf(0) }
    var editingId by remember { mutableStateOf<Long?>(null) }
    var draft by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val myId = server?.signedInUserId?.invoke()
    val limit = 500

    LaunchedEffect(feedId, server, reloadToken) {
        if (server == null) return@LaunchedEffect
        threads = runCatching { server.feeds.comments(feedId, limit = 50).comments }
            .getOrDefault(emptyList())
        loaded = true
        // 이 화면(`comment-edit`)은 이름 그대로 **댓글을 고치러 오는 자리**다.
        // 인앱에서는 목록에서 「수정·삭제」를 눌러 대상을 들고 오지만, 라우트로 바로 열면
        // 대상이 없다 — 그때는 **내 가장 최근 댓글**을 편집 상태로 연다(기획 23-1a 와 같은 상태).
        if (editingId == null && myId != null) {
            newestOwnComment(threads, myId)?.let { mine ->
                editingId = mine.commentId
                draft = mine.content
            }
        }
    }

    GapScaffold(title = "댓글", onBack = onBack, testTag = "comment-edit", error = error) {
        if (threads.isEmpty()) {
            MoyeoEmptyState(
                if (loaded) MoyeoEmptyText.NO_COMMENTS else MoyeoEmptyText.LOADING,
                testTag = "comment-edit-empty"
            )
            return@GapScaffold
        }
        threads.forEach { comment ->
            CommentEditRow(
                comment = comment,
                depth = 0,
                myId = myId,
                state = CommentEditRowState(editingId, draft, busy, limit),
                actions = CommentEditRowActions(
                    onOpen = {
                        editingId = comment.commentId
                        draft = comment.content
                    },
                    onDraft = { draft = it },
                    onCancel = { editingId = null },
                    onSave = { id ->
                        if (server != null) {
                            busy = true
                            error = null
                            scope.launch {
                                runCatching { server.feeds.updateComment(feedId, id, draft.trim()) }
                                    .onSuccess {
                                        editingId = null
                                        reloadToken += 1
                                    }
                                    .onFailure { error = it.message ?: "댓글을 수정하지 못했어요." }
                                busy = false
                            }
                        }
                    },
                    onDelete = { id ->
                        if (server != null) {
                            busy = true
                            error = null
                            scope.launch {
                                runCatching { server.feeds.deleteComment(feedId, id) }
                                    .onSuccess {
                                        editingId = null
                                        reloadToken += 1
                                    }
                                    .onFailure { error = it.message ?: "댓글을 지우지 못했어요." }
                                busy = false
                            }
                        }
                    },
                    onNestedOpen = { id, text ->
                        editingId = id
                        draft = text
                    }
                )
            )
        }
    }
}

// ───────── 18-6 · 모집 내용 수정 (호스트) ─────────

/**
 * 18-5 집합 정보 수정과 **다른 화면**이다 — 그쪽은 `PUT .../meeting-info` 다.
 *
 * **모집 중인 방의 호스트만** 되고, 확정한 뒤에는 아무것도 못 고친다.
 * 저장하면 방에 `호스트가 모집 정보를 수정했어요.` 시스템 메시지가 남는다.
 */
@Composable
fun RecruitEditScreen(tripId: String, onBack: () -> Unit) {
    val server = LocalServerData.current
    val scope = rememberCoroutineScope()
    val roomId = tripId.serverRoomIdOrNull()
    var room by remember(roomId) { mutableStateOf<ChatRoomDetail?>(null) }
    var loaded by remember(roomId) { mutableStateOf(false) }
    var title by remember(roomId) { mutableStateOf("") }
    var description by remember(roomId) { mutableStateOf("") }
    var dayTrip by remember(roomId) { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    // 새 대표 사진. 고르지 않으면 **파트를 보내지 않아** 서버가 지금 사진을 그대로 둔다
    // (PATCH 규칙 · BE 회신 §4). 비우는 길은 없다.
    var pickedThumbnail by remember(roomId) { mutableStateOf<PickedImage?>(null) }
    val thumbnailPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val read = readPickedImage(context, uri)
            // 읽지 못하면 **바꾸지 않은 것으로 둔다** — 대체 이미지를 지어내지 않는다.
            if (read == null) {
                error = "사진을 읽지 못했어요. 20MB 를 넘지 않는 사진을 골라주세요."
            } else {
                error = null
                pickedThumbnail = read
            }
        }
    }

    LaunchedEffect(roomId, server) {
        if (server == null || roomId == null) return@LaunchedEffect
        val detail = runCatching { server.chatRooms.room(roomId) }.getOrNull()
        val myId = server.signedInUserId()
        // 모집 중인 내 방만 고칠 수 있다 — 아니면 화면을 내지 않는다(죽은 버튼을 두지 않는다).
        room = detail?.takeIf { it.hostId == myId && it.status == "RECRUITING" }
        room?.let {
            title = it.title
            description = it.description.orEmpty()
            dayTrip = it.tripType == "DAY_TRIP"
        }
        loaded = true
    }

    GapScaffold(
        title = "모집 내용 수정",
        onBack = onBack,
        testTag = "recruit-edit",
        cta = if (busy) "저장 중..." else "수정 저장",
        // 위임 프로퍼티는 스마트 캐스트가 안 된다 — 지역 변수로 받아 본다.
        ctaEnabled = room?.recruitmentDeadlineDate?.isNotBlank() == true &&
            title.isNotBlank() && !busy,
        error = error ?: NO_ROOM_EDIT_HINT.takeIf { loaded && room == null && server != null },
        onCta = {
            val current = room ?: return@GapScaffold
            if (server == null || roomId == null) return@GapScaffold
            busy = true
            error = null
            scope.launch {
                runCatching {
                    server.chatRooms.updateRoomContent(
                        roomId,
                        RoomContentUpdate(
                            title = title.trim(),
                            description = description.trim().ifEmpty { null },
                            dayTrip = dayTrip,
                            minimumParticipants = current.minimumParticipants,
                            maxParticipants = current.maxParticipants,
                            startDate = current.startDate,
                            endDate = current.endDate,
                            // 서버 요청에 필수다. 값이 없는 방은 CTA 가 잠겨 여기까지 오지 않는다.
                            recruitmentDeadlineDate = current.recruitmentDeadlineDate.orEmpty(),
                            dayTripStartTime = current.dayTripStartTime,
                            dayTripEndTime = current.dayTripEndTime,
                            participationFee = current.participationFee
                        ),
                        pickedThumbnail?.let {
                            MultipartFile("thumbnail", it.fileName, it.mimeType, it.bytes)
                        }
                    )
                }
                    .onSuccess { onBack() }
                    .onFailure { error = it.message ?: "모집 정보를 수정하지 못했어요." }
                busy = false
            }
        }
    ) {
        val current = room
        if (current == null) {
            MoyeoEmptyState(
                if (loaded) NO_ROOM_EDIT_HINT else MoyeoEmptyText.LOADING,
                testTag = "recruit-edit-empty"
            )
            return@GapScaffold
        }
        RecruitEditBody(
            room = current,
            form = RecruitEditForm(title, description, dayTrip),
            actions = RecruitEditFormActions(
                onTitle = { title = it },
                onDescription = { description = it },
                onDayTrip = { dayTrip = it }
            ),
            busy = busy,
            thumbnail = RecruitEditThumbnail(
                picked = pickedThumbnail,
                onPick = {
                    thumbnailPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )
        )
    }
}

// ───────── 20-6 · 내 채팅 메시지 삭제 ─────────

/**
 * 길게 눌러 여는 시트다. **내 메시지에만** 열린다 — 시스템 메시지·남의 메시지는 서버가 막는다.
 *
 * 행을 지우지 않고 본문을 `삭제된 메시지입니다` 로 바꾸므로
 * **id·보낸 사람·보낸 시각·답글 관계는 그대로 남는다** (BE 회신 §6).
 */
@Composable
fun MessageDeleteScreen(tripId: String, onBack: () -> Unit, onDeleted: () -> Unit) {
    val server = LocalServerData.current
    val scope = rememberCoroutineScope()
    val roomId = tripId.serverRoomIdOrNull()
    var message by remember(roomId) { mutableStateOf<RoomMessage?>(null) }
    var loaded by remember(roomId) { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(roomId, server) {
        if (server == null || roomId == null) return@LaunchedEffect
        val myId = server.signedInUserId()
        // 내가 보낸 **일반** 메시지 중 가장 최근 것. 시스템 메시지는 지울 수 없다.
        message = runCatching { server.chatRooms.messages(roomId, limit = 50).messages }
            .getOrDefault(emptyList())
            .firstOrNull { it.senderId == myId && it.type != "SYSTEM" }
        loaded = true
    }

    // 대상이 없을 때만 껍데기를 그린다. 대상이 있으면 **시트 하나**만 띄운다 —
    // 기획·웹은 뒤에 채팅방이 깔리고 상단 헤더가 없다. 헤더를 두면 안드로이드만 달라 보인다.
    if (message == null) {
        GapScaffold(title = "메시지 삭제", onBack = onBack, testTag = "message-delete", error = error) {
            MoyeoEmptyState(
                if (loaded) NO_MESSAGE_HINT else MoyeoEmptyText.LOADING,
                testTag = "message-delete-empty"
            )
        }
    }

    message?.let { target ->
        GapConfirmSheet(
            title = "이 메시지를 삭제할까요?",
            description = "“${target.content}” · ${target.createdAt.drop(11).take(5)}",
            lines = listOf(
                "모두에게서 사라지고 “삭제된 메시지입니다”만 남아요.",
                "사진·위치·투표도 내용만 사라지고 자리는 남아요.",
                "보낸 시각과 이 메시지에 달린 답글은 그대로예요.",
                "되돌릴 수 없어요."
            ),
            cancel = "그대로 둘게요",
            confirm = "삭제하기",
            danger = true,
            busy = busy,
            testTag = "message-delete-confirm",
            background = { ChatRoomScreen(threadId = tripId, isOnline = true, onBack = {}) },
            onDismiss = onBack,
            onConfirm = {
                if (server == null || roomId == null) return@GapConfirmSheet
                busy = true
                error = null
                scope.launch {
                    runCatching { server.chatRooms.deleteMessage(roomId, target.messageId) }
                        .onSuccess { onDeleted() }
                        .onFailure { error = it.message ?: "메시지를 지우지 못했어요." }
                    busy = false
                }
            }
        )
    }
}

/** 내 피드만 돌려준다. 남의 피드면 null — 서버도 403 으로 막는다. */
@Composable
private fun rememberMyFeed(feedId: Long): ServerFeed? {
    val server = LocalServerData.current
    var feed by remember(feedId) { mutableStateOf<ServerFeed?>(null) }
    LaunchedEffect(feedId, server) {
        if (server == null) return@LaunchedEffect
        val detail = runCatching { server.feeds.feed(feedId) }.getOrNull()
        val myId = server.signedInUserId()
        feed = detail?.takeIf { it.author.userId == myId }
    }
    return feed
}

/** 내가 쓴 댓글 중 가장 최근 것. 답글도 후보다(서버가 최상위를 최신순으로 준다). */
private fun newestOwnComment(comments: List<FeedComment>, myId: Long): FeedComment? {
    comments.forEach { comment ->
        if (comment.author.userId == myId) return comment
        newestOwnComment(comment.replies, myId)?.let { return it }
    }
    return null
}
