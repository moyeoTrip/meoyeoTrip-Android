package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kr.hanchae.moyeotrip.data.tourism.SampleTourismContentRepository
import kr.hanchae.moyeotrip.data.tourism.TourismContentDetail
import kr.hanchae.moyeotrip.data.tourism.TourismContentRepository
import kr.hanchae.moyeotrip.data.tourism.TourismContentSummary
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.KakaoMapView
import kr.hanchae.moyeotrip.ui.components.MapMarker
import kr.hanchae.moyeotrip.ui.components.MapMarkerShape
import kr.hanchae.moyeotrip.ui.components.MoyeoLatLng
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

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
    val menuImageUrls: List<String> = emptyList()
)

internal object TourismPlaceCatalog {
    val places = listOf(
        TourismPlace("2864117", TourismContentType.Spot, "주왕산국립공원", "경상북도 청송군 부동면 공원길 226", 36.3931, 129.1728),
        TourismPlace("2871004", TourismContentType.Spot, "주산지", "경상북도 청송군 부동면 주산지길 259", 36.3494, 129.1436),
        TourismPlace(
            "2299341",
            TourismContentType.Food,
            "달기약수터 백숙거리",
            "경상북도 청송군 청송읍 약수길 5",
            36.427812,
            129.048915,
            postalCode = "37411",
            phone = "054-873-7777",
            phoneLabel = "달기약수터 관리사무소",
            homepage = "cheongsong.go.kr/tour",
            description = "탄산이 섞인 달기약수로 끓여내는 백숙이 유명한 거리예요. 산행 뒤 늦은 점심 자리로 많이 찾으며 방문 전 예약 여부를 확인하는 편이 좋아요.",
            photoCount = 8,
            menuNames = listOf("닭백숙 정식", "오리 백숙", "한방 삼계탕", "더덕구이")
        ),
        TourismPlace("2740882", TourismContentType.Stay, "청송 솔기온천 한옥스테이", "경상북도 청송군 청송읍 금월로 273", 36.4361, 129.0573),
        TourismPlace("2510773", TourismContentType.Spot, "청송 객주문학관", "경상북도 청송군 진보면 청송로 6359", 36.4739, 129.0093)
    )

    fun filtered(query: String, type: TourismContentType?): List<TourismPlace> = places.filter { place ->
        (type == null || place.type == type) &&
            (query.isBlank() || place.title.contains(query, true) || place.address.contains(query, true))
    }

    fun find(contentId: String): TourismPlace = places.firstOrNull { it.contentId == contentId } ?: places[2]
}

