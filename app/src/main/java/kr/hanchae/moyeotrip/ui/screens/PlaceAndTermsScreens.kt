package kr.hanchae.moyeotrip.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kr.hanchae.moyeotrip.data.RecruitmentDraftStore
import kr.hanchae.moyeotrip.data.RouteStop
import kr.hanchae.moyeotrip.data.auth.AuthDependencies
import kr.hanchae.moyeotrip.data.terms.TermDetail
import kr.hanchae.moyeotrip.data.tourism.TourismContentDetail
import kr.hanchae.moyeotrip.data.tourism.TourismContentRepository
import kr.hanchae.moyeotrip.data.tourism.TourismContentSummary
import kr.hanchae.moyeotrip.data.tourism.TourismContentTypeOption
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.KakaoMapView
import kr.hanchae.moyeotrip.ui.components.MapMarker
import kr.hanchae.moyeotrip.ui.components.MapMarkerShape
import kr.hanchae.moyeotrip.ui.components.MapUnavailablePlaceholder
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyState
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyText
import kr.hanchae.moyeotrip.ui.components.MoyeoLatLng
import kr.hanchae.moyeotrip.ui.components.MoyeoPlaceholderShape
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

/** 한 글자 칠 때마다 서버를 부르지 않도록 두는 최소 간격. */
private const val SEARCH_DEBOUNCE_MILLIS = 250L

internal enum class TourismContentType(val label: String, val icon: ImageVector) {
    Spot("관광지", Icons.Filled.LocationOn),
    Food("식당", Icons.Filled.Restaurant),
    Stay("숙박", Icons.Filled.Home);

    val apiId: Int
        get() = when (this) {
            Spot -> 12
            Stay -> 32
            Food -> 39
        }

    companion object {
        fun fromApiId(value: Int): TourismContentType = entries.firstOrNull { it.apiId == value } ?: Spot
    }
}

/**
 * 우측 상단 공유(17-1b 방문지 상세 · 08-C 약관 상세) — 안드로이드 공유 시트를 띄운다.
 *
 * 웹은 같은 자리에서 `navigator.share`(없으면 클립보드)를 쓴다. 안드로이드에는 그에 대응하는
 * `ACTION_SEND` 가 있어서 클립보드 대체가 필요하지 않다. 보내는 내용은 화면에 이미 보이는
 * 값(제목 + 본문)뿐이다 — 좌표처럼 화면에서 뺀 값은 넣지 않는다.
 *
 * 받을 앱이 하나도 없으면(에뮬레이터에 공유 대상이 없을 수 있다) 조용히 넘어간다 —
 * 웹도 사용자가 취소한 경우를 조용히 넘긴다.
 */
private fun shareText(context: Context, title: String, body: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, listOf(title, body).filter(String::isNotBlank).joinToString("\n\n"))
    }
    runCatching { context.startActivity(Intent.createChooser(send, title)) }
}

internal data class TourismPlace(
    val contentId: String,
    val type: TourismContentType,
    val title: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val postalCode: String = "",
    val phone: String = "",
    val phoneLabel: String = "",
    val homepage: String = "",
    val description: String = "",
    val photoCount: Int = 0,
    val menuNames: List<String> = emptyList(),
    val thumbnailUrl: String? = null,
    val photoUrls: List<String> = emptyList(),
    val menuImageUrls: List<String> = emptyList(),
    /** 서버 원본 타입 id 와 이름 — 이름은 GET tourism-contents/types 에서 받은 값만 채운다. */
    val contentTypeId: Int = 0,
    val typeLabel: String? = null
)

/** 서버 타입 후보(GET tourism-contents/types)로 배지 이름을 바꿔 준다. 후보가 없으면 그대로 둔다. */
internal fun TourismPlace.withServerTypeLabel(options: List<TourismContentTypeOption>): TourismPlace {
    val name = options.firstOrNull { it.contentTypeId == contentTypeId }?.contentTypeName ?: return this
    return copy(typeLabel = name)
}

/** 상세를 받아오기 전 자리값. 서버 응답이 오기 전까지는 값이 없는 카드다. */
internal fun emptyTourismPlace(contentId: String): TourismPlace =
    TourismPlace(contentId, TourismContentType.Spot, "", "", 0.0, 0.0)

