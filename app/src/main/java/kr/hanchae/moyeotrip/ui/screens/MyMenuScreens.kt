package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.R
import kr.hanchae.moyeotrip.data.DogamFriend
import kr.hanchae.moyeotrip.data.FeedPost
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.data.Profile
import kr.hanchae.moyeotrip.data.auth.AuthAccountService
import kr.hanchae.moyeotrip.domain.auth.AuthProvider
import kr.hanchae.moyeotrip.domain.auth.EmailAuthAction
import kr.hanchae.moyeotrip.domain.auth.EmailAuthRequest
import kr.hanchae.moyeotrip.domain.auth.UserDisplayProfile
import kr.hanchae.moyeotrip.ui.theme.Coral
import kr.hanchae.moyeotrip.ui.theme.ForestGreen

@Composable
fun ProfileScreen(
    userProfile: UserDisplayProfile,
    onBack: () -> Unit,
    onOpenProfileEdit: () -> Unit,
    onOpenFriendDex: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val profile = MockTripRepository.profile

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CompactMenuHeader(
            title = "프로필",
            onBack = onBack,
            trailing = {
                IconButton(onClick = onOpenSettings, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "설정",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = menuContentPadding(bottom = 28.dp)
        ) {
            item { ProfileHeaderCard(profile = profile, userProfile = userProfile) }
            item { ProfileStatsRow(profile = profile) }
            item {
                ProfileMenuCard(
                    onOpenProfileEdit = onOpenProfileEdit,
                    onOpenFriendDex = onOpenFriendDex
                )
            }
            item { ProfileTagCard(profile = profile) }
            item { DogamPreviewCard(onOpenFriendDex = onOpenFriendDex) }
        }
    }
}

@Composable
fun ProfileEditScreen(userProfile: UserDisplayProfile, onBack: () -> Unit) {
    val profile = MockTripRepository.profile
    var name by remember(userProfile.nickname) { mutableStateOf(userProfile.nickname ?: "") }
    var bio by remember { mutableStateOf(profile.bio) }
    var selectedRegions by remember { mutableStateOf(setOf("청송", "안동", "경주", "울릉")) }
    var selectedPace by remember { mutableStateOf("천천히") }
    var showSavedDialog by remember { mutableStateOf(false) }
    val regionOptions = listOf("청송", "안동", "경주", "울릉", "포항", "문경")
    val paceOptions = listOf("천천히", "사진 많이", "로컬 맛집", "짧은 코스")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("screen-profile-edit")
    ) {
        CompactMenuHeader(
            title = "내 정보 수정",
            onBack = onBack,
            trailing = {
                TextButton(
                    onClick = { showSavedDialog = true },
                    modifier = Modifier.testTag("profile-edit-save")
                ) {
                    Text(text = "저장", fontWeight = FontWeight.ExtraBold)
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = menuContentPadding(bottom = 42.dp)
        ) {
            item {
                MenuCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        UserAvatar(
                            imageUrl = userProfile.profileImageUrl,
                            nickname = userProfile.nickname,
                            modifier = Modifier.size(62.dp),
                            fallbackFontSize = 28.sp
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(
                                text = userProfile.nickname ?: "내 프로필",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "대표 캐릭터와 소개는 여행 친구에게 먼저 보여요.",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item {
                ProfileEditFieldCard(title = "닉네임") {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile-edit-name"),
                        singleLine = true,
                        enabled = false,
                        supportingText = { Text("닉네임 변경 API가 아직 제공되지 않아요.") }
                    )
                }
            }
            item {
                ProfileEditFieldCard(title = "한 줄 소개") {
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile-edit-bio"),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }
            item {
                ProfileEditFieldCard(title = "선호 지역") {
                    ProfileEditChipGrid(
                        items = regionOptions,
                        selectedItems = selectedRegions,
                        onToggle = { region ->
                            selectedRegions = if (selectedRegions.contains(region)) {
                                selectedRegions - region
                            } else {
                                selectedRegions + region
                            }
                        }
                    )
                }
            }
            item {
                ProfileEditFieldCard(title = "여행 취향") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        paceOptions.forEach { option ->
                            SelectableProfilePill(
                                text = option,
                                selected = selectedPace == option,
                                onClick = { selectedPace = option }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSavedDialog) {
        AlertDialog(
            onDismissRequest = { showSavedDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(text = "저장 완료", fontWeight = FontWeight.ExtraBold)
            },
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
}

@Composable
fun MyFeedScreen(onBack: () -> Unit, onOpenPost: (String) -> Unit) {
    val posts = MockTripRepository.feedPosts

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
                ProfilePill(text = "${posts.size}", tint = ForestGreen)
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = menuContentPadding(bottom = 34.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "내가 남긴 경북 여행 기록",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "사진, 경로, 함께 간 친구가 남아 있는 피드를 모아봐요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            items(items = posts, key = { post -> post.id }) { post ->
                MyFeedPostCard(
                    post = post,
                    onClick = { onOpenPost(post.id) }
                )
            }
        }
    }
}

@Composable
private fun MyFeedPostCard(post: FeedPost, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("my-feed-post-${post.id}"),
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
                MascotCircle(text = post.avatar, size = 42)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = post.author,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${post.region} · ${post.visibility.label}",
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

            FeedMediaGrid(
                post = post,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                compactRoute = true
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = post.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfilePill(text = post.region, tint = ForestGreen)
                ProfilePill(text = post.visibility.label, tint = Coral)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "좋아요 ${post.likes}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "댓글 ${post.comments}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = post.photoCountText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun FriendDexScreen(onBack: () -> Unit) {
    val friends = MockTripRepository.dogamFriends
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(DogamFriendFilter.All) }
    val filteredFriends = friends
        .filter { selectedFilter.includes(it) }
        .filter { friend ->
            searchQuery.isBlank() || friend.nickname.contains(searchQuery.trim(), ignoreCase = true)
        }

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
                        text = if (searchQuery.isBlank()) selectedFilter.summary else "검색 결과",
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
                    DogamFriendFilter.entries.forEach { filter ->
                        DogamFilterChip(
                            text = filter.title,
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            modifier = Modifier.testTag("friend-dex-filter-${filter.name}")
                        )
                    }
                }
            }
            item {
                if (filteredFriends.isEmpty()) {
                    DogamEmptyResult()
                } else {
                    DogamGrid(friends = filteredFriends)
                }
            }
            item {
                Text(
                    text = "다음 모임에서 새 친구를 만나보세요",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private enum class DogamFriendFilter(val title: String, val summary: String) {
    All("전체 12", "최근 동행 순"),
    Repeated("2회 이상 4", "2회 이상 만난 친구"),
    Recent("최근 1주 3", "최근 1주 동행");

    fun includes(friend: DogamFriend): Boolean = when (this) {
        All -> true
        Repeated -> friend.metCount >= 2
        Recent -> friend.lastMetAt.contains("일 전")
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
                MenuCard(contentPadding = 0.dp) {
                    CustomerCenterRow(
                        title = "문의 접수",
                        subtitle = "모집, 채팅, 결제 문의를 남겨요",
                        badge = "평균 2시간"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.58f))
                    CustomerCenterRow(
                        title = "신고 내역",
                        subtitle = "접수한 신고와 처리 상태를 확인해요",
                        badge = "0건"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.58f))
                    CustomerCenterRow(
                        title = "자주 묻는 질문",
                        subtitle = "동행 확정, 환불, 안전 수칙을 빠르게 찾아요",
                        badge = "FAQ"
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerCenterRow(title: String, subtitle: String, badge: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
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
        ProfilePill(text = badge, tint = ForestGreen)
    }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    accountService: AuthAccountService,
    onAuthenticationCleared: () -> Unit,
    onOpenNotificationDetail: () -> Unit = {},
    onOpenBlockedUsers: () -> Unit = {},
    onOpenAccountDelete: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var chatEnabled by remember { mutableStateOf(true) }
    var deadlineEnabled by remember { mutableStateOf(true) }
    var friendEnabled by remember { mutableStateOf(true) }
    var marketingEnabled by remember { mutableStateOf(false) }
    var selectedAction by remember { mutableStateOf<SettingsMockAction?>(null) }
    var isPerformingAccountAction by remember { mutableStateOf(false) }
    var accountErrorMessage by remember { mutableStateOf<String?>(null) }
    var showProviderDialog by remember { mutableStateOf(false) }
    var connectedProviders by remember { mutableStateOf<Set<AuthProvider>>(emptySet()) }
    var providerLoading by remember { mutableStateOf(false) }
    var linkingProvider by remember { mutableStateOf<AuthProvider?>(null) }
    var providerError by remember { mutableStateOf<String?>(null) }

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
                    SettingsValueRow(title = "알림 세부 설정", value = "방해금지 · 모임별") {
                        onOpenNotificationDetail()
                    }
                }
            }

            item {
                SettingsSectionGroup("화면") {
                    SettingsValueRow(action = SettingsMockAction.Theme) {
                        selectedAction = it
                    }
                    SettingsValueRow(action = SettingsMockAction.Language) {
                        selectedAction = it
                    }
                }
            }

            item {
                SettingsSectionGroup("계정") {
                    SettingsValueRow(
                        title = "로그인 방식",
                        value = connectedProviders.providerSummary().ifBlank { "확인하기" },
                        onClick = {
                            showProviderDialog = true
                            providerLoading = true
                            providerError = null
                            coroutineScope.launch {
                                runCatching { accountService.linkedProviders() }
                                    .onSuccess { connectedProviders = it }
                                    .onFailure { providerError = it.message }
                                providerLoading = false
                            }
                        }
                    )
                    SettingsValueRow(action = SettingsMockAction.BlockedUsers) {
                        onOpenBlockedUsers()
                    }
                    SettingsValueRow(action = SettingsMockAction.PrivacyPolicy) {
                        selectedAction = it
                    }
                    SettingsValueRow(action = SettingsMockAction.Terms) {
                        selectedAction = it
                    }
                }
            }

            item {
                SettingsSectionGroup("정보") {
                    SettingsValueRow(action = SettingsMockAction.Version) {
                        selectedAction = it
                    }
                    SettingsValueRow(action = SettingsMockAction.Contact) {
                        selectedAction = it
                    }
                    SettingsValueRow(action = SettingsMockAction.Rate) {
                        selectedAction = it
                    }
                    SettingsDangerRow(action = SettingsMockAction.Logout) {
                        selectedAction = it
                    }
                    SettingsDangerRow(action = SettingsMockAction.DeleteAccount) {
                        onOpenAccountDelete()
                    }
                }
            }
        }
    }

    selectedAction?.let { action ->
        SettingsActionDialog(
            action = action,
            isPerforming = isPerformingAccountAction,
            errorMessage = accountErrorMessage,
            onDismiss = {
                if (!isPerformingAccountAction) {
                    selectedAction = null
                    accountErrorMessage = null
                }
            },
            onConfirm = {
                if (action != SettingsMockAction.Logout && action != SettingsMockAction.DeleteAccount) {
                    selectedAction = null
                    return@SettingsActionDialog
                }
                isPerformingAccountAction = true
                accountErrorMessage = null
                coroutineScope.launch {
                    runCatching {
                        if (action == SettingsMockAction.DeleteAccount) {
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

    if (showProviderDialog) {
        ProviderManagementDialog(
            connectedProviders = connectedProviders,
            isLoading = providerLoading,
            linkingProvider = linkingProvider,
            errorMessage = providerError,
            onDismiss = { if (!providerLoading) showProviderDialog = false },
            onLink = { provider, emailRequest ->
                providerLoading = true
                linkingProvider = provider
                providerError = null
                coroutineScope.launch {
                    runCatching { accountService.linkProvider(provider, emailRequest) }
                        .onSuccess { connectedProviders = it }
                        .onFailure { providerError = it.message ?: "로그인 방식을 연결하지 못했어요." }
                    providerLoading = false
                    linkingProvider = null
                }
            }
        )
    }
}

@Composable
private fun CompactMenuHeader(
    title: String,
    onBack: () -> Unit,
    trailing: @Composable () -> Unit = {
        Box(modifier = Modifier.size(34.dp))
    }
) {
    Column {
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
private fun menuContentPadding(start: Dp = 18.dp, top: Dp = 18.dp, end: Dp = 18.dp, bottom: Dp = 32.dp): PaddingValues {
    val density = LocalDensity.current
    val navigationBottom = with(density) {
        WindowInsets.navigationBars.getBottom(density).toDp()
    }
    return PaddingValues(start = start, top = top, end = end, bottom = bottom + navigationBottom)
}

@Composable
private fun ProfileHeaderCard(profile: Profile, userProfile: UserDisplayProfile) {
    MenuCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            UserAvatar(
                imageUrl = userProfile.profileImageUrl,
                nickname = userProfile.nickname,
                modifier = Modifier.size(76.dp),
                fallbackFontSize = 34.sp
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    text = userProfile.nickname ?: "내 프로필",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = userProfile.nicknameColor?.let { "닉네임 색상 · $it" } ?: "모여트립 여행자",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForestGreen,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = profile.region,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            profile.badges.take(3).forEach { badge ->
                ProfilePill(text = badge, tint = Coral)
            }
        }
    }
}

@Composable
private fun ProfileStatsRow(profile: Profile) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard(
            title = "여행",
            value = profile.joinedTrips.toString(),
            valueTag = "profile-stat-trips-value",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "호스트",
            value = profile.hostedTrips.toString(),
            valueTag = "profile-stat-hosted-value",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "피드",
            value = profile.feedCount.toString(),
            valueTag = "profile-stat-feed-value",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(title: String, value: String, valueTag: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(82.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                modifier = Modifier.testTag(valueTag),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileMenuCard(onOpenProfileEdit: () -> Unit, onOpenFriendDex: () -> Unit) {
    MenuCard(contentPadding = 0.dp) {
        ProfileMenuRow(
            title = "내 정보 수정",
            subtitle = "프로필과 여행 취향을 관리해요",
            modifier = Modifier.testTag("profile-menu-edit"),
            onClick = onOpenProfileEdit
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.58f))
        ProfileMenuRow(
            title = "친구 도감",
            subtitle = "함께 다녀온 친구를 모아봐요",
            modifier = Modifier.testTag("profile-menu-friend-dex"),
            onClick = onOpenFriendDex
        )
    }
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
private fun ProfileTagCard(profile: Profile) {
    MenuCard {
        Text(
            text = "선호 지역",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("청송", "안동", "경주", "울릉").forEach { region ->
                ProfilePill(text = region, tint = ForestGreen)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.58f))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            MascotCircle(text = "🍃", size = 38)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "이번 달 추천",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "문경새재 숲길 힐링 워크가 프로필 취향과 잘 맞아요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DogamPreviewCard(onOpenFriendDex: () -> Unit) {
    MenuCard {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "지금까지 만난 친구",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${MockTripRepository.dogamFriends.size}마리 · 최근 동행 순",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            ProfilePill(text = "친구에게만", tint = ForestGreen)
        }
        MockTripRepository.dogamFriends.take(4).forEach { friend ->
            DogamPreviewRow(friend = friend, onClick = onOpenFriendDex)
        }
    }
}

@Composable
private fun DogamPreviewRow(friend: DogamFriend, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("profile-dogam-preview-${friend.id}")
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MascotCircle(text = friend.avatar, size = 38)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = friend.nickname,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = friend.lastMetAt,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (friend.metCount > 1) {
            ProfilePill(text = "${friend.metCount}x", tint = Coral)
        }
    }
}

@Composable
private fun DogamGrid(friends: List<DogamFriend>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        friends.chunked(3).forEach { rowFriends ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowFriends.forEach { friend ->
                    DogamFriendCard(friend = friend, modifier = Modifier.weight(1f))
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
private fun DogamFriendCard(friend: DogamFriend, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(108.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (friend.id == "dogam-01") ForestGreen else MaterialTheme.colorScheme.outline
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
                MascotCircle(text = friend.avatar, size = 42)
                if (friend.metCount > 1) {
                    Text(
                        text = "${friend.metCount}x",
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
                text = friend.nickname,
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
private fun SettingsValueRow(action: SettingsMockAction, onClick: (SettingsMockAction) -> Unit) {
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

@Composable
private fun ProviderManagementDialog(
    connectedProviders: Set<AuthProvider>,
    isLoading: Boolean,
    linkingProvider: AuthProvider?,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onLink: (AuthProvider, EmailAuthRequest?) -> Unit
) {
    var isEmailFormExpanded by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val darkTheme = isSystemInDarkTheme()
    val inputBackground = if (darkTheme) Color(0xFF0D1411) else Color.White
    val softLine = if (darkTheme) Color(0xFF24332D) else Color(0xFFEEF0EE)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
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
                    Text(
                        text = "로그인 방식",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text("완료", fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = "연결된 로그인 수단으로 같은 계정을 안전하게 이용할 수 있어요.",
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
                                            EmailAuthRequest(email, password, EmailAuthAction.CREATE_ACCOUNT)
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
                    errorMessage?.let { Text(it, color = Coral, style = MaterialTheme.typography.bodySmall) }
                    if (isLoading && linkingProvider == null) Text("로그인 방식을 확인하고 있어요…")
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
    val darkTheme = isSystemInDarkTheme()
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
                        text = if (connected) "연결됨" else provider.connectionHint(),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (connected) ForestGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (connected) {
                    Text("연결됨", color = ForestGreen, fontWeight = FontWeight.Bold)
                }
            }
            if (!connected && showAction) {
                ProviderConnectionButton(
                    provider = provider,
                    isLoading = isLoading,
                    onClick = onClick,
                    enabled = enabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("providers-${provider.pathValue}-link")
                )
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
    val darkTheme = isSystemInDarkTheme()
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
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.48f),
            disabledContentColor = contentColor.copy(alpha = 0.48f)
        ),
        border = border,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(22.dp),
                    color = contentColor,
                    strokeWidth = 2.dp
                )
            }
            Text(
                text = if (isLoading) "연결하고 있어요" else "${provider.displayName()} 연결",
                fontWeight = FontWeight.ExtraBold
            )
        }
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
        val darkTheme = isSystemInDarkTheme()
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

private fun AuthProvider.connectionHint(): String = when (this) {
    AuthProvider.EMAIL -> "새 이메일과 비밀번호가 필요해요"
    else -> "추가 로그인 수단으로 연결할 수 있어요"
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
private fun SettingsDangerRow(action: SettingsMockAction, onClick: (SettingsMockAction) -> Unit) {
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
    action: SettingsMockAction,
    isPerforming: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
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
                    text = action.dialogBody,
                    style = MaterialTheme.typography.bodyMedium
                )
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

private enum class SettingsMockAction(
    val rowTitle: String,
    val rowValue: String?,
    val dialogTitle: String,
    val dialogBody: String,
    val confirmLabel: String = "닫기",
    val danger: Boolean = false
) {
    Theme(
        rowTitle = "테마",
        rowValue = "시스템 기본",
        dialogTitle = "테마 설정",
        dialogBody = "시스템 설정에 맞춰 밝은 화면과 어두운 화면을 자동으로 전환해요."
    ),
    Language(
        rowTitle = "언어",
        rowValue = "한국어",
        dialogTitle = "언어 설정",
        dialogBody = "현재는 한국어 기준으로 표시되고, 지역 안내 문구도 같은 언어 기준을 따라가요."
    ),
    LoginMethod(
        rowTitle = "로그인 방식",
        rowValue = "카카오",
        dialogTitle = "로그인 방식",
        dialogBody = "카카오 계정으로 연결된 상태예요. 계정 연결과 해제는 이곳에서 관리해요."
    ),
    BlockedUsers(
        rowTitle = "차단한 사용자",
        rowValue = "2명",
        dialogTitle = "차단한 사용자",
        dialogBody = "차단 목록 2명을 확인하고 필요하면 차단을 해제할 수 있어요."
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
        rowValue = "1.0.4 (최신)",
        dialogTitle = "앱 버전",
        dialogBody = "현재 설치된 버전은 1.0.4이며 최신 상태예요."
    ),
    Contact(
        rowTitle = "문의하기",
        rowValue = null,
        dialogTitle = "문의하기",
        dialogBody = "채팅, 모집, 결제 문의를 남기는 고객센터로 이어져요."
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

@Composable
private fun MascotCircle(text: String, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = (size * 0.46f).sp)
    }
}
