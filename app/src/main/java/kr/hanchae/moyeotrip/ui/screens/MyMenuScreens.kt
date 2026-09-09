package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.BuildConfig
import kr.hanchae.moyeotrip.R
import kr.hanchae.moyeotrip.data.auth.AuthAccountService
import kr.hanchae.moyeotrip.data.feed.FeedTab
import kr.hanchae.moyeotrip.data.feed.ServerFeed
import kr.hanchae.moyeotrip.data.profile.ProfileOptions
import kr.hanchae.moyeotrip.data.profile.ProfileUpdate
import kr.hanchae.moyeotrip.data.profile.ServerUserProfile
import kr.hanchae.moyeotrip.data.settings.ThemePreference
import kr.hanchae.moyeotrip.data.social.DexCompanion
import kr.hanchae.moyeotrip.domain.auth.AuthProvider
import kr.hanchae.moyeotrip.domain.auth.EmailAuthRequest
import kr.hanchae.moyeotrip.domain.auth.UserDisplayProfile
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.MoyeoContact
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyState
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyText
import kr.hanchae.moyeotrip.ui.components.MoyeoNicknameAnimal
import kr.hanchae.moyeotrip.ui.components.MoyeoPlaceholderShape
import kr.hanchae.moyeotrip.ui.components.ServerListState
import kr.hanchae.moyeotrip.ui.components.moyeoRelativeTime
import kr.hanchae.moyeotrip.ui.theme.Coral
import kr.hanchae.moyeotrip.ui.theme.ForestGreen
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

/** 27 친구 도감 카드 한 장. 서버 도감(GET users/me/travel-dex) 응답을 화면 모양으로 옮긴 값이다. */
internal data class DexFriend(
    val id: String,
    val nickname: String,
    /** 닉네임 동물 이모지 — `profileImageUrl` 이 없거나 못 불러올 때만 쓰는 폴백이다(R5). */
    val avatar: String,
    /** 서버 도감이 주는 실제 프로필 이미지. 있으면 **반드시** 이 이미지를 그린다. */
    val profileImageUrl: String?,
    val lastMetAt: String,
    /** 최근 동행일 원본(`yyyy-MM-dd`) — `최근 1개월` 필터의 근거다. */
    val lastMetDate: String,
    val metCount: Int
)

/**
 * 27 도감 필터 칩. iOS 와 같은 세 갈래다 — 도감 응답이 주는 값만으로 가른다.
 *
 * `최근 1개월` 은 최근 동행일이 오늘로부터 한 달 안인 친구다. 날짜를 못 읽으면 **포함하지 않는다**
 * (읽지 못한 값을 "최근"으로 세면 숫자를 지어내는 셈이다).
 */
internal enum class DexFilter(val label: String) {
    All("전체"),
    Repeat("2회 이상"),
    Recent("최근 1개월");

    fun matches(friend: DexFriend): Boolean = when (this) {
        All -> true

        Repeat -> friend.metCount >= 2

        Recent -> runCatching { java.time.LocalDate.parse(friend.lastMetDate) }
            .getOrNull()
            ?.isAfter(java.time.LocalDate.now().minusMonths(1)) == true
    }
}