@Composable
fun PlaceSearchScreen(
    draftId: String,
    onBack: () -> Unit,
    onOpenDetail: (String, String) -> Unit,
    onDone: (String) -> Unit,
    repository: TourismContentRepository
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedTypeId by rememberSaveable { mutableStateOf<Int?>(null) }
    var showsMap by rememberSaveable { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<TourismPlace>>(emptyList()) }
    var totalCount by remember { mutableIntStateOf(0) }
    var searchFailed by remember { mutableStateOf(false) }
    // 담은 방문지는 초안이 근거다 — 화면 안에서만 사는 "이미 담긴 목록"을 미리 채우지 않는다
    val draft = remember(draftId) { RecruitmentDraftStore.draft(draftId) }
    val addedIds = remember(draftId) {
        mutableStateListOf<String>().apply {
            addAll(draft.routeStops.mapNotNull { it.id.substringAfterLast("place-", "").takeIf(String::isNotEmpty) })
        }
    }
    // 타입 칩 후보는 서버(GET tourism-contents/types)에서 받는다 — 못 받으면 칩 줄이 사라진다
    var typeOptions by remember(repository) { mutableStateOf<List<TourismContentTypeOption>>(emptyList()) }
    LaunchedEffect(repository) {
        // 위와 같은 이유로 취소를 실패로 보지 않는다 — 보면 칩 줄이 잠깐 사라진다.
        typeOptions = try {
            repository.types()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Throwable) {
            emptyList()
        }
    }
    // 검색은 서버가 한다(GET tourism-contents?keyword=&contentTypeId=) — 클라이언트에서 다시 거르지 않는다.
    LaunchedEffect(repository, selectedTypeId, typeOptions, query) {
        delay(SEARCH_DEBOUNCE_MILLIS)
        // `runCatching` 을 쓰면 안 된다 — **코루틴 취소(`CancellationException`)까지 잡아
        // 실패로 만든다.** 이 효과는 `typeOptions` 가 도착하거나 검색어·타입이 바뀌면 다시
        // 실행되고, 그때 진행 중이던 요청이 취소되면서 「불러오지 못했어요」가 잠깐 떴다.
        // 캡처가 그 창을 찍어 매 회차마다 다른 페이지가 실패로 기록됐다(서버는 12회 연속 200).
        try {
            val page = repository.contents(keyword = query, contentTypeId = selectedTypeId)
            searchFailed = false
            results = page.items.map { summary -> summary.toPlace().withServerTypeLabel(typeOptions) }
            totalCount = page.totalElements.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        } catch (cancelled: CancellationException) {
            throw cancelled          // 취소는 실패가 아니다 — 상태를 건드리지 않고 그대로 넘긴다
        } catch (error: Throwable) {
            searchFailed = true
            results = emptyList()
            totalCount = 0
        }
    }

    /** 담기/빼기는 초안 방문지에 바로 반영한다 — 좌표까지 함께 넣어야 코스 미리보기 지도가 그려진다. */
    fun toggleAdded(place: TourismPlace) {
        val current = RecruitmentDraftStore.draft(draftId)
        val stopId = "$draftId-place-${place.contentId}"
        val updated = if (place.contentId in addedIds) {
            addedIds.remove(place.contentId)
            current.routeStops.filterNot { it.id == stopId }
        } else {
            addedIds.add(place.contentId)
            current.routeStops + RouteStop(
                id = stopId,
                // 17-1 이 나눠 둔 마지막 날에 담는다 — 날을 늘렸는데 Day 1 로 들어가면 되돌릴 수 없다
                day = current.dayCount,
                time = "",
                name = place.title,
                memo = place.address,
                latitude = place.latitude.takeIf { it != 0.0 },
                longitude = place.longitude.takeIf { it != 0.0 }
            )
        }
        RecruitmentDraftStore.update(current.copy(routeStops = updated))
    }

    Scaffold(
        topBar = {
            ChangeLogTopBar("방문지 검색", onBack) {
                // 화면기획의 우측 상단 "지도에서 보기"
                IconButton(
                    onClick = { showsMap = !showsMap },
                    modifier = Modifier.testTag("place-search-map-toggle")
                ) {
                    Icon(Icons.Filled.Map, "지도에서 보기", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        },
        bottomBar = {
            Surface(shadowElevation = 10.dp) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("${addedIds.size}곳 담김", Modifier.weight(1f), fontWeight = FontWeight.ExtraBold)
                    Button(onClick = {
                        onDone(draftId)
                    }, modifier = Modifier.testTag("place-search-done"), shape = RoundedCornerShape(12.dp)) {
                        Text("코스에 반영")
                    }
                }
            }
        }
    ) { padding ->
        // 검색 입력과 분류 칩은 **고정**이다 — 웹·iOS 처럼 목록만 스크롤된다.
        // 이전에는 둘을 LazyColumn 의 첫 item 으로 넣어 결과와 함께 밀려 올라갔다.
        Column(Modifier.fillMaxSize().padding(padding).testTag("place-search-screen")) {
            Column(
                Modifier.padding(horizontal = 20.dp, vertical = 8.dp).testTag("place-search-sticky-header"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().testTag("place-search-query"),
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    placeholder = { Text("지역이나 장소 이름") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PlaceTypeChip("전체", selectedTypeId == null) { selectedTypeId = null }
                    typeOptions.forEach { type ->
                        PlaceTypeChip(type.contentTypeName, selectedTypeId == type.contentTypeId) {
                            selectedTypeId = type.contentTypeId
                        }
                    }
                }
            }
            LazyColumn(
                Modifier.fillMaxWidth().weight(1f).testTag("place-search-results"),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                item {
                    Column(
                        Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (showsMap) {
                            // "지도에서 보기" — TourAPI 방문지 좌표를 그대로 실지도 순번 마커로 올린다
                            val placeMarkers = results
                                .filter { it.latitude != 0.0 && it.longitude != 0.0 }
                                .mapIndexed { index, place ->
                                    MapMarker(
                                        id = "place-${place.contentId}",
                                        position = MoyeoLatLng(place.latitude, place.longitude),
                                        shape = MapMarkerShape.Numbered,
                                        badge = "${index + 1}"
                                    )
                                }
                            // 좌표가 하나도 없으면 지도를 그리지 않는다
                            if (placeMarkers.isNotEmpty()) {
                                KakaoMapView(
                                    center = placeMarkers.first().position,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    markers = placeMarkers,
                                    fallback = { fallbackModifier -> MapUnavailablePlaceholder(fallbackModifier) }
                                )
                            }
                        }
                        Text(
                            // 개수는 서버 totalElements 다 — 화면에 그려진 페이지 크기가 아니다.
                            "${totalCount}곳",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (results.isEmpty()) {
                    item {
                        MoyeoEmptyState(
                            if (searchFailed) MoyeoEmptyText.FAILED else MoyeoEmptyText.NO_SEARCH_RESULTS,
                            testTag = "place-search-empty"
                        )
                    }
                }
                items(results, key = TourismPlace::contentId) { place ->
                    PlaceResultRow(
                        place = place,
                        added = place.contentId in addedIds,
                        onOpen = { onOpenDetail(draftId, place.contentId) },
                        onToggle = { toggleAdded(place) }
                    )
                }
                item {
                    Text(
                        "목록에는 제목·주소·썸네일·좌표만 표시해요. 전화번호와 소개는 상세에서 확인할 수 있어요.",
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceResultRow(place: TourismPlace, added: Boolean, onOpen: () -> Unit, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PlaceImage(Modifier.size(width = 76.dp, height = 68.dp), place)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    place.title,
                    Modifier.weight(1f),
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                PlaceTypeBadge(place.type, place.typeLabel)
            }
            Text(
                place.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            // 좌표는 적지 않는다 — 주소 줄이 바로 위에 있고 기획에도 없다.
            // 웹·iOS 도 목록에서 뺐는데 안드로이드만 `36.5732488352, 128.7683329532` 처럼
            // 소수점 열 자리를 그대로 보여주고 있었다(핀을 잡는 17-3·18-5·20-2c 만 예외다).
        }
        IconButton(
            onClick = onToggle,
            modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(Icons.Filled.Add, if (added) "코스에서 빼기" else "코스에 담기", tint = MaterialTheme.colorScheme.primary)
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .4f))
}

@Composable
fun PlaceDetailScreen(
    draftId: String,
    contentId: String,
    onBack: () -> Unit,
    onAdd: (String) -> Unit,
    repository: TourismContentRepository
) {
    var place by remember(contentId) { mutableStateOf(emptyTourismPlace(contentId)) }
    LaunchedEffect(repository, contentId) {
        val types = runCatching { repository.types() }.getOrElse { emptyList() }
        place = repository.content(contentId).toPlace().withServerTypeLabel(types)
    }
    var menuSelected by rememberSaveable { mutableStateOf(false) }
    val shareContext = LocalContext.current
    Scaffold(
        topBar = {
            ChangeLogTopBar("방문지 상세", onBack) {
                // 화면기획·웹의 우측 상단 공유
                IconButton(
                    onClick = {
                        // 아직 상세를 못 받았으면 보낼 내용이 없다 — 빈 공유 시트를 띄우지 않는다
                        if (place.title.isNotBlank()) {
                            shareText(
                                context = shareContext,
                                title = place.title,
                                body = listOf(place.address, place.description)
                                    .filter(String::isNotBlank)
                                    .joinToString("\n\n")
                            )
                        }
                    },
                    enabled = place.title.isNotBlank(),
                    modifier = Modifier.testTag("place-detail-share")
                ) {
                    Icon(Icons.Filled.Share, "방문지 공유", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        },
        bottomBar = {
            Surface(shadowElevation = 10.dp) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onBack, Modifier.height(48.dp), shape = RoundedCornerShape(12.dp)) { Text("목록으로") }
                    Button(
                        {
                            // 예전에는 화면만 되돌리고 초안은 그대로였다 — "담기" 를 눌러도
                            // 코스에 아무것도 늘지 않았다. 방문지 검색(담기/빼기)과 같은 규칙으로 넣는다.
                            val current = RecruitmentDraftStore.draft(draftId)
                            val stopId = "$draftId-place-${place.contentId}"
                            if (current.routeStops.none { it.id == stopId } &&
                                current.routeStops.size < RecruitmentDraftStore.MAX_ROUTE_STOPS
                            ) {
                                RecruitmentDraftStore.update(
                                    current.copy(
                                        routeStops = current.routeStops + RouteStop(
                                            id = stopId,
                                            day = current.dayCount,
                                            time = "",
                                            name = place.title,
                                            memo = place.address,
                                            latitude = place.latitude.takeIf { it != 0.0 },
                                            longitude = place.longitude.takeIf { it != 0.0 }
                                        )
                                    )
                                )
                            }
                            onAdd(draftId)
                        },
                        Modifier.weight(
                            1f
                        ).height(48.dp).testTag("place-detail-add"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Add, null)
                        Text("이 장소를 코스에 담기")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("place-detail-screen")) {
            item {
                Box {
                    PlaceImage(
                        Modifier.fillMaxWidth().aspectRatio(1.7f),
                        place,
                        fallbackShape = MoyeoPlaceholderShape.LANDSCAPE
                    )
                    // 화면기획·웹의 사진 카운터
                    // 사진 카운터는 서버가 준 사진 장수가 근거다 — 사진이 없으면 붙이지 않는다
                    if (place.photoUrls.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                            shape = RoundedCornerShape(50),
                            color = Color.Black.copy(alpha = 0.46f)
                        ) {
                            Text(
                                text = "1/${place.photoUrls.size}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    .testTag("place-detail-photo-counter"),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            item {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            place.title,
                            Modifier.weight(1f),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        PlaceTypeBadge(place.type, place.typeLabel)
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        // 서버가 주지 않는 값(우편번호·전화·홈페이지가 null)은 지어내지 않고 줄째로 숨긴다.
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (place.address.isNotBlank()) {
                                DetailInfo(
                                    Icons.Filled.LocationOn,
                                    place.address,
                                    place.postalCode.takeIf(String::isNotBlank)?.let { "우편번호 $it" }
                                )
                            }
                            if (place.phone.isNotBlank()) {
                                DetailInfo(
                                    Icons.Filled.Phone,
                                    place.phone,
                                    place.phoneLabel.takeIf(String::isNotBlank)
                                )
                            }
                            if (place.latitude != 0.0 || place.longitude != 0.0) {
                                // 좌표가 있을 때만 줄을 그리되 **보여주는 것은 동작 문구**다 —
                                // 웹·iOS 와 같은 처리. 예전에는 소수점 열 자리가 그대로 보였다.
                                DetailInfo(Icons.Filled.Map, "지도에서 열기", null)
                            }
                            if (place.homepage.isNotBlank()) {
                                DetailInfo(Icons.Filled.Share, place.homepage, "홈페이지")
                            }
                        }
                    }
                    // overview 가 null 이면 소개 자체를 숨긴다 — 대체 문구를 지어내지 않는다.
                    if (place.description.isNotBlank()) {
                        Text("소개", fontWeight = FontWeight.ExtraBold)
                        Text(place.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    // 서버 상세는 메뉴 이름을 주지 않는다 — 음식점 메뉴판은 menuImages 로 온다.
                    val menuCount = place.menuNames.size.takeIf { it > 0 } ?: place.menuImageUrls.size
                    val showsMenu = place.type == TourismContentType.Food && menuCount > 0
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PlaceTypeChip("사진 ${place.photoUrls.size}", !menuSelected || !showsMenu) {
                            menuSelected = false
                        }
                        if (showsMenu) {
                            PlaceTypeChip("메뉴판 $menuCount", menuSelected) { menuSelected = true }
                        }
                    }
                    if (menuSelected && showsMenu) {
                        place.menuNames.forEach { name ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(name, Modifier.fillMaxWidth().padding(16.dp), fontWeight = FontWeight.Bold)
                            }
                        }
                        if (place.menuNames.isEmpty()) {
                            PlacePhotoRow(place, place.menuImageUrls)
                        }
                    } else {
                        PlacePhotoRow(place, place.photoUrls)
                    }
                }
            }
        }
    }
}

internal enum class TermsDocument(
    val routeKey: String,
    val title: String,
    val required: Boolean,
    val version: String,
    val effectiveDate: String,
    val summary: String,
    val sections: List<Pair<String, String>>
) {
    Service(
        "service",
        "이용약관",
        true,
        "v1.2",
        "2026년 5월 1일 시행",
        "모여트립을 쓰면서 지켜야 할 것과, 우리가 약속하는 것을 적었어요.",
        listOf(
            "제1조 (목적)" to
                "이 약관은 모여트립 in 경북(이하 “서비스”)이 제공하는 경상북도 여행 코스 기반 동행 매칭 서비스의 이용 조건과 절차, 회사와 회원의 권리·의무를 정하는 것을 목적으로 합니다.",
            "제2조 (용어의 정의)" to
                "“모집”이란 회원이 특정 코스로 함께 떠날 동행을 구하는 게시물을 말합니다. “캠프”란 최소 인원이 충족되어 자동으로 열리는 채팅방을 말합니다. “호스트”란 모집을 개설한 회원을, “게스트”란 해당 모집에 참여한 회원을 말합니다.",
            "제3조 (모집의 성립과 소멸)" to
                "모집은 마감일까지 최소 인원(3명)을 충족한 경우에만 확정됩니다. 마감일 기준 인원이 미달한 모집은 자동으로 소멸하며, 이 경우 회원에게 별도의 책임이 발생하지 않습니다.",
            "제4조 (여행 중 발생하는 사항)" to
                "회사는 회원 간의 만남을 매개할 뿐, 여행 중 발생하는 이동·숙박·식음 등의 계약 당사자가 아닙니다. 여행 비용은 회원 간에 직접 정산하며, 서비스 내 정산 기능은 기록용 메모입니다.",
            "제5조 (금지 행위)" to
                "타인을 사칭하거나, 금전 거래를 유도하거나, 성적·차별적 표현으로 다른 회원에게 불쾌감을 주는 행위를 금지합니다. 위반 시 이용이 제한될 수 있습니다.",
            "제6조 (계정과 닉네임)" to
                "가입 시 선택한 닉네임과 캐릭터는 도감 기록의 동일성을 위해 변경되지 않습니다. 다만 회사는 서비스 정착 이후 정책을 변경할 수 있으며, 변경 시 사전에 공지합니다."
        )
    ),
    Privacy(
        "privacy",
        "개인정보 처리방침",
        true,
        "v1.4",
        "2026년 7월 10일 시행",
        "어떤 정보를 왜 받고, 얼마나 보관하는지 적었어요.",
        listOf(
            "수집하는 정보" to
                "가입 시 소셜 로그인 식별자, 생년(나이대), 성별을 받습니다. 실명·연락처는 받지 않습니다. 서비스 이용 과정에서 작성한 모집·채팅·피드 내용과 접속 기록이 저장됩니다.",
            "이용 목적" to
                "동행 매칭(나이대·성별 조건 확인), 안전한 이용 환경 유지(신고·차단 처리), 서비스 개선을 위한 통계 분석에 사용합니다.",
            "다른 회원에게 보이는 정보" to
                "닉네임·캐릭터·나이대·성별·매너 점수·여행 횟수가 공개됩니다. 생년월일과 로그인 계정 정보는 공개되지 않습니다.",
            "보관 기간" to
                "탈퇴 시 30일간 보관 후 완전히 삭제합니다. 다만 신고·분쟁 처리 이력은 관련 법령이 정한 기간 동안 별도 보관합니다.",
            "위탁 및 제3자 제공" to
                "지도·관광정보 표시를 위해 한국관광공사 OpenAPI 등 외부 서비스를 이용하며, 이 과정에서 회원의 개인정보를 전달하지 않습니다."
        )
    ),
    Location(
        "location",
        "위치정보 이용 동의",
        false,
        "v1.0",
        "2026년 5월 1일 시행",
        "켜지 않아도 서비스를 쓸 수 있어요. 켜면 근처 모집을 먼저 보여드려요.",
        listOf(
            "이용 목적" to
                "현재 위치 기준 30km 이내에서 오늘 출발하는 모집을 홈 상단에 보여주고, 집합 장소까지의 길 찾기를 제공합니다.",
            "동의를 거부할 경우" to
                "근처 모집 추천과 길 찾기만 제한되고, 그 외 기능은 모두 동일하게 이용할 수 있습니다."
        )
    ),
    Marketing(
        "marketing",
        "마케팅 정보 수신 동의",
        false,
        "v1.0",
        "2026년 5월 1일 시행",
        "새 코스와 계절 이벤트 소식을 받고 싶을 때만 선택해요.",
        listOf(
            "보내는 내용" to
                "계절별 신규 코스 소개, 지역 축제 일정, 이벤트 안내를 앱 푸시로 보냅니다.",
            "보내지 않는 것" to
                "모집 승인·채팅·마감 임박처럼 이용에 꼭 필요한 알림은 이 동의와 무관하게 발송됩니다.",
            "철회 방법" to
                "설정 › 알림에서 언제든 끌 수 있고, 끄더라도 서비스 이용에는 아무런 제한이 없습니다."
        )
    );

    companion object {
        fun fromRoute(value: String): TermsDocument = entries.firstOrNull { it.routeKey == value } ?: Service
    }
}

@Composable
fun TermsDetailScreen(
    documentKey: String,
    source: String,
    onBack: () -> Unit,
    onAgree: () -> Unit = onBack,
    /**
     * 약관 화면에서 넘겨준 서버 약관 ID. 있으면 이 ID 로 바로 본문을 받는다 —
     * 제목 키워드로 찾지 않는다. 서버가 약관을 새로 추가해도 키워드 표에 없다는 이유로
     * 본문을 못 여는 일이 없어야 한다.
     */
    serverTermId: Long? = null
) {
    val document = remember(documentKey) { TermsDocument.fromRoute(documentKey) }
    val context = LocalContext.current
    // 약관은 서버가 공개로 열어둔 엔드포인트라 로그인 전에도 받을 수 있다.
    // 가입 플로우(LocalServerData == null)에서도 본문을 보여줘야 한다.
    val termsRepository = remember(context) { AuthDependencies.appDefault(context).terms }
    var serverTerm by remember(documentKey, serverTermId) { mutableStateOf<TermDetail?>(null) }
    LaunchedEffect(documentKey, serverTermId) {
        serverTerm = runCatching {
            if (serverTermId != null) {
                termsRepository.term(serverTermId)
            } else {
                termsRepository.terms()
                    .firstOrNull { it.title.matchesTermsDocument(documentKey) }
                    ?.let { termsRepository.term(it.termId) }
            }
        }.getOrNull()
    }
    val isRequired = serverTerm?.required ?: document.required
    // 화면기획 08-C~08-G는 흰 페이지 위에 회색 채움 요약 박스다. 기본 배경(#F7F8F7)은
    // surfaceVariant와 같은 값이라 그대로 두면 요약 박스가 배경에 묻힌다.
    Scaffold(
        containerColor = MoyeoTheme.pageSurface,
        topBar = {
            val shareTitle = serverTerm?.displayTitle() ?: document.title
            ChangeLogTopBar(shareTitle, onBack) {
                // 화면기획의 우측 상단 공유. 웹과 같이 화면에 보이는 본문을 그대로 보낸다 —
                // 서버 약관을 받았으면 그 본문, 못 받았으면 앱에 내장한 조항들이다.
                IconButton(
                    onClick = {
                        shareText(
                            context = context,
                            title = shareTitle,
                            body = serverTerm?.content
                                ?: document.sections.joinToString("\n\n") { (heading, body) ->
                                    "## $heading\n\n$body"
                                }
                        )
                    },
                    modifier = Modifier.testTag("terms-detail-share")
                ) {
                    Icon(Icons.Filled.Share, "약관 공유", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        },
        bottomBar = if (source == "signup") {
            {
                Surface(shadowElevation = 8.dp) {
                    // 화면기획은 닫기 / 동의하고 돌아가기 두 버튼이다.
                    // 동의 버튼만 두면 "읽고 그냥 나가기" 경로가 헤더 뒤로만 남는다.
                    Row(
                        Modifier.fillMaxWidth().padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.width(84.dp).height(50.dp).testTag("terms-detail-close"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) { Text("닫기") }
                        Button(
                            onAgree,
                            Modifier.weight(1f).height(50.dp).testTag("terms-detail-agree"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isRequired) "동의하고 돌아가기" else "이 항목에 동의하기")
                        }
                    }
                }
            }
        } else {
            {}
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).testTag("terms-detail-${document.routeKey}"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isRequired) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    ) {
                        Text(
                            if (isRequired) "필수" else "선택",
                            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        serverTerm?.version ?: "${document.version} · ${document.effectiveDate}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            val term = serverTerm
            if (term != null) {
                items(term.content.toTermBlocks()) { block ->
                    when (block) {
                        is TermBlock.Heading -> Text(block.text, fontWeight = FontWeight.ExtraBold)

                        is TermBlock.Paragraph ->
                            Text(block.text, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        is TermBlock.Bullets -> Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            block.items.forEach { item ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(item, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        is TermBlock.Table -> TermTable(block)
                    }
                }
            } else {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MoyeoTheme.subtleSurface
                    ) {
                        Text(document.summary, Modifier.fillMaxWidth().padding(14.dp))
                    }
                }
                items(document.sections) { section ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(section.first, fontWeight = FontWeight.ExtraBold)
                        Text(section.second, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Text(
                        "이전 판본은 설정 › 이용약관 › 지난 버전에서 볼 수 있어요. " +
                            "약관이 바뀌면 시행 7일 전에 공지와 푸시로 알려드려요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** 서버 약관 제목의 "[필수] "/"[선택] " 접두는 배지가 대신 보여준다. */
private fun TermDetail.displayTitle(): String = title.removePrefix("[필수]").removePrefix("[선택]").trim()

/** 서버 약관 목록에서 앱 라우트 키에 해당하는 문서를 제목 키워드로 찾는다. */
private fun String.matchesTermsDocument(routeKey: String): Boolean = when (routeKey) {
    "service" -> contains("이용약관")
    "privacy" -> contains("개인정보")
    "location" -> contains("위치")
    "marketing" -> contains("마케팅")
    else -> false
}

/**
 * 서버 약관 본문(마크다운)의 렌더 단위.
 *
 * 이전에는 소제목과 문단만 구분해서, 개인정보 동의의 표가
 * `| 구분 | 수집 항목 |` 과 구분선 그대로 본문에 노출됐다.
 */
private sealed interface TermBlock {
    data class Heading(val text: String) : TermBlock

    data class Paragraph(val text: String) : TermBlock

    data class Bullets(val items: List<String>) : TermBlock

    data class Table(val header: List<String>, val rows: List<List<String>>) : TermBlock
}

private fun String.isTableSeparatorLine(): Boolean = startsWith("|") && contains("-") && all { it in " |-:" }

private fun String.toTableCells(): List<String> = trim('|').split("|").map(String::trim)

private fun String.toTermBlocks(): List<TermBlock> {
    val blocks = mutableListOf<TermBlock>()
    val bullets = mutableListOf<String>()
    val tableRows = mutableListOf<List<String>>()

    fun flushBullets() {
        if (bullets.isNotEmpty()) {
            blocks += TermBlock.Bullets(bullets.toList())
            bullets.clear()
        }
    }
    fun flushTable() {
        if (tableRows.isNotEmpty()) {
            blocks += TermBlock.Table(tableRows.first(), tableRows.drop(1))
            tableRows.clear()
        }
    }

    lineSequence().map(String::trim).forEach { line ->
        when {
            line.isBlank() -> {
                flushBullets()
                flushTable()
            }

            // `|---|---|` 는 그리지 않는다. 표가 이어지고 있다는 신호일 뿐이다.
            line.isTableSeparatorLine() -> Unit

            line.startsWith("|") -> {
                flushBullets()
                tableRows += line.toTableCells()
            }

            line.startsWith("## ") -> {
                flushBullets()
                flushTable()
                blocks += TermBlock.Heading(line.removePrefix("## ").trim())
            }

            // 문서 제목(#)은 상단 바가 이미 보여준다
            line.startsWith("#") -> {
                flushBullets()
                flushTable()
            }

            line.startsWith("- ") || line.startsWith("* ") -> {
                flushTable()
                bullets += line.drop(2).trim()
            }

            else -> {
                flushBullets()
                flushTable()
                blocks += TermBlock.Paragraph(line)
            }
        }
    }
    flushBullets()
    flushTable()
    return blocks
}

@Composable
private fun ChangeLogTopBar(title: String, onBack: () -> Unit, action: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로") }
        Text(title, Modifier.weight(1f), fontWeight = FontWeight.ExtraBold)
        if (action != null) action() else Spacer(Modifier.width(48.dp))
    }
}

@Composable
private fun PlaceTypeBadge(type: TourismContentType, label: String? = null) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(type.icon, null, Modifier.size(12.dp))
            Text(label ?: type.label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ScenicPlaceholder(modifier: Modifier, type: TourismContentType) {
    // 사진 자리에 카테고리 아이콘을 크게 띄우면 "사진이 있어야 하는 자리"가 아이콘 자리로 읽힌다.
    // 코스 카드와 같은 풍경 패널을 그린다.
    PlaceScenicPanel(
        kind = when (type) {
            TourismContentType.Spot -> PlaceScenicKind.Forest
            TourismContentType.Food -> PlaceScenicKind.Autumn
            TourismContentType.Stay -> PlaceScenicKind.Hanok
        },
        modifier = modifier
    )
}

/** 방문지 카테고리 칩. 선택 상태는 화면기획과 같이 외곽선 + 브랜드 틴트 + 진한 글자다. */
@Composable
private fun PlaceTypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val tints = MoyeoTheme.tints
    Surface(
        modifier = Modifier.height(30.dp).clip(CircleShape).clickable(onClick = onClick),
        shape = CircleShape,
        color = if (selected) tints.primaryTint else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Box(Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (selected) tints.onPrimaryTint else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PlaceImage(
    modifier: Modifier,
    place: TourismPlace,
    // 관광 데이터는 이미지가 간헐적으로 비어 있다. 자리 비율에 맞는 마스코트로 채운다.
    fallbackShape: MoyeoPlaceholderShape = MoyeoPlaceholderShape.SQUARE
) {
    CachedRemoteImage(
        url = place.thumbnailUrl ?: place.photoUrls.firstOrNull(),
        contentDescription = place.title,
        modifier = modifier.clip(RoundedCornerShape(10.dp)),
        contentScale = ContentScale.Crop,
        fallbackShape = fallbackShape
    ) {
        ScenicPlaceholder(modifier, place.type)
    }
}

/**
 * 사진·메뉴판 타일. 서버가 사진을 주지 않은 방문지는 줄째로 사라진다 —
 * 가짜 풍경으로 사진 자리를 채우지 않는다.
 */
@Composable
private fun PlacePhotoRow(place: TourismPlace, urls: List<String>) {
    if (urls.isEmpty()) return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val photos = urls.take(3)
        repeat(3) { index ->
            val photo = photos.getOrNull(index)
            if (photo == null) {
                // 서버가 준 장수만큼만 채우고 남는 칸은 비운다(3열 배치는 유지한다).
                Spacer(Modifier.weight(1f))
            } else {
                CachedRemoteImage(
                    url = photo,
                    contentDescription = null,
                    modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop,
                    fallbackShape = MoyeoPlaceholderShape.SQUARE
                ) {
                    ScenicPlaceholder(Modifier.weight(1f).aspectRatio(1f), place.type)
                }
            }
        }
    }
}

private fun TourismContentSummary.toPlace() = TourismPlace(
    contentId = contentId,
    type = TourismContentType.fromApiId(contentTypeId),
    contentTypeId = contentTypeId,
    title = title,
    address = listOfNotNull(address1, address2).joinToString(" ").ifBlank { "주소 정보 없음" },
    latitude = latitude ?: 0.0,
    longitude = longitude ?: 0.0,
    thumbnailUrl = thumbnailUrl,
    photoUrls = listOfNotNull(thumbnailUrl)
)

private fun TourismContentDetail.toPlace(): TourismPlace {
    val base = summary.toPlace()
    val photos = (contentImageUrls + listOfNotNull(summary.thumbnailUrl)).distinct()
    return base.copy(
        postalCode = zipcode.orEmpty(),
        phone = telephone.orEmpty(),
        phoneLabel = telephoneName.orEmpty(),
        homepage = homepage.orEmpty(),
        description = overview.orEmpty(),
        photoCount = photos.size,
        menuNames = menuNames,
        photoUrls = photos,
        menuImageUrls = menuImageUrls
    )
}

@Composable
private fun DetailInfo(icon: ImageVector, value: String, caption: String?) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column {
            Text(value, fontWeight = FontWeight.Bold)
            if (!caption.isNullOrBlank()) {
                Text(
                    caption,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 약관 본문의 표.
 *
 * 좁은 화면에서 글자를 줄이지 않고 **가로로 스크롤**한다 — 개인정보 동의 표가 4열이다.
 */
@Composable
private fun TermTable(table: TermBlock.Table) {
    val columnCount = maxOf(table.header.size, table.rows.maxOfOrNull { it.size } ?: 0)
    val border = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .testTag("term-table")
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, border, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
        ) {
            TermTableRow(table.header, columnCount, isHeader = true, border = border)
            table.rows.forEach { row ->
                HorizontalDivider(color = border)
                TermTableRow(row, columnCount, isHeader = false, border = border)
            }
        }
    }
}

@Composable
private fun TermTableRow(cells: List<String>, columnCount: Int, isHeader: Boolean, border: Color) {
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .background(
                if (isHeader) MoyeoTheme.subtleSurface else MaterialTheme.colorScheme.surface
            )
    ) {
        repeat(columnCount) { index ->
            Text(
                text = cells.getOrElse(index) { "" },
                modifier = Modifier.width(132.dp).padding(horizontal = 10.dp, vertical = 9.dp),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                color = if (isHeader) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            if (index < columnCount - 1) {
                Box(Modifier.width(1.dp).fillMaxHeight().background(border))
            }
        }
    }
}