@Composable
fun PlaceSearchScreen(
    draftId: String,
    onBack: () -> Unit,
    onOpenDetail: (String, String) -> Unit,
    onDone: (String) -> Unit,
    repository: TourismContentRepository = SampleTourismContentRepository
) {
    var query by rememberSaveable { mutableStateOf("청송") }
    var selectedType by rememberSaveable { mutableStateOf<TourismContentType?>(null) }
    var addedIds by rememberSaveable { mutableStateOf(setOf("2864117", "2871004", "2299341")) }
    var showsMap by rememberSaveable { mutableStateOf(false) }
    var places by remember { mutableStateOf(TourismPlaceCatalog.places) }
    LaunchedEffect(repository, selectedType) {
        places = repository.contents(selectedType?.apiId).items.map(TourismContentSummary::toPlace)
    }
    val results = places.filter { place ->
        query.isBlank() || place.title.contains(query, true) || place.address.contains(query, true)
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
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).testTag("place-search-screen"),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            item {
                Column(
                    Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
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
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PlaceTypeChip("전체", selectedType == null) { selectedType = null }
                        TourismContentType.entries.forEach { type ->
                            PlaceTypeChip(type.label, selectedType == type) { selectedType = type }
                        }
                    }
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
                        KakaoMapView(
                            center = placeMarkers.firstOrNull()?.position ?: MoyeoLatLng(36.4361, 129.0573),
                            modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)),
                            markers = placeMarkers,
                            fallback = { fallbackModifier ->
                                PlaceScenicPanel(
                                    kind = PlaceScenicKind.Forest,
                                    modifier = fallbackModifier,
                                    cornerRadius = 12.dp
                                )
                            }
                        )
                    }
                    Text(
                        "${results.size}곳 · 주왕산 코스 근처순",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(results, key = TourismPlace::contentId) { place ->
                PlaceResultRow(
                    place = place,
                    added = place.contentId in addedIds,
                    onOpen = { onOpenDetail(draftId, place.contentId) },
                    onToggle = {
                        addedIds =
                            if (place.contentId in addedIds) addedIds - place.contentId else addedIds + place.contentId
                    }
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
                PlaceTypeBadge(place.type)
            }
            Text(
                place.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            Text(
                "${place.latitude}, ${place.longitude}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    repository: TourismContentRepository = SampleTourismContentRepository
) {
    var place by remember(contentId) { mutableStateOf(TourismPlaceCatalog.find(contentId)) }
    LaunchedEffect(repository, contentId) {
        place = repository.content(contentId).toPlace()
    }
    var menuSelected by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        topBar = {
            ChangeLogTopBar("방문지 상세", onBack) {
                // 화면기획·웹의 우측 상단 공유
                IconButton(onClick = {}, modifier = Modifier.testTag("place-detail-share")) {
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
                    PlaceImage(Modifier.fillMaxWidth().aspectRatio(1.7f), place)
                    // 화면기획·웹의 사진 카운터
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                        shape = RoundedCornerShape(50),
                        color = Color.Black.copy(alpha = 0.46f)
                    ) {
                        Text(
                            text = "1/${maxOf(place.photoCount, 1)}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                .testTag("place-detail-photo-counter"),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
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
                        PlaceTypeBadge(place.type)
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            DetailInfo(
                                Icons.Filled.LocationOn,
                                place.address,
                                "우편번호 ${place.postalCode.ifBlank {
                                    "37411"
                                }}"
                            )
                            DetailInfo(
                                Icons.Filled.Phone,
                                place.phone.ifBlank {
                                    "054-873-7777"
                                },
                                place.phoneLabel.ifBlank { "관광 안내" }
                            )
                            DetailInfo(Icons.Filled.Map, "${place.latitude}, ${place.longitude}", "지도에서 열기")
                            DetailInfo(Icons.Filled.Share, place.homepage.ifBlank { "visitkorea.or.kr" }, "홈페이지")
                        }
                    }
                    Text("소개", fontWeight = FontWeight.ExtraBold)
                    Text(
                        place.description.ifBlank {
                            "경북의 풍경과 이야기를 천천히 즐길 수 있는 방문지예요."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PlaceTypeChip("사진 ${place.photoCount.coerceAtLeast(6)}", !menuSelected) {
                            menuSelected = false
                        }
                        if (place.type == TourismContentType.Food) {
                            PlaceTypeChip("메뉴판 ${place.menuNames.size}", menuSelected) { menuSelected = true }
                        }
                    }
                    if (menuSelected) {
                        place.menuNames.forEach { name ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(name, Modifier.fillMaxWidth().padding(16.dp), fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val photos = place.photoUrls.take(3)
                            repeat(3) { index ->
                                val photo = photos.getOrNull(index)
                                CachedRemoteImage(
                                    url = photo,
                                    contentDescription = null,
                                    modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                ) {
                                    ScenicPlaceholder(Modifier.weight(1f).aspectRatio(1f), place.type)
                                }
                            }
                        }
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
fun TermsDetailScreen(documentKey: String, source: String, onBack: () -> Unit, onAgree: () -> Unit = onBack) {
    val document = remember(documentKey) { TermsDocument.fromRoute(documentKey) }
    // 화면기획 08-C~08-G는 흰 페이지 위에 회색 채움 요약 박스다. 기본 배경(#F7F8F7)은
    // surfaceVariant와 같은 값이라 그대로 두면 요약 박스가 배경에 묻힌다.
    Scaffold(
        containerColor = MoyeoTheme.pageSurface,
        topBar = {
            ChangeLogTopBar(document.title, onBack) {
                // 화면기획의 우측 상단 공유
                IconButton(onClick = {}, modifier = Modifier.testTag("terms-detail-share")) {
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
                            Text(if (document.required) "동의하고 돌아가기" else "이 항목에 동의하기")
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
                        color = if (document.required) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    ) {
                        Text(
                            if (document.required) "필수" else "선택",
                            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        "${document.version} · ${document.effectiveDate}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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
private fun PlaceTypeBadge(type: TourismContentType) {
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
            Text(type.label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
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
private fun PlaceImage(modifier: Modifier, place: TourismPlace) {
    CachedRemoteImage(
        url = place.thumbnailUrl ?: place.photoUrls.firstOrNull(),
        contentDescription = place.title,
        modifier = modifier.clip(RoundedCornerShape(10.dp)),
        contentScale = ContentScale.Crop
    ) {
        ScenicPlaceholder(modifier, place.type)
    }
}

private fun TourismContentSummary.toPlace() = TourismPlace(
    contentId = contentId,
    type = TourismContentType.fromApiId(contentTypeId),
    title = title,
    address = listOfNotNull(address1, address2).joinToString(" ").ifBlank { "주소 정보 없음" },
    latitude = latitude ?: 0.0,
    longitude = longitude ?: 0.0,
    thumbnailUrl = firstThumbnailUrl ?: firstImageUrl,
    photoUrls = listOfNotNull(firstImageUrl).distinct()
)

private fun TourismContentDetail.toPlace(): TourismPlace {
    val base = summary.toPlace()
    return base.copy(
        postalCode = zipcode.orEmpty(),
        phone = telephone.orEmpty(),
        phoneLabel = telephoneName.orEmpty(),
        homepage = homepage.orEmpty(),
        description = overview.orEmpty(),
        photoCount = contentImageUrls.size,
        menuNames = menuNames,
        photoUrls = (contentImageUrls + listOfNotNull(summary.firstImageUrl)).distinct(),
        menuImageUrls = menuImageUrls
    )
}

@Composable
private fun DetailInfo(icon: ImageVector, value: String, caption: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column {
            Text(value, fontWeight = FontWeight.Bold)
            Text(
                caption,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