@Composable
fun ProfileEditScreen(
    userProfile: UserDisplayProfile,
    onBack: () -> Unit,
    // 캡처용 — true면 28-1 여행 취향 편집 시트가 열린 채 시작한다
    showTasteSheetInitially: Boolean = false
) {
    var selectedStyles by remember { mutableStateOf(emptySet<String>()) }
    // 서버 프로필을 받으면 아래 LaunchedEffect 가 덮어쓴다. 받기 전에는 비워 둔다 —
    // 사용자가 고르지 않은 취향이 잠깐이라도 본인 것처럼 보이면 안 된다.
    var selectedRegions by remember { mutableStateOf(emptySet<String>()) }
    var showTasteSheet by remember { mutableStateOf(showTasteSheetInitially) }

    /**
     * 28-1 의 세 줄(자기소개·생년월일·성별)을 실제로 고치는 시트.
     * **예전에는 `>` 만 그려 놓고 누를 수 없었다** — iOS 는 이미 이 시트를 갖고 있었고
     * 웹·기획은 화살표를 그리지 않는다. 안드로이드만 표시와 동작이 어긋나 있었다.
     */
    var editingField by remember { mutableStateOf<ProfileEditField?>(null) }
    var showSavedDialog by remember { mutableStateOf(false) }
    var saveErrorMessage by remember { mutableStateOf<String?>(null) }
    val colors = MaterialTheme.colorScheme
    val tints = MoyeoTheme.tints

    // 로그인 상태면 실서버 프로필·취향 후보(GET users/me/profile · profile/options)로 대체한다.
    // 취향 후보가 기획 목록과 다르면 서버 값을 쓴다.
    val server = LocalServerData.current
    var serverProfile by remember(server) { mutableStateOf<ServerUserProfile?>(null) }
    var serverOptions by remember(server) { mutableStateOf<ProfileOptions?>(null) }
    val saveScope = rememberCoroutineScope()
    LaunchedEffect(server) {
        if (server == null) {
            serverProfile = null
            serverOptions = null
            return@LaunchedEffect
        }
        val loaded = runCatching { server.userProfile.profile() }.getOrNull() ?: return@LaunchedEffect
        serverProfile = loaded
        selectedStyles = loaded.travelStyles.map { it.label }.toSet()
        selectedRegions = loaded.interestedRegions.map { it.label }.toSet()
        serverOptions = runCatching { server.userProfile.options() }.getOrNull()
    }
    // 28 취향 후보는 서버(GET users/me/profile/options)가 정본이다 — 못 받으면 고를 후보가 없다.
    // 앱이 들고 있는 목록으로 채우면 저장(PUT)에 필요한 id 가 없어 고를 수는 있지만 저장되지 않는다.
    val styleOptions = serverOptions?.travelStyles?.map { it.label }.orEmpty()
    val regionOptions = serverOptions?.interestedRegions?.map { it.label }.orEmpty()

    fun saveToServer(styles: Set<String>, regions: Set<String>, onSaved: () -> Unit) {
        val current = serverProfile
        val options = serverOptions
        if (server == null || current == null || options == null) {
            onSaved()
            return
        }
        val update = ProfileUpdate(
            introduction = current.introduction,
            travelStyleIds = options.travelStyles.filter { it.label in styles }.map { it.id },
            interestedRegionIds = options.interestedRegions.filter { it.label in regions }.map { it.id },
            birthDate = current.birthDate.orEmpty(),
            gender = current.gender.ifBlank { "N" }
        )
        saveScope.launch {
            runCatching { server.userProfile.updateProfile(update) }
                .onSuccess { saved ->
                    serverProfile = saved
                    selectedStyles = saved.travelStyles.map { it.label }.toSet()
                    selectedRegions = saved.interestedRegions.map { it.label }.toSet()
                    onSaved()
                }
                .onFailure { error ->
                    saveErrorMessage = error.message ?: "프로필 저장에 실패했어요."
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .testTag("screen-profile-edit")
    ) {
        CompactMenuHeader(
            title = "프로필 수정",
            onBack = onBack,
            containerColor = MoyeoTheme.cardSurface,
            trailing = {
                TextButton(
                    onClick = {
                        if (serverProfile != null) {
                            saveToServer(selectedStyles, selectedRegions) { showSavedDialog = true }
                        } else {
                            showSavedDialog = true
                        }
                    },
                    modifier = Modifier.testTag("profile-edit-save")
                ) {
                    Text(text = "저장", fontWeight = FontWeight.ExtraBold)
                }
            }
        )

        // 화면기획 구조: 중앙 아바타(고정 배지) → 공개 프로필 그룹 → 비공개 정보 그룹.
        // 항목마다 카드를 두면 무엇이 공개/비공개인지 묶음이 읽히지 않는다.
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 42.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MoyeoTheme.cardSurface)
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box {
                        // 화면기획 28의 아바타 배경은 연초록(primary100)이다 — 기본 코랄이 아니다
                        // **올린 프로필 사진을 그린다.** 예전에는 `AnimalAvatar` 만 그려서
                        // 사진을 올린 사람에게도 동물 이모지가 보였다 — 다른 화면(피드 카드 등)은
                        // 이미 `UserAvatar` 로 사진을 그리고 있어 같은 사람이 화면마다 달라 보였다.
                        // 사진이 없으면 `UserAvatar` 가 닉네임의 동물로 대신한다 (R5 규칙 그대로).
                        //
                        // 자물쇠 배지는 두지 않는다 — 고칠 수 없다는 사실은 아래 비공개 정보의
                        // `닉네임`·`캐릭터` 줄에 잠금 표시로 이미 있다.
                        UserAvatar(
                            imageUrl = serverProfile?.profileImageUrl,
                            nickname = serverProfile?.nickname ?: userProfile.nickname,
                            modifier = Modifier.size(96.dp),
                            fallbackFontSize = 44.sp
                        )
                    }
                    Text(
                        serverProfile?.nickname ?: userProfile.nickname.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    // "바꿀 수 없어요" 경고를 두지 않는다 — 아래 비공개 정보의
                    // `닉네임`·`캐릭터` 줄에 잠금 표시로 이미 있다 (기획 28 과 같다).
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
            item { ProfileEditGroupHeader("공개 프로필") }
            item {
                ProfileEditRow(
                    label = "자기소개",
                    value = serverProfile?.introduction.orEmpty(),
                    showsChevron = true,
                    onClick = { editingField = ProfileEditField.Introduction }
                )
            }
            item {
                // changeLog13 — 여행 스타일 행 + 관심 지역 블록을 여행 취향 통합 블록 하나로 합쳤다.
                // 블록 안 인라인 편집은 없다 — 어디를 탭해도 28-1 편집 시트가 열린다.
                ProfileEditTasteBlock(
                    selectedStyles = styleOptions.filter { it in selectedStyles },
                    selectedRegions = regionOptions.filter { it in selectedRegions },
                    onClick = { showTasteSheet = true }
                )
            }
            item { ProfileEditGroupHeader("비공개 정보") }
            item {
                // 닉네임과 캐릭터는 선택 후 바꿀 수 없다 — 잠금 표시로 알린다
                ProfileEditRow(
                    label = "닉네임",
                    value = serverProfile?.nickname ?: userProfile.nickname.orEmpty(),
                    locked = true
                )
            }
            item { ProfileEditRow(label = "캐릭터", value = "고정됨", locked = true) }
            item {
                ProfileEditRow(
                    label = "생년월일",
                    value = serverProfile?.birthDate?.replace('-', '.').orEmpty(),
                    showsChevron = true,
                    onClick = { editingField = ProfileEditField.BirthDate }
                )
            }
            item {
                ProfileEditRow(
                    label = "성별",
                    value = serverProfile?.gender?.genderLabel().orEmpty(),
                    showsChevron = true,
                    onClick = { editingField = ProfileEditField.Gender }
                )
            }
            item {
                Text(
                    "비공개 정보는 다른 여행자에게 보이지 않아요.",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }

    if (showSavedDialog) {
        AlertDialog(
            onDismissRequest = { showSavedDialog = false },
            title = { Text(text = "저장했어요", fontWeight = FontWeight.ExtraBold) },
            text = {
                Text(text = "프로필 변경사항이 저장됐어요.")
            },
            confirmButton = {
                TextButton(onClick = { showSavedDialog = false }) {
                    Text(text = "확인", fontWeight = FontWeight.ExtraBold)
                }
            }
        )
    }

    if (saveErrorMessage != null) {
        AlertDialog(
            onDismissRequest = { saveErrorMessage = null },
            title = { Text(text = "저장하지 못했어요", fontWeight = FontWeight.ExtraBold) },
            text = { Text(text = saveErrorMessage.orEmpty()) },
            confirmButton = {
                TextButton(onClick = { saveErrorMessage = null }) {
                    Text(text = "확인", fontWeight = FontWeight.ExtraBold)
                }
            }
        )
    }

    editingField?.let { field ->
        ProfileFieldEditSheet(
            field = field,
            current = serverProfile,
            onDismiss = { editingField = null },
            onSave = { introduction, birthDate, gender ->
                editingField = null
                val current = serverProfile ?: return@ProfileFieldEditSheet
                val options = serverOptions ?: return@ProfileFieldEditSheet
                // 저장은 취향 시트와 같은 한 번의 PUT users/me/profile 이다.
                val update = ProfileUpdate(
                    introduction = introduction,
                    travelStyleIds = options.travelStyles.filter { it.label in selectedStyles }.map { it.id },
                    interestedRegionIds = options.interestedRegions
                        .filter { it.label in selectedRegions }
                        .map { it.id },
                    birthDate = birthDate,
                    gender = gender
                )
                val target = server ?: return@ProfileFieldEditSheet
                saveScope.launch {
                    runCatching { target.userProfile.updateProfile(update) }
                        .onSuccess { serverProfile = it }
                        .onFailure { saveErrorMessage = it.message ?: "프로필 저장에 실패했어요." }
                }
            }
        )
    }

    if (showTasteSheet) {
        TasteEditSheet(
            initialStyles = selectedStyles,
            initialRegions = selectedRegions,
            styleOptions = styleOptions,
            regionOptions = regionOptions,
            onDismiss = { showTasteSheet = false },
            onSave = { styles, regions ->
                selectedStyles = styles
                selectedRegions = regions
                showTasteSheet = false
                // 28-1 시트 저장은 PUT users/me/profile 로 즉시 반영한다
                if (serverProfile != null) {
                    saveToServer(styles, regions) {}
                }
            }
        )
    }
}

/**
 * 28-1 에서 고칠 수 있는 세 줄. **화살표만 있고 못 고치던 자리**다(iOS `ProfileEditField` 와 같다).
 * 닉네임·캐릭터는 정한 뒤 바꿀 수 없으므로 여기 없다.
 */
private enum class ProfileEditField(val title: String) {
    Introduction("자기소개"),
    BirthDate("생년월일"),
    Gender("성별")
}

/**
 * 세 줄 중 하나를 고치는 시트. 저장은 취향 시트와 같은 **한 번의 `PUT users/me/profile`** 이라
 * 고치지 않는 값은 서버가 준 현재 값을 그대로 되돌려 보낸다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileFieldEditSheet(
    field: ProfileEditField,
    current: ServerUserProfile?,
    onDismiss: () -> Unit,
    onSave: (introduction: String?, birthDate: String, gender: String) -> Unit
) {
    var introduction by remember(field) { mutableStateOf(current?.introduction.orEmpty()) }
    var birthDate by remember(field) { mutableStateOf(current?.birthDate.orEmpty()) }
    var gender by remember(field) { mutableStateOf(current?.gender.orEmpty().ifBlank { "N" }) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(field.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            when (field) {
                ProfileEditField.Introduction -> OutlinedTextField(
                    value = introduction,
                    onValueChange = { introduction = it },
                    placeholder = { Text("어떤 여행을 좋아하는지 적어주세요") },
                    modifier = Modifier.fillMaxWidth().testTag("profile-edit-introduction")
                )

                ProfileEditField.BirthDate -> OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    placeholder = { Text("1998-04-12") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile-edit-birth-date")
                )

                ProfileEditField.Gender -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 서버가 받는 값은 F·M·N 세 가지다 — 화면 문구는 `genderLabel()` 과 같은 표를 쓴다.
                    listOf("F", "M", "N").forEach { code ->
                        val selected = gender == code
                        // 모서리·높이를 직접 준다 — Material3 기본은 알약 + 40dp 라
                        // 기획·웹의 12dp 둥근 사각형 + 44dp 와 달라 보인다 (버튼 전수조사 2026-09-09).
                        if (selected) {
                            Button(
                                onClick = { gender = code },
                                modifier = Modifier.weight(1f).height(44.dp).testTag("profile-edit-gender-$code"),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text(code.genderLabel()) }
                        } else {
                            OutlinedButton(
                                onClick = { gender = code },
                                modifier = Modifier.weight(1f).height(44.dp).testTag("profile-edit-gender-$code"),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text(code.genderLabel()) }
                        }
                    }
                }
            }
            Button(
                onClick = { onSave(introduction.ifBlank { null }, birthDate, gender) },
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("profile-edit-save"),
                shape = RoundedCornerShape(12.dp)
            ) { Text("저장") }
        }
    }
}

private fun String.genderLabel(): String = when (this) {
    "F" -> "여성"

    "M" -> "남성"

    // 가입(06)에서 고르는 이름이 「선택 안 함」이다 — 프로필에서 「비공개」로 바꿔 부르면
    // 같은 값을 두 이름으로 부르게 된다. 웹·iOS 도 「선택 안 함」이다 (28, 2026-09-09).
    else -> "선택 안 함"
}

/** 28의 여행 취향 통합 블록 — 조회와 수정 진입이 한 자리에서 일어난다 (changeLog13). */
@Composable
private fun ProfileEditTasteBlock(selectedStyles: List<String>, selectedRegions: List<String>, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MoyeoTheme.cardSurface)
            .testTag("profile-edit-taste")
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("여행 취향", fontWeight = FontWeight.Bold)
            Text(
                "탭해서 바로 수정",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant
            )
        }
        Text(
            "여행 스타일",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant
        )
        TasteSummaryChipRow(labels = selectedStyles)
        Text(
            "관심 지역",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant
        )
        TasteSummaryChipRow(labels = selectedRegions)
    }
}

/** 선택된 취향 칩 줄 + `+ 추가` 칩. 칩 자체는 탭 대상이 아니다 — 블록 전체가 시트를 연다. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TasteSummaryChipRow(labels: List<String>) {
    val colors = MaterialTheme.colorScheme
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        labels.forEach { label ->
            Surface(
                shape = RoundedCornerShape(50),
                color = colors.primary
            ) {
                Text(
                    label,
                    Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onPrimary
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = colors.surfaceVariant,
            border = BorderStroke(1.dp, colors.outline)
        ) {
            Text(
                "+ 추가",
                Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** 28-1 · 여행 취향 편집 Bottom Sheet — 후보·기본값·"각 1개 이상" 규칙은 06-1과 동일. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TasteEditSheet(
    initialStyles: Set<String>,
    initialRegions: Set<String>,
    onDismiss: () -> Unit,
    onSave: (Set<String>, Set<String>) -> Unit,
    // 로그인 상태면 서버 후보(GET users/me/profile/options)가 들어온다
    styleOptions: List<String>,
    regionOptions: List<String>
) {
    var draftStyles by remember { mutableStateOf(initialStyles) }
    var draftRegions by remember { mutableStateOf(initialRegions) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MoyeoTheme.sheetSurface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp)
                .testTag("taste-edit-sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "여행 취향 편집",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "여행 스타일과 관심 지역은 함께 저장돼요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TasteRequirementLabel(title = "여행 스타일", requirement = "1개 이상")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    styleOptions.forEach { label ->
                        TasteChip(
                            label = label,
                            selected = label in draftStyles,
                            tag = "taste-edit-style-$label",
                            contentDescription = "여행 스타일 $label 선택",
                            onClick = {
                                draftStyles = if (label in draftStyles) draftStyles - label else draftStyles + label
                            }
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TasteRequirementLabel(title = "관심 지역", requirement = "경북 안에서 1곳 이상")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    regionOptions.forEach { label ->
                        TasteChip(
                            label = label,
                            selected = label in draftRegions,
                            tag = "taste-edit-region-$label",
                            contentDescription = "관심 지역 $label 선택",
                            onClick = {
                                draftRegions = if (label in draftRegions) draftRegions - label else draftRegions + label
                            }
                        )
                    }
                }
            }
            TasteSelectionCaption(styleCount = draftStyles.size, regionCount = draftRegions.size)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("taste-edit-cancel")
                ) {
                    Text("취소", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onSave(draftStyles, draftRegions) },
                    enabled = draftStyles.isNotEmpty() && draftRegions.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("taste-edit-save"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("저장", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

/** 프로필 수정의 그룹 제목. 공개/비공개 묶음을 구분한다. */
@Composable
private fun ProfileEditGroupHeader(title: String) {
    // 회색 밴드는 페이지 배경(bgSubtle)이 그대로 드러난 자리다 — 행 그룹만 흰 표면을 깐다
    Text(
        title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold
    )
}

/** 라벨 좌측 · 값 우측의 한 줄 행. 카드로 감싸지 않는다 (화면기획). */
@Composable
private fun ProfileEditRow(
    label: String,
    value: String,
    showsChevron: Boolean = false,
    locked: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = Modifier.background(MoyeoTheme.cardSurface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // `>` 를 그리면 누를 수 있어야 한다. 잠긴 줄(닉네임·캐릭터)은 화살표도 없다.
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 라벨이 `weight(1f)` 이면 **값이 길 때 라벨이 눌려 줄바꿈된다** —
            // 웹에서 자기소개가 길어지자 라벨이 「자/기/소/개」로 쪼개진 것을 봤다.
            // 라벨은 제 폭을 지키고, 남은 폭을 값이 받아 줄임표로 줄인다.
            Text(label, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(
                value,
                modifier = Modifier.weight(1f).padding(start = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (locked) colors.onSurfaceVariant else colors.onSurface,
                fontWeight = if (locked) FontWeight.Normal else FontWeight.Bold,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = if (locked) Icons.Filled.Check else Icons.Filled.ChevronRight,
                contentDescription = null,
                modifier = Modifier.padding(start = 6.dp).size(16.dp),
                tint = colors.onSurfaceVariant
            )
        }
        HorizontalDivider(color = colors.outlineVariant)
    }
}

/**
 * 26-1 내 피드 — 내가 쓴 글만 보여주는 API 가 아직 없다.
 * 그래서 발견 탭(GET feeds?tab=DISCOVER)에서 **내 userId 글만** 골라 그린다.
 * 내 프로필을 못 읽으면 고를 근거가 없어 빈 상태다.
 */
@Composable
fun MyFeedScreen(onBack: () -> Unit, onOpenPost: (String) -> Unit) {
    val server = LocalServerData.current
    var feeds by remember(server) { mutableStateOf<ServerListState<ServerFeed>>(ServerListState.Loading) }
    var reloadKey by remember(server) { mutableIntStateOf(0) }
    LaunchedEffect(server, reloadKey) {
        if (server == null) {
            feeds = ServerListState.Loaded(emptyList())
            return@LaunchedEffect
        }
        val myNickname = runCatching { server.userProfile.profile().nickname }.getOrNull()
        feeds = runCatching { server.feeds.feeds(FeedTab.DISCOVER).feeds }
            .fold(
                { loaded -> ServerListState.Loaded(loaded.filter { it.author.nickname == myNickname }) },
                { ServerListState.Failed }
            )
    }
    val loaded = (feeds as? ServerListState.Loaded)?.items.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("screen-my-feed")
    ) {
        CompactMenuHeader(
            title = "내 피드",
            onBack = onBack,
            trailing = {
                ProfilePill(text = "${loaded.size}", tint = ForestGreen)
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = menuContentPadding(bottom = 34.dp)
        ) {
            val state = feeds
            when {
                state is ServerListState.Loading -> item { MoyeoEmptyState(MoyeoEmptyText.LOADING) }

                state is ServerListState.Failed -> item {
                    MoyeoEmptyState(MoyeoEmptyText.FAILED, onRetry = { reloadKey++ })
                }

                loaded.isEmpty() -> item {
                    // 남의 피드가 없는 것과 다른 상황이다 — 내가 쓰면 채워진다.
                    MoyeoEmptyState(MoyeoEmptyText.NO_MY_FEEDS, testTag = "my-feed-empty")
                }

                else -> items(items = loaded, key = { it.feedId }) { feed ->
                    MyFeedPostCard(feed = feed, onClick = { onOpenPost("srv-${feed.feedId}") })
                }
            }
        }
    }
}

@Composable
private fun MyFeedPostCard(feed: ServerFeed, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("my-feed-post-${feed.feedId}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UserAvatar(
                    imageUrl = feed.author.profileImageUrl,
                    nickname = feed.author.nickname,
                    modifier = Modifier.size(42.dp),
                    fallbackFontSize = 19.sp
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = feed.author.nickname,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = moyeoRelativeTime(feed.createdAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            feed.imageUrls.firstOrNull()?.let { imageUrl ->
                CachedRemoteImage(
                    url = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop,
                    fallbackShape = MoyeoPlaceholderShape.LANDSCAPE
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }

            feed.trip?.courseTitle?.let { courseTitle ->
                Text(
                    text = courseTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = feed.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "좋아요 ${feed.likeCount}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "댓글 ${feed.commentCount}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun FriendDexScreen(onBack: () -> Unit, onOpenCompanion: (DexCompanion?) -> Unit = {}) {
    // 로그인 상태면 실서버 도감(GET users/me/travel-dex)으로 대체한다
    val server = LocalServerData.current
    var serverCompanions by remember(server) { mutableStateOf<List<DexFriend>?>(null) }
    // 프로필 카드(25)에 나와 함께한 여행과 내가 남긴 메시지를 넘기려면 원본이 필요하다.
    // 도감 응답에 이미 들어 있어 카드에서 다시 조회하지 않는다.
    var serverRaw by remember(server) { mutableStateOf<List<DexCompanion>>(emptyList()) }
    LaunchedEffect(server) {
        serverCompanions = if (server == null) {
            serverRaw = emptyList()
            null
        } else {
            runCatching {
                val loaded = server.social.travelDex()
                serverRaw = loaded
                loaded.map { companion ->
                    DexFriend(
                        id = companion.userId.toString(),
                        nickname = companion.nickname,
                        // 이모지는 이미지가 없을 때의 폴백일 뿐이다 (R5 정본)
                        avatar = MoyeoNicknameAnimal.emojiForNickname(companion.nickname),
                        profileImageUrl = companion.profileImageUrl,
                        lastMetAt = companion.latestTripDate.replace('-', '.'),
                        lastMetDate = companion.latestTripDate,
                        metCount = companion.tripCount
                    )
                }
            }.getOrNull()
        }
    }
    val friends = serverCompanions.orEmpty()
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    // 27 도감 필터 — 근거는 도감 응답의 동행 횟수(`tripCount`)와 최근 동행일(`latestTripDate`)뿐이다.
    // 0건이어도 칩은 그린다(iOS 와 같다). 숫자는 실제 목록을 센 값이다.
    var dexFilter by remember { mutableStateOf(DexFilter.All) }
    val searched = friends.filter { friend ->
        searchQuery.isBlank() || friend.nickname.contains(searchQuery.trim(), ignoreCase = true)
    }
    val filteredFriends = searched.filter(dexFilter::matches)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CompactMenuHeader(
            title = "친구 도감",
            onBack = onBack,
            trailing = {
                IconButton(onClick = { showSearch = !showSearch }, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = if (showSearch) Icons.Filled.Close else Icons.Filled.Search,
                        contentDescription = if (showSearch) "검색 닫기" else "검색",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = menuContentPadding(bottom = 28.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.Bottom) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "지금까지 만난 친구",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = filteredFriends.size.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = ForestGreen,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "마리",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = if (searchQuery.isBlank()) "최근 동행 순" else "검색 결과",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (showSearch) {
                item {
                    DogamSearchField(query = searchQuery, onQueryChange = { searchQuery = it })
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DexFilter.entries.forEach { option ->
                        DogamFilterChip(
                            text = "${option.label} ${searched.count(option::matches)}",
                            selected = dexFilter == option,
                            onClick = { dexFilter = option },
                            modifier = Modifier.testTag("friend-dex-filter-${option.name.lowercase()}")
                        )
                    }
                }
            }
            item {
                when {
                    // 검색어가 있는데 결과가 없으면 §2 "검색 결과가 없어요"
                    filteredFriends.isEmpty() && searchQuery.isNotBlank() -> DogamEmptyResult()

                    filteredFriends.isEmpty() -> MoyeoEmptyState(
                        "아직 함께 여행한 친구가 없어요.",
                        testTag = "friend-dex-empty"
                    )

                    else -> DogamGrid(
                        friends = filteredFriends,
                        onOpenFriend = { friend ->
                            onOpenCompanion(serverRaw.firstOrNull { it.userId.toString() == friend.id })
                        }
                    )
                }
            }
            item {
                Text(
                    text = "다음 모임에서 새 친구를 만나보세요 ✨",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DogamSearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("friend-dex-search-field"),
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                TextButton(onClick = { onQueryChange("") }) {
                    Text("지우기", fontWeight = FontWeight.ExtraBold)
                }
            }
        },
        placeholder = {
            Text("친구 이름 검색")
        }
    )
}

@Composable
fun CustomerCenterScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CompactMenuHeader(title = "고객센터", onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = menuContentPadding(bottom = 44.dp)
        ) {
            item {
                MenuCard {
                    Text(
                        text = "오늘도 도와드릴게요",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "여행 모집과 채팅 중 불편한 점을 남기면 상담함에 바로 접수돼요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            item {
                // 예전에는 동작 없는 행 셋(`문의 접수`·`신고 내역`·`자주 묻는 질문`)이었다.
                // `>` 표시만 있고 콜백 자체가 없어 **눌러도 아무 일이 없었다.**
                // 상담함·신고 내역·FAQ 는 존재하지 않는 개념이다(정본 changeLog14) —
                // 실제로 갈 수 있는 두 창구만 남긴다. iOS `CustomerCenterLinkRow` · 웹 29 설정과 같다.
                MenuCard(contentPadding = 0.dp) {
                    CustomerCenterRow(
                        title = MoyeoContact.ISSUES_LABEL,
                        subtitle = "버그 제보와 기능 제안을 올려요",
                        onClick = { uriHandler.openUri(MoyeoContact.ISSUES_URL) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.58f))
                    CustomerCenterRow(
                        title = MoyeoContact.EMAIL_LABEL,
                        subtitle = MoyeoContact.EMAIL,
                        onClick = { uriHandler.openUri(MoyeoContact.MAILTO_URL) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerCenterRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            // `>` 를 그려 놓고 누를 수 없던 행이었다 — 표시와 동작을 같이 둔다.
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    accountService: AuthAccountService,
    onAuthenticationCleared: () -> Unit,
    onOpenNotificationDetail: () -> Unit = {},
    onOpenBlockedUsers: () -> Unit = {},
    /** 29-5 계정 연결. 예전에는 팝업이라 라우트가 없었다. */
    onOpenAccountProviders: () -> Unit = {},
    /** 13-2 내보내진 기록. 알림이 사라져도 사유를 다시 볼 수 있어야 한다. */
    onOpenKickHistory: () -> Unit = {},
    onOpenAccountDelete: () -> Unit = {},
    onOpenTerms: (String) -> Unit = {},
    onOpenOssLicenses: () -> Unit = {},
    /** 저장된 테마 설정. 캡처 모드에서는 화면기획대로 항상 `시스템 기본`이 내려온다. */
    themePreference: ThemePreference = ThemePreference.System,
    /** 테마 행 탭 — 화면기획에 선택 시트가 없어 시스템 기본 → 라이트 → 다크 순으로 순환한다. */
    onCycleThemePreference: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    // 캡처에서도 실제 빌드 버전을 그대로 보여준다 — 화면기획 값(1.0.4)을 덮어씌우면
    // 스크린샷이 있지도 않은 버전을 말하게 된다.
    // 최신 버전을 알려주는 서버 API가 없어 "최신 상태" 주장은 하지 않는다.
    val versionRowValue = BuildConfig.VERSION_NAME
    val versionDialogBody = "현재 설치된 버전은 ${BuildConfig.VERSION_NAME}이에요."
    var chatEnabled by remember { mutableStateOf(false) }
    var deadlineEnabled by remember { mutableStateOf(false) }
    var friendEnabled by remember { mutableStateOf(false) }
    var marketingEnabled by remember { mutableStateOf(false) }
    // 방해금지 시간대는 서버 알림 설정이 근거다 — 못 받아오면 줄에 값을 쓰지 않는다
    var doNotDisturbText by remember { mutableStateOf<String?>(null) }
    var selectedAction by remember { mutableStateOf<SettingsRowAction?>(null) }
    var isPerformingAccountAction by remember { mutableStateOf(false) }
    var accountErrorMessage by remember { mutableStateOf<String?>(null) }
    var connectedProviders by remember { mutableStateOf<Set<AuthProvider>>(emptySet()) }
    // 행에 적을 값(연결된 로그인 방식)은 서버가 근거다 — 못 받아오면 `관리` 로 남는다
    LaunchedEffect(accountService) {
        runCatching { accountService.linkedProviders() }.onSuccess { connectedProviders = it }
    }
    // 차단 인원 수는 실서버(GET users/me/blocks)가 근거다 — 못 받아오면 줄에 값을 쓰지 않는다
    val server = LocalServerData.current
    var serverBlockedCount by remember(server) { mutableStateOf<Int?>(null) }
    LaunchedEffect(server) {
        if (server == null) {
            serverBlockedCount = null
            doNotDisturbText = null
            return@LaunchedEffect
        }
        serverBlockedCount = runCatching { server.social.blocks().size }.getOrNull()
        runCatching { server.userProfile.profile() }.getOrNull()?.let { profile ->
            chatEnabled = profile.chatNotificationMode != "OFF"
            deadlineEnabled = profile.recruitmentDeadlineEnabled
            friendEnabled = profile.socialActivityEnabled
            marketingEnabled = profile.marketingEnabled
        }
        doNotDisturbText = runCatching { server.notifications.settings() }.getOrNull()
            ?.takeIf { it.doNotDisturbEnabled }
            ?.let { setting ->
                listOfNotNull(setting.doNotDisturbStartTime, setting.doNotDisturbEndTime)
                    .map { it.take(5) }
                    .takeIf { it.size == 2 }
                    ?.joinToString("~")
                    ?.let { "방해금지 $it" }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CompactMenuHeader(title = "설정", onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = menuContentPadding(top = 14.dp, bottom = 34.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsSectionGroup("알림") {
                    SettingsToggleRow("채팅 메시지", null, chatEnabled) {
                        chatEnabled = it
                    }
                    SettingsToggleRow("모집 마감 임박", "D-3부터 알려드려요", deadlineEnabled) {
                        deadlineEnabled = it
                    }
                    SettingsToggleRow("친구 신청·피드 반응", null, friendEnabled) {
                        friendEnabled = it
                    }
                    SettingsToggleRow("마케팅 알림", "이벤트·새 코스 소개", marketingEnabled) {
                        marketingEnabled = it
                    }
                    SettingsValueRow(title = "알림 세부 설정", value = doNotDisturbText) {
                        onOpenNotificationDetail()
                    }
                }
            }

            item {
                SettingsSectionGroup("화면") {
                    SettingsValueRow(
                        title = SettingsRowAction.Theme.rowTitle,
                        value = themePreference.label,
                        onClick = onCycleThemePreference
                    )
                    SettingsValueRow(action = SettingsRowAction.Language) {
                        selectedAction = it
                    }
                }
            }

            item {
                SettingsSectionGroup("계정") {
                    SettingsValueRow(
                        title = "로그인 방식",
                        value = connectedProviders.providerSummary().ifBlank { "관리" },
                        onClick = onOpenAccountProviders
                    )
                    SettingsValueRow(
                        title = SettingsRowAction.BlockedUsers.rowTitle,
                        value = serverBlockedCount?.let { "${it}명" } ?: SettingsRowAction.BlockedUsers.rowValue
                    ) {
                        onOpenBlockedUsers()
                    }
                    // 13-2 — 13-1 은 알림 한 건을 여는 화면이라, 알림이 사라지면 사유를 다시 볼 길이 없었다
                    SettingsValueRow(title = "내보내진 기록", value = "") { onOpenKickHistory() }
                    SettingsValueRow(action = SettingsRowAction.PrivacyPolicy) {
                        onOpenTerms("privacy")
                    }
                    SettingsValueRow(action = SettingsRowAction.Terms) {
                        onOpenTerms("service")
                    }
                }
            }

            item {
                SettingsSectionGroup("정보") {
                    SettingsValueRow(
                        title = SettingsRowAction.Version.rowTitle,
                        value = versionRowValue,
                        onClick = { selectedAction = SettingsRowAction.Version }
                    )
                    SettingsValueRow(action = SettingsRowAction.Contact) {
                        selectedAction = it
                    }
                    SettingsValueRow(action = SettingsRowAction.Rate) {
                        selectedAction = it
                    }
                    // 29-4 오픈소스 라이선스 — 앱 평가하기 다음, 로그아웃 위 (changeLog17)
                    SettingsValueRow(
                        title = "오픈소스 라이선스",
                        value = null,
                        onClick = onOpenOssLicenses
                    )
                    SettingsDangerRow(action = SettingsRowAction.Logout) {
                        selectedAction = it
                    }
                    SettingsDangerRow(action = SettingsRowAction.DeleteAccount) {
                        onOpenAccountDelete()
                    }
                }
            }
        }
    }

    selectedAction?.let { action ->
        SettingsActionDialog(
            action = action,
            body = if (action == SettingsRowAction.Version) versionDialogBody else action.dialogBody,
            isPerforming = isPerformingAccountAction,
            errorMessage = accountErrorMessage,
            onDismiss = {
                if (!isPerformingAccountAction) {
                    selectedAction = null
                    accountErrorMessage = null
                }
            },
            onConfirm = {
                if (action != SettingsRowAction.Logout && action != SettingsRowAction.DeleteAccount) {
                    selectedAction = null
                    return@SettingsActionDialog
                }
                isPerformingAccountAction = true
                accountErrorMessage = null
                coroutineScope.launch {
                    runCatching {
                        if (action == SettingsRowAction.DeleteAccount) {
                            accountService.withdraw()
                        } else {
                            accountService.logout()
                        }
                    }.onSuccess {
                        selectedAction = null
                        onAuthenticationCleared()
                    }.onFailure { error ->
                        accountErrorMessage = error.message ?: "계정 요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요."
                    }
                    isPerformingAccountAction = false
                }
            }
        )
    }
}

@Composable
internal fun CompactMenuHeader(
    title: String,
    onBack: () -> Unit,
    // 화면기획에서 헤더는 페이지가 회색인 화면(28)에서만 흰 표면으로 따로 칠해진다
    containerColor: Color? = null,
    trailing: @Composable () -> Unit = {
        Box(modifier = Modifier.size(34.dp))
    }
) {
    Column(modifier = if (containerColor == null) Modifier else Modifier.background(containerColor)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.weight(1f))
            trailing()
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.72f))
    }
}

@Composable
internal fun menuContentPadding(
    start: Dp = 18.dp,
    top: Dp = 18.dp,
    end: Dp = 18.dp,
    bottom: Dp = 32.dp
): PaddingValues {
    val density = LocalDensity.current
    val navigationBottom = with(density) {
        WindowInsets.navigationBars.getBottom(density).toDp()
    }
    return PaddingValues(start = start, top = top, end = end, bottom = bottom + navigationBottom)
}

@Composable
private fun ProfileMenuRow(title: String, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(66.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun DogamGrid(friends: List<DexFriend>, onOpenFriend: (DexFriend) -> Unit = {}) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        friends.chunked(3).forEach { rowFriends ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowFriends.forEach { friend ->
                    DogamFriendCard(
                        friend = friend,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenFriend(friend) }
                    )
                }
                repeat(3 - rowFriends.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DogamEmptyResult() {
    MenuCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "검색 결과가 없어요",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "이름이나 필터를 바꿔 다시 찾아보세요.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DogamFriendCard(friend: DexFriend, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(108.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 7.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box {
                // 서버가 프로필 이미지를 주면 그 이미지를 그린다 — 이모지는 없거나 못 불러올 때만이다.
                UserAvatar(
                    imageUrl = friend.profileImageUrl,
                    nickname = friend.nickname,
                    modifier = Modifier.size(42.dp),
                    fallbackFontSize = 20.sp
                )
                // 내 카드는 "나" 배지, 여러 번 만난 친구는 횟수 배지 (화면기획 27)
                // 여러 번 만난 친구는 횟수 배지 (화면기획 27)
                val badge = if (friend.metCount > 1) "${friend.metCount}x" else null
                if (badge != null) {
                    Text(
                        text = badge,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(50))
                            .background(ForestGreen)
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Text(
                // 화면기획 27의 카드 라벨은 닉네임 앞 두 어절만 — 숫자까지 넣으면 한 줄에 안 들어간다
                text = friend.nickname.split(" ").take(2).joinToString(" "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = friend.lastMetAt,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DogamFilterChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) ForestGreen else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        style = MaterialTheme.typography.labelMedium,
        color = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
private fun SettingsSectionGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsToggleRow(title: String, subtitle: String?, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (subtitle == null) 58.dp else 64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
    SettingsRowDivider()
}

@Composable
private fun SettingsValueRow(action: SettingsRowAction, onClick: (SettingsRowAction) -> Unit) {
    SettingsValueRow(action.rowTitle, action.rowValue) { onClick(action) }
}

@Composable
private fun SettingsValueRow(title: String, value: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
    SettingsRowDivider()
}

/**
 * 29-5 계정 연결 (로그인 방식) — `GET/POST /api/v1/auth/providers`.
 *
 * 29 설정의 `로그인 방식 › 관리` 가 가리키는 목적지가 없었다 — 셰브런이 다음 화면을 약속하는데
 * 열리는 것은 팝업이라 캡처에도 잡히지 않았다. 이제 라우트를 가진 화면이다(정본 §6-1).
 *
 * 서버에는 **연결 해제 API 가 없다**. 그래서 화면에도 끊는 버튼을 두지 않고, 왜 없는지를 적는다.
 */
@Composable
fun AccountProvidersScreen(accountService: AuthAccountService, onBack: () -> Unit) {
    var connectedProviders by remember { mutableStateOf<Set<AuthProvider>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }
    var linkingProvider by remember { mutableStateOf<AuthProvider?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(accountService) {
        isLoading = true
        errorMessage = null
        runCatching { accountService.linkedProviders() }
            .onSuccess { connectedProviders = it }
            .onFailure { errorMessage = it.message ?: "로그인 방식을 확인하지 못했어요." }
        isLoading = false
    }

    val onLink: (AuthProvider, EmailAuthRequest?) -> Unit = { provider, emailRequest ->
        isLoading = true
        linkingProvider = provider
        errorMessage = null
        coroutineScope.launch {
            runCatching { accountService.linkProvider(provider, emailRequest) }
                .onSuccess { connectedProviders = it }
                .onFailure { errorMessage = it.message ?: "로그인 방식을 연결하지 못했어요." }
            isLoading = false
            linkingProvider = null
        }
    }

    var isEmailFormExpanded by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val darkTheme = MoyeoTheme.isDark
    val inputBackground = if (darkTheme) Color(0xFF0D1411) else Color.White
    val softLine = if (darkTheme) Color(0xFF24332D) else Color(0xFFEEF0EE)

    run {
        Surface(
            modifier = Modifier.fillMaxSize().testTag("account-providers-screen"),
            color = if (darkTheme) Color(0xFF0D1411) else Color.White,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(start = 18.dp, top = 10.dp, end = 18.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                    Text(
                        text = "로그인 방식",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = "어느 방법으로든 같은 계정으로 들어와요. " +
                        "하나를 더 연결해두면 한쪽을 못 쓰게 돼도 들어올 수 있어요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    providerConnectionOrder.forEach { provider ->
                        val connected = provider in connectedProviders
                        ProviderConnectionCard(
                            provider = provider,
                            connected = connected,
                            isLoading = linkingProvider == provider,
                            enabled = !isLoading && !connected,
                            showAction = provider != AuthProvider.EMAIL || !isEmailFormExpanded,
                            onClick = {
                                if (provider == AuthProvider.EMAIL) {
                                    isEmailFormExpanded = true
                                } else {
                                    onLink(provider, null)
                                }
                            }
                        ) {
                            if (provider == AuthProvider.EMAIL && !connected && isEmailFormExpanded) {
                                Text(
                                    text = "아직 가입되지 않은 이메일을 새 로그인 수단으로 추가해요.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    placeholder = { Text("새 이메일") },
                                    singleLine = true,
                                    enabled = !isLoading,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = inputBackground,
                                        unfocusedContainerColor = inputBackground,
                                        disabledContainerColor = inputBackground,
                                        focusedBorderColor = softLine,
                                        unfocusedBorderColor = softLine,
                                        disabledBorderColor = softLine
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("providers-email-email")
                                )
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    placeholder = { Text("비밀번호") },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    enabled = !isLoading,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = inputBackground,
                                        unfocusedContainerColor = inputBackground,
                                        disabledContainerColor = inputBackground,
                                        focusedBorderColor = softLine,
                                        unfocusedBorderColor = softLine,
                                        disabledBorderColor = softLine
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("providers-email-password")
                                )
                                Button(
                                    onClick = {
                                        onLink(
                                            AuthProvider.EMAIL,
                                            EmailAuthRequest(email, password)
                                        )
                                    },
                                    enabled = !isLoading && email.contains('@') && password.length >= 6,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ForestGreen,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 46.dp)
                                        .testTag("providers-email-submit")
                                ) {
                                    if (linkingProvider == AuthProvider.EMAIL) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.size(8.dp))
                                    }
                                    Text(
                                        if (linkingProvider == AuthProvider.EMAIL) {
                                            "연결하고 있어요"
                                        } else {
                                            "새 이메일 연결"
                                        },
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                TextButton(
                                    onClick = { isEmailFormExpanded = false },
                                    enabled = !isLoading,
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("입력 닫기")
                                }
                            }
                        }
                    }
                    errorMessage?.let {
                        Text(
                            it,
                            modifier = Modifier.testTag("account-providers-error"),
                            color = Coral,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (isLoading && linkingProvider == null) Text("로그인 방식을 확인하고 있어요…")
                    // 마지막 하나는 끊을 수 없다 — 끊으면 아무 방법으로도 못 들어온다.
                    // (서버에 연결 해제 API 자체가 없어, 화면에도 끊는 버튼을 두지 않는다.)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            "마지막 하나 남은 로그인 방식은 끊을 수 없어요. 계정을 아예 지우려면 설정에서 탈퇴해 주세요.",
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private val providerConnectionOrder = listOf(
    AuthProvider.KAKAO,
    AuthProvider.GOOGLE,
    AuthProvider.EMAIL,
    AuthProvider.APPLE
)

@Composable
private fun ProviderConnectionCard(
    provider: AuthProvider,
    connected: Boolean,
    isLoading: Boolean,
    enabled: Boolean,
    showAction: Boolean,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val darkTheme = MoyeoTheme.isDark
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) Color(0xFF18231E) else Color.White
        ),
        border = BorderStroke(
            1.dp,
            if (darkTheme) Color(0xFF24332D) else Color(0xFFEEF0EE)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProviderConnectionIcon(provider)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = provider.displayName(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (connected) "연결됨" else "연결되지 않음",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (connected) ForestGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (connected) {
                    Text("연결됨", color = ForestGreen, fontWeight = FontWeight.Bold)
                } else if (showAction) {
                    // 연결 버튼은 **줄 오른쪽에 작게** 둔다 (웹과 같은 배치, 사용자 지적 2026-09-09).
                    // 예전에는 줄 아래에 폭을 꽉 채우는 큰 버튼이라 카드 하나가 웹의 두 배 높이였다.
                    // 색은 그대로다 — 카카오 노랑·Apple 흑백·Google 외곽선은 각 사업자의 브랜드 규정이다.
                    ProviderConnectionButton(
                        provider = provider,
                        isLoading = isLoading,
                        onClick = onClick,
                        enabled = enabled,
                        modifier = Modifier.testTag("providers-${provider.pathValue}-link")
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun ProviderConnectionButton(
    provider: AuthProvider,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val darkTheme = MoyeoTheme.isDark
    val containerColor = when (provider) {
        AuthProvider.KAKAO -> Color(0xFFFEE500)
        AuthProvider.GOOGLE -> if (darkTheme) Color(0xFF131314) else Color.White
        AuthProvider.EMAIL -> if (darkTheme) Color(0xFF18231E) else Color.White
        AuthProvider.APPLE -> if (darkTheme) Color.White else Color.Black
    }
    val contentColor = when (provider) {
        AuthProvider.KAKAO -> Color.Black.copy(alpha = 0.85f)
        AuthProvider.GOOGLE -> if (darkTheme) Color(0xFFE3E3E3) else Color(0xFF1F1F1F)
        AuthProvider.EMAIL -> MaterialTheme.colorScheme.onSurface
        AuthProvider.APPLE -> if (darkTheme) Color.Black else Color.White
    }
    val border = when (provider) {
        AuthProvider.GOOGLE ->
            BorderStroke(1.dp, if (darkTheme) Color(0xFF8E918F) else Color(0xFF747775))

        AuthProvider.EMAIL -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)

        else -> null
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(34.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.48f),
            disabledContentColor = contentColor.copy(alpha = 0.48f)
        ),
        border = border,
        contentPadding = PaddingValues(horizontal = 14.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(6.dp))
        }
        // 문구는 네 표면 모두 「연결하기」다 (기획 `ScreenAccountProviders`).
        // 사업자 이름은 바로 왼쪽 줄에 이미 적혀 있어 버튼에서 한 번 더 부르지 않는다.
        Text(
            text = if (isLoading) "연결 중" else "연결하기",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun ProviderConnectionIcon(provider: AuthProvider) {
    if (provider == AuthProvider.EMAIL) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "@",
                color = ForestGreen,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    } else {
        val darkTheme = MoyeoTheme.isDark
        val containerColor = when (provider) {
            AuthProvider.KAKAO -> Color(0xFFFEE500)
            AuthProvider.GOOGLE -> if (darkTheme) Color(0xFF131314) else Color.White
            AuthProvider.APPLE -> if (darkTheme) Color.White else Color.Black
            AuthProvider.EMAIL -> Color.Transparent
        }
        Surface(
            modifier = Modifier.size(38.dp),
            shape = RoundedCornerShape(9.dp),
            color = containerColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Image(
                    painter = painterResource(provider.iconResource()),
                    contentDescription = "${provider.displayName()} 로고",
                    modifier = Modifier
                        .size(
                            when (provider) {
                                AuthProvider.KAKAO -> 30.dp
                                AuthProvider.APPLE -> 20.dp
                                AuthProvider.GOOGLE -> 20.dp
                                AuthProvider.EMAIL -> 18.dp
                            }
                        )
                )
            }
        }
    }
}

private fun AuthProvider.iconResource(): Int = when (this) {
    AuthProvider.KAKAO -> R.drawable.kakao_login_official
    AuthProvider.GOOGLE -> R.drawable.google_g_official
    AuthProvider.APPLE -> R.drawable.apple_continue_official
    AuthProvider.EMAIL -> error("이메일은 텍스트 아이콘을 사용해요.")
}

private fun Set<AuthProvider>.providerSummary(): String = sortedBy(AuthProvider::ordinal)
    .joinToString(" · ") { it.displayName() }

private fun AuthProvider.displayName(): String = when (this) {
    AuthProvider.EMAIL -> "이메일"
    AuthProvider.GOOGLE -> "Google"
    AuthProvider.KAKAO -> "카카오"
    AuthProvider.APPLE -> "Apple"
}

@Composable
private fun SettingsDangerRow(action: SettingsRowAction, onClick: (SettingsRowAction) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clickable { onClick(action) }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = action.rowTitle,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                color = Coral,
                fontWeight = FontWeight.ExtraBold
            )
            if (action.rowValue != null) {
                Text(
                    text = action.rowValue,
                    style = MaterialTheme.typography.labelSmall,
                    color = Coral.copy(alpha = 0.78f)
                )
            }
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
    SettingsRowDivider()
}

@Composable
private fun SettingsRowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 16.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    )
}

@Composable
private fun SettingsActionDialog(
    action: SettingsRowAction,
    body: String,
    isPerforming: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MoyeoTheme.sheetSurface,
        titleContentColor = if (action.danger) Coral else MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Text(
                text = action.dialogTitle,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium
                )
                // 문의하기는 안내만 하고 끝나면 아무 일도 일어나지 않는다 —
                // 실제로 갈 수 있는 두 창구를 버튼으로 둔다.
                if (action == SettingsRowAction.Contact) {
                    val uriHandler = LocalUriHandler.current
                    OutlinedButton(
                        onClick = { uriHandler.openUri(MoyeoContact.ISSUES_URL) },
                        modifier = Modifier.fillMaxWidth().height(44.dp).testTag("settings-contact-issues"),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(MoyeoContact.ISSUES_LABEL, fontWeight = FontWeight.ExtraBold) }
                    OutlinedButton(
                        onClick = { uriHandler.openUri(MoyeoContact.MAILTO_URL) },
                        modifier = Modifier.fillMaxWidth().height(44.dp).testTag("settings-contact-email"),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(MoyeoContact.EMAIL_LABEL, fontWeight = FontWeight.ExtraBold) }
                }
                errorMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = Coral
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isPerforming) {
                Text(
                    text = if (isPerforming) "처리 중..." else action.confirmLabel,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        dismissButton = {
            if (action.danger) {
                TextButton(onClick = onDismiss, enabled = !isPerforming) {
                    Text("취소")
                }
            }
        }
    )
}

/** 설정 화면의 한 줄. 값(rowValue)은 서버·기기 상태로 채우는 자리는 null 이다. */
private enum class SettingsRowAction(
    val rowTitle: String,
    val rowValue: String?,
    val dialogTitle: String,
    val dialogBody: String,
    val confirmLabel: String = "닫기",
    val danger: Boolean = false
) {
    Theme(
        rowTitle = "테마",
        // 값은 화면이 저장된 테마 설정으로 채운다 — 여기 고정값을 두지 않는다
        rowValue = null,
        dialogTitle = "테마 설정",
        dialogBody = "시스템 설정에 맞춰 밝은 화면과 어두운 화면을 자동으로 전환해요."
    ),
    Language(
        rowTitle = "언어",
        rowValue = "한국어",
        dialogTitle = "언어 설정",
        dialogBody = "현재는 한국어 기준으로 표시되고, 지역 안내 문구도 같은 언어 기준을 따라가요."
    ),
    BlockedUsers(
        rowTitle = "차단한 사용자",
        // 인원수는 화면이 GET users/me/blocks 로 채운다
        rowValue = null,
        dialogTitle = "차단한 사용자",
        dialogBody = "차단 목록을 확인하고 필요하면 차단을 해제할 수 있어요."
    ),
    PrivacyPolicy(
        rowTitle = "개인정보 처리방침",
        rowValue = null,
        dialogTitle = "개인정보 처리방침",
        dialogBody = "모여트립의 개인정보 수집, 보관, 삭제 기준을 확인해요."
    ),
    Terms(
        rowTitle = "이용약관",
        rowValue = null,
        dialogTitle = "이용약관",
        dialogBody = "여행 모집, 채팅, 후기 이용 규칙을 확인해요."
    ),
    Version(
        rowTitle = "버전",
        // 값과 본문은 화면에서 BuildConfig.VERSION_NAME 으로 채운다 — 여기 고정값을 두지 않는다.
        rowValue = null,
        dialogTitle = "앱 버전",
        dialogBody = ""
    ),
    Contact(
        rowTitle = "문의하기",
        rowValue = null,
        dialogTitle = "문의하기",
        // 예전 문구는 "고객센터로 이어져요" 였는데 고객센터는 존재하지 않는 개념이다.
        dialogBody = MoyeoContact.DIALOG_BODY
    ),
    Rate(
        rowTitle = "앱 평가하기",
        rowValue = null,
        dialogTitle = "앱 평가하기",
        dialogBody = "스토어 평가로 이동하기 전, 모여트립 사용 경험을 한 번 더 확인해요."
    ),
    Logout(
        rowTitle = "로그아웃",
        rowValue = null,
        dialogTitle = "로그아웃 안내",
        dialogBody = "로그아웃하면 이 기기에서 계정 연결이 해제돼요.",
        confirmLabel = "확인",
        danger = true
    ),
    DeleteAccount(
        rowTitle = "계정 탈퇴",
        rowValue = "30일 안에는 되살릴 수 있어요",
        dialogTitle = "계정 탈퇴 안내",
        dialogBody = "탈퇴 요청 후 30일 동안 복구할 수 있고, 이후 사용자 정보와 로그인 연결이 영구 삭제돼요.",
        confirmLabel = "탈퇴 계속하기",
        danger = true
    )
}

@Composable
private fun ProfileEditFieldCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    MenuCard {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold
        )
        content()
    }
}

@Composable
private fun ProfileEditChipGrid(items: List<String>, selectedItems: Set<String>, onToggle: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(3).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { item ->
                    SelectableProfilePill(
                        text = item,
                        selected = selectedItems.contains(item),
                        onClick = { onToggle(item) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile-edit-region-$item")
                    )
                }
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SelectableProfilePill(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) ForestGreen else ForestGreen.copy(alpha = 0.14f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        style = MaterialTheme.typography.labelMedium,
        color = if (selected) MaterialTheme.colorScheme.onPrimary else ForestGreen,
        fontWeight = FontWeight.ExtraBold,
        maxLines = 1
    )
}

@Composable
private fun MenuCard(contentPadding: androidx.compose.ui.unit.Dp = 16.dp, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@Composable
private fun ProfilePill(text: String, tint: androidx.compose.ui.graphics.Color) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(tint.copy(alpha = 0.14f))
            .padding(horizontal = 9.dp, vertical = 5.dp),
        style = MaterialTheme.typography.labelSmall,
        color = tint,
        fontWeight = FontWeight.ExtraBold,
        maxLines = 1
    )
}

/** 프로필 메뉴 한 줄. 아이콘 + 라벨 + chevron (화면기획). */
@Composable
private fun ProfileMenuRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.onSurface)
            Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = colors.onSurfaceVariant
            )
        }
        HorizontalDivider(color = colors.outlineVariant)
    }
}
