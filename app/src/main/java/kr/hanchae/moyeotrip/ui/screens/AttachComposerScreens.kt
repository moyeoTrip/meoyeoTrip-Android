package kr.hanchae.moyeotrip.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.data.ServerDataDependencies
import kr.hanchae.moyeotrip.data.rooms.ChatRoomDetail
import kr.hanchae.moyeotrip.data.rooms.NewPoll
import kr.hanchae.moyeotrip.data.rooms.ROOM_NOTICE_PIN_LIMIT
import kr.hanchae.moyeotrip.data.rooms.RoomNotices
import kr.hanchae.moyeotrip.data.rooms.publishNotice
import kr.hanchae.moyeotrip.data.tourism.TourismContentRepository
import kr.hanchae.moyeotrip.data.tourism.TourismContentSummary
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.KakaoMapView
import kr.hanchae.moyeotrip.ui.components.MapMarker
import kr.hanchae.moyeotrip.ui.components.MapMarkerShape
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyState
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyText
import kr.hanchae.moyeotrip.ui.components.MoyeoLatLng
import kr.hanchae.moyeotrip.ui.components.MoyeoPlaceholderShape

// 20-2 첨부 메뉴에서 이어지는 작성 화면 6종 (20-2a~20-2f).
// 정본: docs/alignment/ATTACH-COMPOSER-CANON.md
//
// 여섯 화면은 기획의 `AttachFrame` 골격(헤더 + 스크롤 본문 + 하단 고정 CTA 한 개)을 공유한다(R1).
// 제각각이면 같은 시트에서 나온 화면으로 읽히지 않는다.
//
// 모두 실서버 방("room-{id}")에서만 보낼 수 있다. 방이 없으면 CTA 를 잠그고 이유를 적는다 —
// 예시 데이터를 대신 그리지 않는다(NO-MOCK-CANON R1).

/** 공통 골격 (R1). [ctaEnabled] 가 false 면 하단 CTA 는 눌리지 않는다. */
@Composable
private fun AttachFrame(
    title: String,
    hint: String?,
    cta: String,
    ctaEnabled: Boolean,
    onBack: () -> Unit,
    onSend: () -> Unit,
    testTag: String,
    error: String? = null,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로", tint = colors.onBackground)
            }
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = colors.onBackground,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.size(48.dp))
        }
        if (hint != null) {
            Text(
                text = hint,
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
            HorizontalDivider(color = colors.outline.copy(alpha = .45f))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }
        Surface(color = colors.surface, shadowElevation = 8.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                error?.let {
                    Text(
                        text = it,
                        modifier = Modifier.testTag("$testTag-error"),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.error
                    )
                }
                Button(
                    onClick = onSend,
                    enabled = ctaEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("$testTag-cta"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(cta, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

/** 작성 화면마다 반복되는 작은 제목. */
@Composable
private fun FieldLabel(text: String, required: Boolean = false) {
    Text(
        text = if (required) "$text *" else text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.ExtraBold
    )
}

/** "이건 이렇게 동작해요" 안내 상자. */
@Composable
private fun NoteBox(lines: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            lines.forEach { line ->
                Text(
                    text = "· $line",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** 켜고 끄는 줄 — 익명·상단 고정이 같은 모양을 쓴다. */
@Composable
private fun ToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    enabled: Boolean = true,
    testTag: String,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onToggle(!checked) }
            .testTag(testTag)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(
                description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onToggle, enabled = enabled)
    }
}

/** 방이 지정되지 않았을 때의 공통 안내 — 예시 데이터를 그리는 대신 이유를 적는다. */
private const val NO_ROOM_HINT = "보낼 모임을 찾지 못했어요. 채팅방에서 다시 열어주세요."

/** 실서버 방 id 를 꺼내고, 없으면 null. */
@Composable
private fun rememberComposerRoom(threadId: String?): Pair<Long, ServerDataDependencies>? {
    val server = LocalServerData.current ?: return null
    val roomId = threadId?.serverRoomIdOrNull() ?: return null
    return roomId to server
}

// ───────── 20-2a · 사진 보내기 ─────────

/**
 * 20-2a 사진 보내기 — POST chat-rooms/{id}/messages/images (multipart).
 *
 * 안드로이드는 사진 접근 권한 없이 "최근 사진" 격자를 만들 수 없다. 그래서 기획의 격자 자리에는
 * 시스템 사진 선택기를 두고, 고른 한 장을 **보내기 전 미리보기**로 크게 보여준다(R2).
 * 사진 목록을 앱이 지어내지 않는다.
 */
@Composable
fun AttachPhotoScreen(threadId: String?, onBack: () -> Unit) {
    val target = rememberComposerRoom(threadId)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var picked by remember { mutableStateOf<PickedImage?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val read = readPickedImage(context, uri)
            if (read == null) {
                error = "사진을 읽지 못했어요. 20MB 를 넘지 않는 사진을 골라주세요."
            } else {
                error = null
                picked = read
            }
        }
    }

    AttachFrame(
        title = "사진 보내기",
        hint = "한 번에 1장씩 보내요. 20MB 까지 올릴 수 있어요.",
        cta = if (busy) "보내는 중..." else "이 사진 보내기",
        ctaEnabled = target != null && picked != null && !busy,
        onBack = onBack,
        onSend = {
            val (roomId, server) = target ?: return@AttachFrame
            val image = picked ?: return@AttachFrame
            busy = true
            scope.launch {
                runCatching { server.chatRooms.shareImage(roomId, image.fileName, image.mimeType, image.bytes) }
                    .onSuccess { onBack() }
                    .onFailure { error = it.message ?: "사진을 보내지 못했어요." }
                busy = false
            }
        },
        testTag = "attach-photo",
        error = error ?: NO_ROOM_HINT.takeIf { target == null }
    ) {
        val image = picked
        if (image == null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(188.dp)
                    .clickable {
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                    .testTag("attach-photo-pick"),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("사진 고르기", fontWeight = FontWeight.ExtraBold)
                }
            }
        } else {
            FieldLabel("이렇게 보내져요")
            val bitmap = remember(image) {
                runCatching { BitmapFactory.decodeByteArray(image.bytes, 0, image.bytes.size) }.getOrNull()
            }
            Box {
                if (bitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "고른 사진",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(188.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp),
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = listOfNotNull(
                            image.bytes.size.megabytesText(),
                            bitmap?.let { "${it.width}×${it.height}" }
                        ).joinToString(" · "),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = "다른 사진 고르기",
                modifier = Modifier
                    .clickable {
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                    .testTag("attach-photo-pick")
                    .padding(vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
        }
        NoteBox(
            listOf(
                "보낸 사진은 채팅방 사이드 메뉴의 공유 항목에 모여요.",
                "20MB 를 넘으면 보내기 전에 알려드려요."
            )
        )
    }
}

private fun Int.megabytesText(): String = String.format("%.1fMB", this / 1024.0 / 1024.0)

// ───────── 20-2b · 장소 카드 보내기 ─────────

/**
 * 20-2b 장소 카드 — GET tourism-contents 로 찾고 POST chat-rooms/{id}/messages/tourism-contents 로 보낸다.
 *
 * 20-2c 지도와 **다른 화면**이다(R3). 이쪽은 이름·분류·사진이 있는 TourAPI 관광지 카드다.
 */
@Composable
fun AttachPlaceScreen(threadId: String?, onBack: () -> Unit, repository: TourismContentRepository) {
    val target = rememberComposerRoom(threadId)
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<TourismContentSummary>?>(null) }
    var searchFailed by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var pickedId by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(repository, query, reloadKey) {
        delay(SEARCH_DEBOUNCE_MILLIS)
        results = null
        searchFailed = false
        runCatching { repository.contents(keyword = query, size = 30) }.fold(
            onSuccess = { results = it.items },
            onFailure = {
                searchFailed = true
                results = emptyList()
            }
        )
    }
    val chosen = results.orEmpty().firstOrNull { it.contentId == pickedId }

    AttachFrame(
        title = "장소 카드 보내기",
        hint = "관광 정보에서 찾은 장소를 카드로 보내요.",
        cta = if (busy) "보내는 중..." else "이 장소 보내기",
        ctaEnabled = target != null && chosen != null && !busy,
        onBack = onBack,
        onSend = {
            val (roomId, server) = target ?: return@AttachFrame
            val contentId = chosen?.contentId?.toLongOrNull() ?: return@AttachFrame
            busy = true
            scope.launch {
                runCatching { server.chatRooms.shareTourismContent(roomId, contentId) }
                    .onSuccess { onBack() }
                    .onFailure { error = it.message ?: "장소를 보내지 못했어요." }
                busy = false
            }
        },
        testTag = "attach-place",
        error = error ?: NO_ROOM_HINT.takeIf { target == null }
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().testTag("attach-place-query"),
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            placeholder = { Text("장소 이름이나 지역을 검색해보세요") },
            singleLine = true
        )
        val list = results
        when {
            list == null -> MoyeoEmptyState(MoyeoEmptyText.LOADING)

            searchFailed -> MoyeoEmptyState(MoyeoEmptyText.FAILED, onRetry = { reloadKey++ })

            list.isEmpty() -> MoyeoEmptyState(MoyeoEmptyText.NO_SEARCH_RESULTS, testTag = "attach-place-empty")

            else -> {
                Text(
                    text = "${list.size}곳을 찾았어요",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                list.forEach { place ->
                    AttachPlaceRow(
                        place = place,
                        selected = place.contentId == pickedId,
                        onClick = { pickedId = place.contentId }
                    )
                }
            }
        }
        // 보내기 전 미리보기 — 21 특수 메시지의 장소 카드와 같은 생김새다 (R2)
        chosen?.let { place ->
            FieldLabel("이렇게 보내져요")
            Surface(
                modifier = Modifier.fillMaxWidth().testTag("attach-place-preview"),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column {
                    CachedRemoteImage(
                        url = place.thumbnailUrl,
                        contentDescription = place.title,
                        modifier = Modifier.fillMaxWidth().height(112.dp),
                        contentScale = ContentScale.Crop,
                        fallbackShape = MoyeoPlaceholderShape.LANDSCAPE
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(112.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(place.title, fontWeight = FontWeight.ExtraBold)
                        place.addressText()?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 주소는 서버가 주는 두 칸을 이어 붙인다. 둘 다 없으면 줄 자체가 사라진다. */
private fun TourismContentSummary.addressText(): String? =
    listOfNotNull(address1?.takeIf(String::isNotBlank), address2?.takeIf(String::isNotBlank))
        .joinToString(" ")
        .takeIf(String::isNotBlank)

@Composable
private fun AttachPlaceRow(place: TourismContentSummary, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("attach-place-${place.contentId}"),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) colors.primaryContainer else colors.surface,
        border = BorderStroke(1.dp, if (selected) colors.primary else colors.outline)
    ) {
        Row(
            Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            CachedRemoteImage(
                url = place.thumbnailUrl,
                contentDescription = place.title,
                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
                fallbackShape = MoyeoPlaceholderShape.SQUARE
            ) {
                Box(Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)).background(colors.surfaceVariant))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    place.title,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                place.addressText()?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ───────── 20-2c · 만날 위치 보내기 ─────────

/**
 * 20-2c 만날 위치 — POST chat-rooms/{id}/messages/locations.
 *
 * **서버 요청에 본문이 없다.** 보내지는 좌표는 방에 등록된 집합 좌표 하나뿐이라,
 * 기획의 "지도를 끌어 핀을 놓고 한 줄 설명을 적는" 입력은 근거가 없다 — 지어내지 않고
 * 방의 실제 집합 좌표를 **실제 카카오 지도**에 올려 보여준다(NO-MOCK-CANON R3·R4).
 * 임의 좌표 전송은 §4 BE 요청으로 넘긴다.
 *
 * 집합 좌표가 없으면 지도를 그리지 않고 CTA 를 잠근다.
 */
@Composable
fun AttachMapScreen(threadId: String?, onBack: () -> Unit) {
    val target = rememberComposerRoom(threadId)
    val scope = rememberCoroutineScope()
    var room by remember(target?.first) { mutableStateOf<ChatRoomDetail?>(null) }
    var loadFailed by remember(target?.first) { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(target?.first) {
        val (roomId, server) = target ?: return@LaunchedEffect
        val loaded = runCatching { server.chatRooms.room(roomId) }.getOrNull()
        room = loaded
        loadFailed = loaded == null
    }

    val point = room?.let { detail ->
        val latitude = detail.meetingLatitude
        val longitude = detail.meetingLongitude
        if (latitude == null || longitude == null || (latitude == 0.0 && longitude == 0.0)) {
            null
        } else {
            MoyeoLatLng(latitude, longitude)
        }
    }

    AttachFrame(
        title = "만날 위치 보내기",
        hint = "모임에 등록된 집합 장소를 지도 카드로 보내요.",
        cta = if (busy) "보내는 중..." else "이 위치 보내기",
        ctaEnabled = target != null && point != null && !busy,
        onBack = onBack,
        onSend = {
            val (roomId, server) = target ?: return@AttachFrame
            busy = true
            scope.launch {
                runCatching { server.chatRooms.shareMeetingLocation(roomId) }
                    .onSuccess { onBack() }
                    .onFailure { error = it.message ?: "만날 위치를 보내지 못했어요." }
                busy = false
            }
        },
        testTag = "attach-map",
        error = error ?: NO_ROOM_HINT.takeIf { target == null }
    ) {
        when {
            target == null -> Unit

            room == null && loadFailed -> MoyeoEmptyState(MoyeoEmptyText.FAILED)

            room == null -> MoyeoEmptyState(MoyeoEmptyText.LOADING)

            point == null -> MoyeoEmptyState(
                text = "아직 집합 장소가 정해지지 않았어요.",
                testTag = "attach-map-no-point"
            )

            else -> {
                // 좌표가 있을 때만 실지도를 그린다. 손으로 그린 지도를 대신 두지 않는다(R4).
                KakaoMapView(
                    center = point,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .testTag("attach-map-view"),
                    markers = listOf(MapMarker("attach-meeting-point", point, MapMarkerShape.Pin)),
                    zoomLevel = 16
                ) { modifier ->
                    Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant))
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            null,
                            Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            room?.meetingDetails?.takeIf(String::isNotBlank)?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "${point.latitude}, ${point.longitude}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        NoteBox(
            listOf(
                "받는 사람은 카드를 눌러 지도 앱으로 길찾기를 열 수 있어요.",
                "집합 장소를 바꾸는 건 아니에요. 집합 장소는 호스트가 모집 정보에서 고쳐요."
            )
        )
    }
}

// ───────── 20-2d · 투표 만들기 ─────────

/**
 * 20-2d 투표 — POST chat-rooms/{id}/messages/polls.
 *
 * 항목은 2~5개다(R5). 2개일 때는 삭제가 잠기고(투표가 성립하지 않는다) 5개에서 추가가 잠긴다.
 * **익명이 기본값**이다. 기획의 "여러 개 고르기"는 서버 요청(`question·options·anonymous`)에
 * 대응 필드가 없어 두지 않는다 — 켜도 서버에 전해지지 않을 스위치를 그리지 않는다(NO-MOCK-CANON R3).
 */
@Composable
fun AttachPollScreen(threadId: String?, onBack: () -> Unit) {
    val target = rememberComposerRoom(threadId)
    val scope = rememberCoroutineScope()
    var question by remember { mutableStateOf("") }
    val options = remember { mutableStateListOf("", "") }
    var anonymous by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val filled = options.map(String::trim).filter(String::isNotEmpty)
    val ready = question.isNotBlank() && filled.size >= POLL_MIN_OPTIONS && filled.size == options.size

    AttachFrame(
        title = "투표 만들기",
        hint = "항목은 ${POLL_MIN_OPTIONS}~${POLL_MAX_OPTIONS}개까지예요.",
        cta = if (busy) "올리는 중..." else "투표 올리기",
        ctaEnabled = target != null && ready && !busy,
        onBack = onBack,
        onSend = {
            val (roomId, server) = target ?: return@AttachFrame
            busy = true
            scope.launch {
                runCatching {
                    server.chatRooms.createPoll(roomId, NewPoll(question.trim(), filled, anonymous))
                }
                    .onSuccess { onBack() }
                    .onFailure { error = it.message ?: "투표를 올리지 못했어요." }
                busy = false
            }
        },
        testTag = "attach-poll",
        error = error ?: NO_ROOM_HINT.takeIf { target == null }
    ) {
        FieldLabel("무엇을 물어볼까요", required = true)
        OutlinedTextField(
            value = question,
            onValueChange = { question = it },
            modifier = Modifier.fillMaxWidth().testTag("attach-poll-question"),
            placeholder = { Text("예: 점심 뭐 먹을까요?") },
            singleLine = true
        )
        FieldLabel("선택 항목", required = true)
        options.forEachIndexed { index, option ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = option,
                    onValueChange = { options[index] = it },
                    modifier = Modifier.weight(1f).testTag("attach-poll-option-$index"),
                    placeholder = { Text("항목 ${index + 1}") },
                    singleLine = true
                )
                // 2개까지는 지울 수 없다 — 투표가 성립하지 않는다 (R5)
                IconButton(
                    onClick = { if (options.size > POLL_MIN_OPTIONS) options.removeAt(index) },
                    enabled = options.size > POLL_MIN_OPTIONS,
                    modifier = Modifier.testTag("attach-poll-remove-$index")
                ) {
                    Icon(Icons.Filled.Close, "항목 ${index + 1} 삭제", Modifier.size(16.dp))
                }
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = options.size < POLL_MAX_OPTIONS) { options.add("") }
                .testTag("attach-poll-add"),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                Modifier.fillMaxWidth().height(44.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val full = options.size >= POLL_MAX_OPTIONS
                Icon(
                    Icons.Filled.Add,
                    null,
                    Modifier.size(15.dp),
                    tint = if (full) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                Text(
                    text = if (full) "항목은 ${POLL_MAX_OPTIONS}개까지예요" else "항목 추가",
                    modifier = Modifier.padding(start = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (full) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .45f))
        ToggleRow(
            label = "익명 투표",
            description = "누가 무엇을 골랐는지 아무도 못 봐요. 결과 숫자만 보여요.",
            checked = anonymous,
            testTag = "attach-poll-anonymous",
            onToggle = { anonymous = it }
        )
        NoteBox(
            listOf(
                "투표는 만든 사람이 언제든 마감할 수 있어요.",
                "마감하면 결과가 채팅방 카드에 그대로 남아요."
            )
        )
    }
}

private const val POLL_MIN_OPTIONS = 2
private const val POLL_MAX_OPTIONS = 5

// ───────── 20-2e · 정산 메모 ─────────

/**
 * 20-2e 정산 메모 — POST chat-rooms/{id}/messages/settlement-memos.
 *
 * **송금이 아니다**(R4). 서버가 받는 값은 `memo` 한 줄이라 항목·총액·인원·1인당을
 * 한 줄로 엮어 보낸다. 1인당 금액은 입력이 아니라 **계산 결과**이고 10원 단위 올림이다.
 */
@Composable
fun AttachSettlementScreen(threadId: String?, onBack: () -> Unit) {
    val target = rememberComposerRoom(threadId)
    val scope = rememberCoroutineScope()
    var subject by remember { mutableStateOf("") }
    var totalText by remember { mutableStateOf("") }
    var people by remember { mutableIntStateOf(2) }
    var note by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val total = totalText.filter(Char::isDigit).toIntOrNull()
    val each = settlementShare(total, people)
    val ready = subject.isNotBlank() && total != null && total > 0

    AttachFrame(
        title = "정산 메모",
        hint = "얼마를 어떻게 나눌지 적어두는 메모예요. 앱에서 돈이 오가지는 않아요.",
        cta = if (busy) "올리는 중..." else "정산 메모 올리기",
        ctaEnabled = target != null && ready && !busy,
        onBack = onBack,
        onSend = {
            val (roomId, server) = target ?: return@AttachFrame
            val amount = total ?: return@AttachFrame
            busy = true
            scope.launch {
                val memo = settlementMemoText(subject.trim(), amount, people, each ?: 0, note.trim())
                runCatching { server.chatRooms.shareSettlementMemo(roomId, memo) }
                    .onSuccess { onBack() }
                    .onFailure { error = it.message ?: "정산 메모를 올리지 못했어요." }
                busy = false
            }
        },
        testTag = "attach-settlement",
        error = error ?: NO_ROOM_HINT.takeIf { target == null }
    ) {
        FieldLabel("무엇을 정산하나요", required = true)
        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            modifier = Modifier.fillMaxWidth().testTag("attach-settlement-subject"),
            placeholder = { Text("예: 점심 · 백숙") },
            singleLine = true
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FieldLabel("총 금액", required = true)
                OutlinedTextField(
                    value = totalText,
                    onValueChange = { totalText = it.filter(Char::isDigit).take(9) },
                    modifier = Modifier.fillMaxWidth().testTag("attach-settlement-total"),
                    suffix = { Text("원") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FieldLabel("나눌 인원", required = true)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (people > 2) people-- },
                        enabled = people > 2,
                        modifier = Modifier.testTag("attach-settlement-people-minus")
                    ) {
                        Text("−", fontWeight = FontWeight.ExtraBold)
                    }
                    Text(
                        "$people",
                        modifier = Modifier.testTag("attach-settlement-people"),
                        fontWeight = FontWeight.ExtraBold
                    )
                    IconButton(
                        onClick = { people++ },
                        modifier = Modifier.testTag("attach-settlement-people-plus")
                    ) {
                        Icon(Icons.Filled.Add, "인원 늘리기", Modifier.size(16.dp))
                    }
                }
            }
        }
        // 1인당 금액은 입력이 아니라 계산 결과다 — 사람들이 실제로 궁금해하는 숫자라 크게 보여준다
        Surface(
            modifier = Modifier.fillMaxWidth().testTag("attach-settlement-share"),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Filled.Payments, null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f)) {
                    Text(
                        "1인당",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = each?.let { "${it.wonText()}원" } ?: "-",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    "10원 단위로\n올림했어요",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.End
                )
            }
        }
        FieldLabel("남길 말 (선택)")
        OutlinedTextField(
            value = note,
            onValueChange = { note = it.take(200) },
            modifier = Modifier.fillMaxWidth().testTag("attach-settlement-note"),
            placeholder = { Text("예: 계좌는 채팅방에 따로 남길게요.") },
            minLines = 3
        )
        NoteBox(
            listOf(
                "모여트립은 송금을 하지 않아요. 실제 정산은 각자 하셔야 해요.",
                "계좌번호는 아무나 볼 수 있으니 꼭 필요할 때만 남겨주세요."
            )
        )
    }
}

/** 1인당 금액 — 10원 단위 올림 (R4). 총액·인원이 없으면 계산하지 않는다. */
internal fun settlementShare(total: Int?, people: Int): Int? {
    if (total == null || total <= 0 || people <= 0) return null
    return (ceil(total.toDouble() / people / 10.0) * 10).toInt()
}

/** 서버가 받는 값은 `memo` 한 줄이라 화면에서 만든 숫자를 그 한 줄에 엮는다. */
internal fun settlementMemoText(subject: String, total: Int, people: Int, each: Int, note: String): String =
    buildString {
        append(subject)
        append(" — 총 ${total.wonText()}원 · ${people}명 · 1인당 ${each.wonText()}원")
        if (note.isNotBlank()) append(" · $note")
    }

private fun Int.wonText(): String = "%,d".format(this)

// ───────── 20-2f · 공지 작성 (호스트) ─────────

/**
 * 20-2f 공지 작성 — POST chat-rooms/{id}/notices.
 *
 * **제목 칸이 없다**(정본 §2). 서버 모델도 `notice` 문자열 하나뿐이라 제목을 두면 클라가 지어내야 했다.
 * 상단 고정은 **최대 1개**다(R5-1) — 이미 고정된 공지가 있으면 새로 고정할 때 그것이 풀린다.
 */
@Composable
fun AttachNoticeScreen(threadId: String?, onBack: () -> Unit) {
    val target = rememberComposerRoom(threadId)
    val scope = rememberCoroutineScope()
    var body by remember { mutableStateOf("") }
    var pinned by remember { mutableStateOf(true) }
    var notices by remember(target?.first) { mutableStateOf<RoomNotices?>(null) }
    // 호스트만 올릴 수 있다 — 20-3 과 같은 판정(GET {id}/members 의 `me && host`)을 쓴다.
    // 아니면 서버가 거절할 요청을 보내게 두는 대신 CTA 를 잠그고 이유를 적는다.
    var isHost by remember(target?.first) { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(target?.first) {
        val (roomId, server) = target ?: return@LaunchedEffect
        notices = runCatching { server.chatRooms.notices(roomId) }.getOrNull()
        isHost = runCatching { server.chatRooms.members(roomId) }.getOrNull()
            ?.members
            ?.any { it.me && it.host } == true
    }
    val alreadyPinned = notices?.pinned.orEmpty().isNotEmpty()

    AttachFrame(
        title = "공지 작성",
        hint = "공지는 호스트만 올릴 수 있어요.",
        cta = if (busy) "올리는 중..." else "공지 올리기",
        ctaEnabled = target != null && isHost && body.isNotBlank() && !busy,
        onBack = onBack,
        onSend = {
            val (roomId, server) = target ?: return@AttachFrame
            busy = true
            scope.launch {
                // 고정을 켠 채로 올리면 기존 고정 공지를 먼저 푼다 — 서버가 개수를 막지 않는다(R5-1)
                runCatching { server.chatRooms.publishNotice(roomId, body.trim(), pinned) }
                    .onSuccess { onBack() }
                    .onFailure { error = it.message ?: "공지를 올리지 못했어요." }
                busy = false
            }
        },
        testTag = "attach-notice",
        error = error
            ?: NO_ROOM_HINT.takeIf { target == null }
            ?: "공지는 호스트만 올릴 수 있어요.".takeIf { notices != null && !isHost }
    ) {
        FieldLabel("공지 내용", required = true)
        OutlinedTextField(
            value = body,
            onValueChange = { body = it.take(NOTICE_MAX_LENGTH) },
            modifier = Modifier.fillMaxWidth().testTag("attach-notice-body"),
            placeholder = { Text("멤버에게 알릴 내용을 적어주세요") },
            minLines = 6,
            supportingText = { Text("${body.length}/$NOTICE_MAX_LENGTH") }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .45f))
        ToggleRow(
            label = "상단에 고정하기",
            description = if (alreadyPinned) NOTICE_PIN_REPLACES else NOTICE_PIN_PLAIN,
            checked = pinned,
            testTag = "attach-notice-pin",
            onToggle = { pinned = it }
        )
        // 고정을 켰을 때만 미리보기 — 채팅방 위에 어떻게 얹히는지 보여준다 (R2)
        if (pinned && body.isNotBlank()) {
            FieldLabel("채팅방 맨 위에 이렇게 보여요")
            Surface(
                modifier = Modifier.fillMaxWidth().testTag("attach-notice-preview"),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Description,
                        null,
                        Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = body.trim(),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("📌", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        NoteBox(
            listOf(
                "고정을 해제해도 공지 이력에는 그대로 남아요.",
                "공지를 올리면 방 사람들에게 알림이 가요."
            )
        )
    }
}

/**
 * 고정 토글 설명 — 세 플랫폼이 **글자 그대로** 같아야 한다 (`ATTACH-COMPOSER-CANON.md` R5-1).
 * 새 문구를 만들지 않는다.
 */
internal const val NOTICE_PIN_PLAIN = "채팅방 맨 위에 계속 보여요."
internal const val NOTICE_PIN_REPLACES = "지금 고정된 공지가 있어요. 이걸 고정하면 그 공지는 풀려요."

/** 서버 공지 본문 상한. 웹 작성 화면과 같은 값이다. */
internal const val NOTICE_MAX_LENGTH = 300

/** 20-3 머리글의 `고정 N / 최대 1` 표기 — [ROOM_NOTICE_PIN_LIMIT] 을 그대로 쓴다. */
internal fun noticeHeaderText(total: Int, pinned: Int): String = "공지 ${total}개 · 고정 $pinned / 최대 $ROOM_NOTICE_PIN_LIMIT"

/** 검색 입력 디바운스 — 17-1a 방문지 검색과 같은 값이다. */
private const val SEARCH_DEBOUNCE_MILLIS = 250L
