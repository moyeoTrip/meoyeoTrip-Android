package kr.hanchae.moyeotrip.ui.navigation

import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.BuildConfig
import kr.hanchae.moyeotrip.data.ServerDataDependencies
import kr.hanchae.moyeotrip.data.auth.AuthDependencies
import kr.hanchae.moyeotrip.data.network.AndroidNetworkMonitor
import kr.hanchae.moyeotrip.data.network.OfflineCacheStore
import kr.hanchae.moyeotrip.data.network.OfflineExperience
import kr.hanchae.moyeotrip.data.network.offlineExperience
import kr.hanchae.moyeotrip.data.oss.OssLicenseCatalog
import kr.hanchae.moyeotrip.data.settings.ThemePreference
import kr.hanchae.moyeotrip.data.settings.ThemePreferenceStore
import kr.hanchae.moyeotrip.data.settings.resolveDarkTheme
import kr.hanchae.moyeotrip.data.social.DexCompanion
import kr.hanchae.moyeotrip.data.tourism.HttpTourismContentRepository
import kr.hanchae.moyeotrip.data.tourism.TourismContentRepository
import kr.hanchae.moyeotrip.domain.auth.SignupGateStage
import kr.hanchae.moyeotrip.notifications.PushNavigationEvent
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.LocalCaptureMode
import kr.hanchae.moyeotrip.ui.components.LocalMapCaptureMode
import kr.hanchae.moyeotrip.ui.components.ServerListState
import kr.hanchae.moyeotrip.ui.screens.AccountDeleteScreen
import kr.hanchae.moyeotrip.ui.screens.AccountProvidersScreen
import kr.hanchae.moyeotrip.ui.screens.ApplyCancelScreen
import kr.hanchae.moyeotrip.ui.screens.AttachMapScreen
import kr.hanchae.moyeotrip.ui.screens.AttachNoticeScreen
import kr.hanchae.moyeotrip.ui.screens.AttachPhotoScreen
import kr.hanchae.moyeotrip.ui.screens.AttachPlaceScreen
import kr.hanchae.moyeotrip.ui.screens.AttachPollScreen
import kr.hanchae.moyeotrip.ui.screens.AttachSettlementScreen
import kr.hanchae.moyeotrip.ui.screens.AuthFlowScreen
import kr.hanchae.moyeotrip.ui.screens.BlockedUsersScreen
import kr.hanchae.moyeotrip.ui.screens.ChatAttachmentScreen
import kr.hanchae.moyeotrip.ui.screens.ChatListScreen
import kr.hanchae.moyeotrip.ui.screens.ChatMenuScreen
import kr.hanchae.moyeotrip.ui.screens.ChatMenuSheet
import kr.hanchae.moyeotrip.ui.screens.ChatRoomScreen
import kr.hanchae.moyeotrip.ui.screens.CommentEditScreen
import kr.hanchae.moyeotrip.ui.screens.CourseDetailScreen
import kr.hanchae.moyeotrip.ui.screens.CoursePublishScreen
import kr.hanchae.moyeotrip.ui.screens.CourseRatingScreen
import kr.hanchae.moyeotrip.ui.screens.CourseRouteScreen
import kr.hanchae.moyeotrip.ui.screens.CourseTitleEditScreen
import kr.hanchae.moyeotrip.ui.screens.CreateDetailScreen
import kr.hanchae.moyeotrip.ui.screens.CreateMeetPointScreen
import kr.hanchae.moyeotrip.ui.screens.CreatePeopleScreen
import kr.hanchae.moyeotrip.ui.screens.CreateScheduleScreen
import kr.hanchae.moyeotrip.ui.screens.CreateSummaryScreen
import kr.hanchae.moyeotrip.ui.screens.CustomCourseScreen
import kr.hanchae.moyeotrip.ui.screens.CustomerCenterScreen
import kr.hanchae.moyeotrip.ui.screens.ExploreScreen
import kr.hanchae.moyeotrip.ui.screens.FavoriteRoomsScreen
import kr.hanchae.moyeotrip.ui.screens.FeedActionsScreen
import kr.hanchae.moyeotrip.ui.screens.FeedCommentsScreen
import kr.hanchae.moyeotrip.ui.screens.FeedDeleteScreen
import kr.hanchae.moyeotrip.ui.screens.FeedDetailScreen
import kr.hanchae.moyeotrip.ui.screens.FeedEditScreen
import kr.hanchae.moyeotrip.ui.screens.FeedScreen
import kr.hanchae.moyeotrip.ui.screens.FeedWriteScreen
import kr.hanchae.moyeotrip.ui.screens.FriendDexScreen
import kr.hanchae.moyeotrip.ui.screens.FriendManageScreen
import kr.hanchae.moyeotrip.ui.screens.FriendsScreen
import kr.hanchae.moyeotrip.ui.screens.HomeScreen
import kr.hanchae.moyeotrip.ui.screens.HostManageScreen
import kr.hanchae.moyeotrip.ui.screens.KickHistoryScreen
import kr.hanchae.moyeotrip.ui.screens.MeetingChatTab
import kr.hanchae.moyeotrip.ui.screens.MeetingEditScreen
import kr.hanchae.moyeotrip.ui.screens.MeetingsScreen
import kr.hanchae.moyeotrip.ui.screens.MessageDeleteScreen
import kr.hanchae.moyeotrip.ui.screens.MyFeedScreen
import kr.hanchae.moyeotrip.ui.screens.MyScreen
import kr.hanchae.moyeotrip.ui.screens.NoticeEditScreen
import kr.hanchae.moyeotrip.ui.screens.NoticeHistoryScreen
import kr.hanchae.moyeotrip.ui.screens.NotificationCenterScreen
import kr.hanchae.moyeotrip.ui.screens.NotificationDetailScreen
import kr.hanchae.moyeotrip.ui.screens.OVERLAY_BACKDROP_THREAD_ID
import kr.hanchae.moyeotrip.ui.screens.OfflineCachedBanner
import kr.hanchae.moyeotrip.ui.screens.OfflineNoCacheScreen
import kr.hanchae.moyeotrip.ui.screens.OssLicenseDetailScreen
import kr.hanchae.moyeotrip.ui.screens.OssLicensesScreen
import kr.hanchae.moyeotrip.ui.screens.PlaceDetailScreen
import kr.hanchae.moyeotrip.ui.screens.PlaceSearchScreen
import kr.hanchae.moyeotrip.ui.screens.ProfileCardScreen
import kr.hanchae.moyeotrip.ui.screens.ProfileEditScreen
import kr.hanchae.moyeotrip.ui.screens.QaComponentStatesScreen
import kr.hanchae.moyeotrip.ui.screens.QaDesignSystemOverviewScreen
import kr.hanchae.moyeotrip.ui.screens.QaLeaveAlertScreen
import kr.hanchae.moyeotrip.ui.screens.RecruitEditScreen
import kr.hanchae.moyeotrip.ui.screens.RecruitmentCourseSourceScreen
import kr.hanchae.moyeotrip.ui.screens.RemovalReasonScreen
import kr.hanchae.moyeotrip.ui.screens.ReportScreen
import kr.hanchae.moyeotrip.ui.screens.RoomNotificationScreen
import kr.hanchae.moyeotrip.ui.screens.SearchResultsScreen
import kr.hanchae.moyeotrip.ui.screens.SearchScreen
import kr.hanchae.moyeotrip.ui.screens.SettingsScreen
import kr.hanchae.moyeotrip.ui.screens.SpecialMessagesScreen
import kr.hanchae.moyeotrip.ui.screens.StartupSplashScreen
import kr.hanchae.moyeotrip.ui.screens.SystemNoticeMode
import kr.hanchae.moyeotrip.ui.screens.SystemNoticeScreen
import kr.hanchae.moyeotrip.ui.screens.TermsDetailScreen
import kr.hanchae.moyeotrip.ui.screens.TripConfirmedScreen
import kr.hanchae.moyeotrip.ui.screens.TripDayScreen
import kr.hanchae.moyeotrip.ui.screens.TripDetailScreen
import kr.hanchae.moyeotrip.ui.screens.TripMessageScreen
import kr.hanchae.moyeotrip.ui.screens.TripStatusScreen
import kr.hanchae.moyeotrip.ui.screens.UnblockConfirmScreen
import kr.hanchae.moyeotrip.ui.screens.serverFeedIdOrZero
import kr.hanchae.moyeotrip.ui.screens.serverRoomIdOrNull
import kr.hanchae.moyeotrip.ui.state.LocalTabDataStore
import kr.hanchae.moyeotrip.ui.state.TabDataStore
import kr.hanchae.moyeotrip.ui.theme.MoyeoTripTheme

private data class BottomDestination(val tab: BottomTab, val icon: ImageVector) {
    val route: String = tab.route
    val label: String = tab.label
}

private val bottomDestinations = listOf(
    BottomDestination(BottomTab.Home, Icons.Filled.Home),
    BottomDestination(BottomTab.Explore, Icons.Filled.Search),
    BottomDestination(BottomTab.Meetings, Icons.Filled.Groups),
    BottomDestination(BottomTab.Feed, Icons.AutoMirrored.Filled.Article),
    BottomDestination(BottomTab.My, Icons.Filled.Person)
)

/**
 * 현재 라우트가 어떤 하단 탭에 속하는지. 탭 화면의 변형 라우트(모임 목록의 신청중 세그먼트,
 * 캡처용 채팅 목록)도 화면기획에서는 같은 탭 안에 있으므로 탭바가 보여야 한다.
 */
private fun bottomTabRouteFor(currentRoute: String?): String? = when (currentRoute) {
    AppRoutes.MEETINGS_APPLIED, AppRoutes.CHAT_LIST -> AppRoutes.MEETINGS
    else -> bottomDestinations.firstOrNull { it.route == currentRoute }?.route
}

private const val STARTUP_SPLASH_HOLD_MILLIS = 1_150L
private const val STARTUP_SPLASH_TRANSITION_MILLIS = 420

/**
 * 캡처 라우트가 넘긴 식별자를 피드 화면이 아는 형태로 바꾼다.
 *
 * 숫자면 실서버 피드 ID 다 — 화면은 `srv-{id}` 접두사로만 실서버 상세를 연다.
 * 그 밖의 값(목 ID)은 그대로 둔다.
 */
internal fun serverFeedRoute(identifier: String?): String? {
    val value = identifier?.trim().orEmpty()
    if (value.isEmpty()) return null
    return if (value.toLongOrNull() != null) "srv-$value" else value
}

/**
 * 실서버 의존성(`LocalServerData`)을 만들지 판정한다.
 *
 * 캡처 라우트(`moyeo_screen` 단독)와 인증 우회 실행은 네트워크를 타지 않는다.
 * 라이브 캡처(`moyeo_live_data`)는 **그 차단만** 푼다.
 */
internal fun injectsServerData(startScreen: String?, skipAuthentication: Boolean, liveCapture: Boolean): Boolean {
    if (liveCapture) return true
    return startScreen == null && !skipAuthentication
}

/**
 * 35·36·37 의 강제 오프라인 플래그(`moyeo_screen=offline|offlineCached|offlineChat`)는
 * **라이브 캡처에서도** 그대로 이긴다.
 *
 * 예전에는 라이브 캡처에서 이 플래그를 버리고 실제 연결 상태만 따랐다. 그런데 라이브 캡처는
 * 서버를 타야 하므로 네트워크를 켠 채 돌린다 — 그래서 35·36 자리에 오프라인 표시가 없는
 * **홈 화면**이 찍혀 그대로 PDF 에 실렸다. iOS 도 `UITEST_OFFLINE_EMPTY`·`UITEST_OFFLINE_CACHED`
 * 를 라이브 여부와 무관하게 적용한다 — 세 플랫폼이 같은 화면을 찍어야 한다.
 *
 * 플래그가 없는 실행에서는 예전과 같이 실제 연결 상태를 그린다.
 */
internal fun resolveNetworkExperience(
    forcedOverride: OfflineExperience?,
    detectedOnline: Boolean,
    hasCachedContent: Boolean
): OfflineExperience = forcedOverride ?: offlineExperience(detectedOnline, hasCachedContent)

@Composable
fun MoyeoTripApp(
    startScreen: String? = null,
    /**
     * 라이브 캡처(`moyeo_live_data`, 디버그 전용). 캡처 라우팅은 그대로 두고 **데이터 차단만** 푼다.
     * false 면 기존 캡처 경로와 100% 동일하다.
     */
    liveData: Boolean = false,
    pushNavigationEvent: PushNavigationEvent? = null,
    skipStartupSplash: Boolean = false,
    skipAuthentication: Boolean = false,
    /** QA 세션이 주입된 실행(디버그 전용). 인증 플로우를 거치지 않고 바로 로그인 상태로 시작한다. */
    qaSessionInjected: Boolean = false,
    /** 번호별 비교 캡처는 다크/라이트 두 테마를 모두 찍는다. null이면 사용자 설정·시스템 설정을 따른다. */
    forceDarkTheme: Boolean? = null,
    onAuthenticationComplete: () -> Unit = {},
    onPushRouteHandled: (Long) -> Unit = {},
    /** 실제 적용된 테마를 액티비티에 알려 상태바 아이콘과 리소스 한정자(uiMode)를 맞춘다. */
    onEffectiveDarkThemeChanged: (Boolean) -> Unit = {}
) {
    val themeContext = LocalContext.current
    val captureMode = startScreen != null
    // 릴리스 빌드에서는 어떤 경로로도 켜지지 않게 한 번 더 잠근다.
    val liveCapture = liveData && BuildConfig.DEBUG
    val themePreferenceStore = remember(themeContext) {
        ThemePreferenceStore(themeContext.applicationContext)
    }
    val storedThemePreference by themePreferenceStore.preference.collectAsState()
    // 캡처는 강제 테마(없으면 시스템)를 쓴다 — 기기에 저장된 사용자 설정이 섞이면 비교 캡처가 깨진다
    val themePreference = if (captureMode) ThemePreference.System else storedThemePreference
    // 사용자 설정이 uiMode 를 덮어쓴 뒤에도 "진짜 시스템 값"을 읽어야 한다.
    // 액티비티 리소스만 덮어쓰므로 애플리케이션 리소스가 시스템 원본이다.
    // OS 테마가 런타임에 바뀌면 액티비티가 재생성되면서 이 값도 다시 읽힌다.
    val systemDarkTheme = LocalConfiguration.current.let { configuration ->
        remember(configuration) {
            themeContext.applicationContext.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        }
    }
    val darkTheme = resolveDarkTheme(forceDarkTheme, themePreference, systemDarkTheme)
    LaunchedEffect(darkTheme) { onEffectiveDarkThemeChanged(darkTheme) }

    MoyeoTripTheme(darkTheme = darkTheme) {
        val currentDensity = LocalDensity.current
        val context = LocalContext.current
        CompositionLocalProvider(
            LocalDensity provides Density(currentDensity.density, fontScale = 1f),
            // 캡처(moyeo_screen)에서는 실지도를 만들지 않는다 — 타일 로딩이 비결정적이라
            // 번호별 비교 캡처가 깨진다. 그때는 지도 자리에 "표시할 수 없음"만 남는다.
            //
            // **라이브 캡처는 예외다.** 실데이터를 보려고 찍는 캡처에서 지도가 비면
            // "지도가 되는지"를 확인할 수 없다.
            LocalMapCaptureMode provides (captureMode && !liveCapture),
            // 라이브 캡처에서도 움직임은 멈춘다.
            LocalCaptureMode provides captureMode
        ) {
            var showStartupSplash by remember { mutableStateOf(!skipStartupSplash) }
            val qaStartRequest = remember(startScreen) { QaStartRequest.parse(startScreen) }
            val networkMonitor = remember(context) { AndroidNetworkMonitor(context.applicationContext) }
            DisposableEffect(networkMonitor) {
                onDispose(networkMonitor::close)
            }
            val cacheStore = remember(context) { OfflineCacheStore(context.applicationContext) }
            val detectedOnline by networkMonitor.isOnline.collectAsState()
            LaunchedEffect(detectedOnline) {
                if (detectedOnline) cacheStore.recordSuccessfulLoad()
            }
            val networkExperience = resolveNetworkExperience(
                forcedOverride = qaStartRequest.offlineExperienceOverride,
                detectedOnline = detectedOnline,
                hasCachedContent = cacheStore.hasCachedContent
            )
            val isOnline = networkExperience == OfflineExperience.Online
            val authDependencies = remember(context) { AuthDependencies.appDefault(context) }
            val appScope = rememberCoroutineScope()
            val storedUserProfile by authDependencies.userProfileStore.profile.collectAsState()
            // 프로필 카드(25)가 누구의 카드인지. 도감에서 눌러 들어오면 그 동행자 정보를 그대로 넘겨
            // 카드 앞면의 '나와 N회 동행'과 뒷면의 '함께한 여행'을 추가 호출 없이 채운다.
            var profileCardTarget by remember { mutableStateOf<DexCompanion?>(null) }
            // 표시용 프로필은 캡처에서도 기기에 저장된 실제 값이다 —
            // 캡처 전용으로 빈 프로필을 끼워 넣으면 캡처가 실제 화면과 달라진다.
            val userProfile = storedUserProfile
            val bypassAuthentication = skipAuthentication || startScreen != null
            var authenticationComplete by remember(authDependencies) {
                // 라이브 캡처는 인증 UI 를 거치지 않으므로 이미 심어진 세션을 로그인 완료로 본다
                // (`LocalServerData` 는 로그인 완료 상태에서만 내려간다).
                val liveSessionReady = liveCapture && authDependencies.sessionStore.current.accessToken != null
                mutableStateOf(qaSessionInjected || liveSessionReady)
            }
            // 서버가 "가입이 아직 안 끝났다"(409 40902·40918)고 막으면 토큰을 다시 받아봐야 소용없다.
            // 세션 자체는 유효하므로 지우지 않고 가입 플로우를 다시 연다 —
            // 어느 단계로 갈지는 AuthFlowScreen 의 restoreSession 이 서버 signupState 로 정한다(정본 R3).
            val returnToSignup: (SignupGateStage) -> Unit = { stage ->
                Log.i("MoyeoAuth", "가입 미완료로 일반 API 차단 (${stage.name}) — 가입 단계로 복귀")
                authenticationComplete = false
            }
            // 방문지 검색은 캡처에서도 실서버(TourAPI 프록시)를 그대로 탄다 —
            // 캡처 전용 예시 방문지를 끼워 넣으면 "검색이 되는지"를 확인할 수 없다.
            val tourismRepository = remember(authDependencies) {
                HttpTourismContentRepository(
                    baseUrl = BuildConfig.AUTH_API_BASE_URL,
                    accessToken = { authDependencies.sessionStore.current.accessToken },
                    onSignupGate = { stage -> returnToSignup(stage) }
                )
            }
            // 캡처 라우트(startScreen)와 인증 우회 실행에서는 아예 만들지 않는다 — 네트워크 회귀 금지.
            // 라이브 캡처는 이 차단만 푼다(라우팅·강제 테마는 캡처와 동일).
            val serverDataDependencies = remember(authDependencies, startScreen, liveCapture) {
                if (!injectsServerData(startScreen, skipAuthentication, liveCapture)) {
                    null
                } else {
                    ServerDataDependencies.create(
                        baseUrl = BuildConfig.AUTH_API_BASE_URL,
                        accessToken = { authDependencies.sessionStore.current.accessToken },
                        refreshAccessToken = {
                            val session = authDependencies.sessionStore.current
                            val refreshToken = session.refreshToken
                            val provider = session.provider
                            if (refreshToken == null || provider == null) {
                                null
                            } else {
                                runCatching { authDependencies.authGateway.refresh(refreshToken) }
                                    .onSuccess { authDependencies.sessionStore.saveSignup(provider, it) }
                                    .getOrNull()
                                    ?.accessToken
                            }
                        },
                        onSignupGate = returnToSignup
                    )
                }
            }
            // 탭 데이터는 탭 화면 바깥(여기)에 둔다 — 탭을 오갈 때마다 재조회하지 않기 위해서다(정본 R1).
            val tabDataStore = remember(authDependencies) { TabDataStore() }
            LaunchedEffect(tabDataStore, authenticationComplete) {
                // 로그아웃·계정 전환·가입 게이트 복귀에서 보관소를 비운다 —
                // 다른 사용자의 목록이 남아 있으면 안 된다(정본 R4).
                if (!authenticationComplete) tabDataStore.clear()
            }
            LaunchedEffect(authDependencies, authenticationComplete) {
                if (authenticationComplete) {
                    onAuthenticationComplete()
                    authDependencies.sessionStore.current.accessToken?.let { accessToken ->
                        authDependencies.userProfileStore.updateFromAccessToken(accessToken)
                        runCatching { authDependencies.authGateway.profileImages(accessToken) }
                            .getOrNull()
                            ?.candidates
                            ?.firstOrNull { it.selected }
                            ?.profileImageUrl
                            ?.let(authDependencies.userProfileStore::saveProfileImage)
                    }
                }
            }
            val startupContentAlpha by animateFloatAsState(
                targetValue = if (showStartupSplash) 0f else 1f,
                animationSpec = tween(
                    durationMillis = STARTUP_SPLASH_TRANSITION_MILLIS,
                    easing = FastOutSlowInEasing
                ),
                label = "startupContentAlpha"
            )
            val startupContentScale by animateFloatAsState(
                targetValue = if (showStartupSplash) 0.985f else 1f,
                animationSpec = tween(
                    durationMillis = STARTUP_SPLASH_TRANSITION_MILLIS,
                    easing = FastOutSlowInEasing
                ),
                label = "startupContentScale"
            )
            val startupContentOffset by animateDpAsState(
                targetValue = if (showStartupSplash) 12.dp else 0.dp,
                animationSpec = tween(
                    durationMillis = STARTUP_SPLASH_TRANSITION_MILLIS,
                    easing = FastOutSlowInEasing
                ),
                label = "startupContentOffset"
            )
            val qaStartRoute = remember(qaStartRequest) { qaStartRequest.toRoute() }
            val qaStartsInExploreMap = qaStartRequest.startsInExploreMap
            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            // 19·19-1은 화면기획에서 "모임" 탭 화면이다 — 세그먼트만 다른 라우트에서도 탭바를 유지한다
            val bottomBarRoute = bottomTabRouteFor(currentRoute)
            val showBottomBar = bottomBarRoute != null
            // 오프라인 배너가 떠 있으면 **상태바는 배너가 이미 차지한다**(배너에 statusBarsPadding 이 붙어 있고,
            // Scaffold 가 배너 높이만큼 innerPadding 을 준다). 여기서 상태바 인셋을 또 더하면
            // 배너와 화면 사이가 상태바 하나만큼 벌어진다 (사용자 지적, 2026-09-06 · 안드 37).
            val offlineBannerVisible = networkExperience == OfflineExperience.Cached
            val topSafePadding = if (offlineBannerVisible) {
                12.dp
            } else {
                WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 12.dp
            }

            LaunchedEffect(skipStartupSplash) {
                if (!skipStartupSplash) {
                    delay(STARTUP_SPLASH_HOLD_MILLIS.milliseconds)
                    showStartupSplash = false
                }
            }

            LaunchedEffect(qaStartRoute) {
                val targetRoute = qaStartRoute
                if (targetRoute != null && targetRoute != AppRoutes.HOME) {
                    navController.navigate(targetRoute) {
                        popUpTo(AppRoutes.HOME) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            }

            LaunchedEffect(pushNavigationEvent?.id, authenticationComplete, bypassAuthentication) {
                val pushEvent = pushNavigationEvent
                if (pushEvent != null && (authenticationComplete || bypassAuthentication)) {
                    val targetRoute = QaStartRequest.parse(pushEvent.route).toRoute() ?: AppRoutes.HOME
                    navController.navigate(targetRoute) {
                        popUpTo(AppRoutes.HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                    onPushRouteHandled(pushEvent.id)
                }
            }

            CompositionLocalProvider(
                LocalServerData provides serverDataDependencies?.takeIf { authenticationComplete },
                LocalTabDataStore provides tabDataStore
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.graphicsLayer {
                            alpha = startupContentAlpha
                            scaleX = startupContentScale
                            scaleY = startupContentScale
                            translationY = with(currentDensity) { startupContentOffset.toPx() }
                        },
                        containerColor = MaterialTheme.colorScheme.background,
                        contentWindowInsets = WindowInsets(0.dp),
                        topBar = {
                            if (networkExperience == OfflineExperience.Cached) {
                                // 채팅방도 이 배너를 쓴다. 예전에는 채팅에서만 감추고
                                // 화면 안에 전용 배너를 따로 그렸는데, **오프라인 안내는
                                // 앱 전역 배너 하나로 통일**하기로 했다 (2026-09-06 사용자 결정).
                                // iOS 는 두 배너가 겹쳐 「연결이 끊겼어요」가 두 번 나왔다.
                                // Scaffold 가 인셋을 소비하지 않으므로 배너가 상태바와 겹친다
                                OfflineCachedBanner(modifier = Modifier.statusBarsPadding())
                            }
                        },
                        bottomBar = {
                            if (showBottomBar) {
                                MoyeoBottomBar(
                                    currentRoute = bottomBarRoute,
                                    onDestinationClick = { destination ->
                                        navController.navigate(destination.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        // 스플래시는 iOS처럼 상태바 뒤까지 꽉 채운다 — 상단 안전 패딩을 주지 않는다
                        val edgeToEdgeRoute = currentRoute == AppRoutes.QA_SPLASH
                        NavHost(
                            navController = navController,
                            startDestination = AppRoutes.HOME,
                            modifier = Modifier
                                .padding(innerPadding)
                                .padding(top = if (edgeToEdgeRoute) 0.dp else topSafePadding)
                                // 상태바 인셋은 **여기서 다 썼다**. 소비해 두지 않으면 화면 안에서
                                // `statusBarsPadding()` 을 다시 붙일 때 상태바 높이만큼 또 밀려
                                // 제목 위에 빈 띠가 생긴다 (18-7·29-5 안드로이드만 상단이 남던 원인,
                                // 사용자 지적 2026-09-09). 스플래시는 상태바 뒤까지 채우니 그대로 둔다.
                                .then(
                                    if (edgeToEdgeRoute) {
                                        Modifier
                                    } else {
                                        Modifier.consumeWindowInsets(WindowInsets.statusBars)
                                    }
                                )
                        ) {
                            composable(AppRoutes.HOME) {
                                HomeScreen(
                                    onOpenCourse = { navController.navigate(AppRoutes.courseDetail(it)) },
                                    onOpenExplore = { navController.navigate(AppRoutes.EXPLORE) },
                                    onOpenNotifications = { navController.navigate(AppRoutes.NOTIFICATIONS) },
                                    onCreateRecruitment = { navController.navigate(AppRoutes.createRecruitment(it)) },
                                    isOnline = isOnline
                                )
                            }
                            composable(AppRoutes.EXPLORE) {
                                ExploreScreen(
                                    onOpenCourse = { navController.navigate(AppRoutes.courseDetail(it)) },
                                    onOpenSearch = { navController.navigate(AppRoutes.SEARCH) },
                                    onCreateRecruitment = { navController.navigate(AppRoutes.createRecruitment(it)) },
                                    startInMap = qaStartsInExploreMap,
                                    onOpenRoom = { roomId ->
                                        navController.navigate(AppRoutes.tripDetail("room-$roomId"))
                                    }
                                )
                            }
                            composable(AppRoutes.MEETINGS) {
                                MeetingsScreen(
                                    onOpenRoom = { navController.navigate(AppRoutes.chatRoom(it)) },
                                    onOpenTrip = { navController.navigate(AppRoutes.tripDetail(it)) },
                                    onOpenSpecialMessages = { navController.navigate(AppRoutes.specialMessages()) },
                                    onOpenApplyCancel = { navController.navigate(AppRoutes.applyCancel(it)) }
                                )
                            }
                            composable(AppRoutes.MEETINGS_APPLIED) {
                                MeetingsScreen(
                                    onOpenRoom = { navController.navigate(AppRoutes.chatRoom(it)) },
                                    onOpenTrip = { navController.navigate(AppRoutes.tripDetail(it)) },
                                    onOpenSpecialMessages = { navController.navigate(AppRoutes.specialMessages()) },
                                    onOpenApplyCancel = { navController.navigate(AppRoutes.applyCancel(it)) },
                                    initialTab = MeetingChatTab.Applied
                                )
                            }
                            composable(AppRoutes.FEED) {
                                FeedScreen(
                                    onOpenPost = { navController.navigate(AppRoutes.feedDetail(it)) },
                                    onWritePost = { navController.navigate(AppRoutes.feedWrite()) },
                                    onOpenReport = { navController.navigate(AppRoutes.report(it)) }
                                )
                            }
                            composable(AppRoutes.MY) {
                                MyScreen(
                                    userProfile = userProfile,
                                    onOpenTrip = { navController.navigate(AppRoutes.tripDetail(it)) },
                                    // 내 카드로는 열지 못한다 — GET /users/me/profile 응답에 userId 가 없어
                                    // 공개 프로필 API 를 내 계정으로 호출할 수 없다(BE 에 요청함).
                                    // 그래서 내 프로필 요약은 28 프로필 수정으로 보낸다.
                                    onOpenProfile = { navController.navigate(AppRoutes.PROFILE_EDIT) },
                                    onOpenMyFeed = { navController.navigate(AppRoutes.MY_FEED) },
                                    onOpenFriendDex = { navController.navigate(AppRoutes.FRIEND_DEX) },
                                    onOpenSettings = { navController.navigate(AppRoutes.SETTINGS) },
                                    onOpenCustomerCenter = { navController.navigate(AppRoutes.CUSTOMER_CENTER) },
                                    onOpenFriends = { navController.navigate(AppRoutes.FRIENDS) },
                                    onOpenCourse = { navController.navigate(AppRoutes.courseDetail(it)) }
                                )
                            }
                            composable(AppRoutes.PROFILE) {
                                // 25 는 프로필 카드다. 카드 자체가 그 유저의 프로필이므로
                                // 도감·피드 작성자·멤버 시트가 모두 이 화면으로 온다.
                                // 관리 진입점(내 정보 수정·친구 관리)은 마이(26)로 옮겼다.
                                ProfileCardScreen(
                                    userId = profileCardTarget?.userId ?: qaStartRequest.profileUserId,
                                    dexCompanion = profileCardTarget,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(AppRoutes.PROFILE_CARD_BACK) {
                                // 25-1 캡처 전용 — 뒤집힌 상태로 연다
                                ProfileCardScreen(
                                    userId = profileCardTarget?.userId ?: qaStartRequest.profileUserId,
                                    dexCompanion = profileCardTarget,
                                    onBack = { navController.popBackStack() },
                                    initialFlipped = true
                                )
                            }
                            composable(AppRoutes.PROFILE_EDIT) {
                                ProfileEditScreen(
                                    userProfile = userProfile,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(AppRoutes.MY_FEED) {
                                MyFeedScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenPost = { navController.navigate(AppRoutes.feedDetail(it)) }
                                )
                            }
                            composable(AppRoutes.FRIEND_DEX) {
                                FriendDexScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenCompanion = { companion ->
                                        profileCardTarget = companion
                                        navController.navigate(AppRoutes.PROFILE)
                                    }
                                )
                            }
                            composable(AppRoutes.SETTINGS) {
                                SettingsScreen(
                                    onBack = { navController.popBackStack() },
                                    accountService = authDependencies.accountService,
                                    themePreference = themePreference,
                                    onCycleThemePreference = {
                                        if (!captureMode) {
                                            themePreferenceStore.save(themePreferenceStore.current.next())
                                        }
                                    },
                                    onOpenOssLicenses = { navController.navigate(AppRoutes.OSS_LICENSES) },
                                    onOpenNotificationDetail = {
                                        navController.navigate(AppRoutes.NOTIFICATION_DETAIL)
                                    },
                                    onOpenBlockedUsers = { navController.navigate(AppRoutes.BLOCKED_USERS) },
                                    onOpenAccountProviders = { navController.navigate(AppRoutes.ACCOUNT_PROVIDERS) },
                                    onOpenKickHistory = { navController.navigate(AppRoutes.KICK_HISTORY) },
                                    onOpenAccountDelete = { navController.navigate(AppRoutes.ACCOUNT_DELETE) },
                                    onOpenTerms = { document ->
                                        navController.navigate(AppRoutes.termsDetail(document, "settings"))
                                    },
                                    onAuthenticationCleared = {
                                        authenticationComplete = false
                                        navController.navigate(AppRoutes.HOME) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                inclusive = false
                                            }
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                            // 29-5 계정 연결 (정본 §6-1) — 설정 `로그인 방식 › 관리` 가 여기로 온다
                            composable(AppRoutes.ACCOUNT_PROVIDERS) {
                                AccountProvidersScreen(
                                    accountService = authDependencies.accountService,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(AppRoutes.CUSTOMER_CENTER) {
                                CustomerCenterScreen(onBack = { navController.popBackStack() })
                            }
                            composable(AppRoutes.OSS_LICENSES) {
                                OssLicensesScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenLicense = { slug ->
                                        navController.navigate(AppRoutes.ossLicenseDetail(slug))
                                    }
                                )
                            }
                            composable(AppRoutes.OSS_LICENSE_DETAIL) { entry ->
                                OssLicenseDetailScreen(
                                    slug = entry.arguments?.getString("slug").orEmpty(),
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(AppRoutes.COURSE_DETAIL) { entry ->
                                CourseDetailScreen(
                                    courseId = entry.arguments?.getString("courseId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    onCreateRecruitment = { navController.navigate(AppRoutes.createRecruitment(it)) },
                                    onOpenRoom = { navController.navigate(AppRoutes.tripDetail(it)) }
                                )
                            }
                            composable(AppRoutes.TRIP_DETAIL) { entry ->
                                TripDetailScreen(
                                    tripId = entry.arguments?.getString("tripId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    onOpenChatRoom = { navController.navigate(AppRoutes.chatRoom(it)) }
                                )
                            }
                            composable(AppRoutes.FEED_DETAIL) { entry ->
                                FeedDetailScreen(
                                    postId = entry.arguments?.getString("postId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    onOpenAllComments = {
                                        navController.navigate(
                                            AppRoutes.feedComments(entry.arguments?.getString("postId").orEmpty())
                                        )
                                    },
                                    // 30-2 피드 신고 — 서버가 접수하는 유일한 신고다(정본 §2)
                                    onOpenReport = { navController.navigate(AppRoutes.report(it)) }
                                )
                            }
                            composable(AppRoutes.CHAT_LIST) {
                                ChatListScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenRoom = { navController.navigate(AppRoutes.chatRoom(it)) }
                                )
                            }
                            composable(AppRoutes.CHAT_ROOM) { entry ->
                                ChatRoomScreen(
                                    threadId = entry.arguments?.getString("threadId").orEmpty(),
                                    isOnline = isOnline,
                                    onBack = { navController.popBackStack() },
                                    onOpenNotices = { navController.navigate(AppRoutes.noticeHistory(it)) },
                                    onOpenRoute = { navController.navigate(AppRoutes.courseRoute(it)) },
                                    onOpenMenu = {
                                        navController.navigate(
                                            AppRoutes.chatMenu(entry.arguments?.getString("threadId").orEmpty())
                                        )
                                    },
                                    onOpenAttachment = {
                                        navController.navigate(
                                            AppRoutes.chatAttach(entry.arguments?.getString("threadId"))
                                        )
                                    }
                                )
                            }
                            composable(
                                route = AppRoutes.SPECIAL_MESSAGES,
                                arguments = listOf(
                                    navArgument("threadId") {
                                        type = NavType.StringType
                                        nullable = true
                                        defaultValue = null
                                    }
                                )
                            ) { entry ->
                                // 방을 지정해 들어오면(`msgs:room-121`) 그 방의 특수 메시지만 그린다.
                                SpecialMessagesScreen(
                                    threadId = entry.arguments?.getString("threadId"),
                                    onBack = { navController.popBackStack() },
                                    onOpenTripConfirmed = { navController.navigate(AppRoutes.tripConfirmed()) },
                                    onOpenNotices = { navController.navigate(AppRoutes.noticeHistory(it)) }
                                )
                            }
                            composable(AppRoutes.NOTIFICATIONS) {
                                NotificationCenterScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenTrip = { navController.navigate(AppRoutes.tripDetail(it)) },
                                    onOpenPost = { navController.navigate(AppRoutes.feedDetail(it)) },
                                    onOpenCourse = { navController.navigate(AppRoutes.courseDetail(it)) },
                                    onOpenTripConfirmed = { navController.navigate(AppRoutes.tripConfirmed()) },
                                    onOpenTripMessage = { navController.navigate(AppRoutes.TRIP_MESSAGE) },
                                    onOpenRemovalReason = { notificationId ->
                                        navController.navigate(AppRoutes.removalReason(notificationId))
                                    }
                                )
                            }
                            composable(
                                route = AppRoutes.REMOVAL_REASON,
                                arguments = listOf(
                                    navArgument("notificationId") {
                                        type = NavType.LongType
                                        defaultValue = -1L
                                    }
                                )
                            ) { entry ->
                                RemovalReasonScreen(
                                    onBack = { navController.popBackStack() },
                                    notificationId = entry.arguments?.getLong("notificationId")?.takeIf { it >= 0 }
                                )
                            }
                            composable(
                                route = AppRoutes.CREATE_RECRUITMENT,
                                arguments = listOf(navArgument("courseId") { type = NavType.StringType })
                            ) { entry ->
                                RecruitmentCourseSourceScreen(
                                    courseId = entry.arguments?.getString("courseId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    onOpenCustomCourse = { navController.navigate(AppRoutes.customCourse(it)) },
                                    onOpenSchedule = { navController.navigate(AppRoutes.createSchedule(it)) }
                                )
                            }
                            composable(
                                route = AppRoutes.CUSTOM_COURSE,
                                arguments = listOf(
                                    navArgument("courseId") {
                                        type = NavType.LongType
                                        // 0 = 불러올 코스 없음(빈 에디터). 17-1 캡처만 코스 id 를 넘긴다.
                                        defaultValue = 0L
                                    }
                                )
                            ) { entry ->
                                CustomCourseScreen(
                                    draftId = entry.arguments?.getString("draftId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    onOpenPlaceSearch = { navController.navigate(AppRoutes.placeSearch(it)) },
                                    onContinue = { navController.navigate(AppRoutes.createSchedule(it)) },
                                    startingCourseId = entry.arguments?.getLong("courseId")?.takeIf { it > 0L }
                                )
                            }
                            composable(AppRoutes.PLACE_SEARCH) { entry ->
                                PlaceSearchScreen(
                                    draftId = entry.arguments?.getString("draftId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    onOpenDetail = { draftId, contentId ->
                                        navController.navigate(AppRoutes.placeDetail(draftId, contentId))
                                    },
                                    onDone = { draftId ->
                                        navController.navigate(AppRoutes.customCourse(draftId)) {
                                            popUpTo(AppRoutes.CUSTOM_COURSE) { inclusive = true }
                                        }
                                    },
                                    repository = tourismRepository
                                )
                            }
                            composable(AppRoutes.PLACE_DETAIL) { entry ->
                                PlaceDetailScreen(
                                    draftId = entry.arguments?.getString("draftId").orEmpty(),
                                    contentId = entry.arguments?.getString("contentId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    onAdd = { draftId ->
                                        navController.navigate(AppRoutes.customCourse(draftId)) {
                                            popUpTo(AppRoutes.PLACE_SEARCH) { inclusive = false }
                                        }
                                    },
                                    repository = tourismRepository
                                )
                            }
                            composable(AppRoutes.CREATE_SCHEDULE) { entry ->
                                CreateScheduleScreen(
                                    draftId = entry.arguments?.getString("draftId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    onContinue = { navController.navigate(AppRoutes.createMeetPoint(it)) }
                                )
                            }
                            composable(
                                route = AppRoutes.CREATE_PEOPLE,
                                arguments = listOf(
                                    navArgument("capacity") {
                                        type = NavType.IntType
                                        // 0 = 지정 없음. 17-4a/17-4b 캡처만 4·10 을 넘긴다.
                                        defaultValue = 0
                                    }
                                )
                            ) { entry ->
                                CreatePeopleScreen(
                                    draftId = entry.arguments?.getString("draftId").orEmpty(),
                                    initialCapacity = entry.arguments?.getInt("capacity")?.takeIf { it > 0 },
                                    onBack = { navController.popBackStack() },
                                    onContinue = { navController.navigate(AppRoutes.createDetail(it)) }
                                )
                            }
                            composable(AppRoutes.CREATE_MEET_POINT) { entry ->
                                CreateMeetPointScreen(
                                    draftId = entry.arguments?.getString("draftId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    onSave = { navController.navigate(AppRoutes.createPeople(it)) }
                                )
                            }
                            composable(AppRoutes.CREATE_DETAIL) { entry ->
                                CreateDetailScreen(
                                    draftId = entry.arguments?.getString("draftId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    onSave = { navController.navigate(AppRoutes.createSummary(it)) }
                                )
                            }
                            composable(AppRoutes.CREATE_SUMMARY) { entry ->
                                CreateSummaryScreen(
                                    draftId = entry.arguments?.getString("draftId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    // 방을 만들면 호스트는 18 모집 관리(신청 승인)로 들어간다
                                    onCreatedRoom = { roomId ->
                                        navController.navigate(AppRoutes.hostManage("room-$roomId")) {
                                            popUpTo(AppRoutes.CREATE_RECRUITMENT) { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable(AppRoutes.COURSE_ROUTE) { entry ->
                                CourseRouteScreen(
                                    tripId = entry.arguments?.getString("tripId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    // 「집합 정보 수정」 — 18-2 (PUT chat-rooms/{id}/meeting-info).
                                    // 생성 플로우의 17-3 이 아니다. 빈 람다라 눌러도 아무 일이 없었다.
                                    onOpenMeetingPoint = { navController.navigate(AppRoutes.meetingEdit(it)) },
                                    onOpenNotices = { navController.navigate(AppRoutes.noticeHistory(it)) }
                                )
                            }
                            composable(AppRoutes.NOTICE_HISTORY) { entry ->
                                NoticeHistoryScreen(
                                    tripId = entry.arguments?.getString("tripId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    // 20-3 하단 CTA 는 20-2f 공지 작성 화면으로 간다 (정본 §1)
                                    onOpenComposer = { navController.navigate(AppRoutes.attachNotice(it)) },
                                    // 20-3a 공지 수정 · 삭제 (정본 §6-1)
                                    onOpenNoticeEdit = { trip, noticeId ->
                                        navController.navigate(AppRoutes.noticeEdit(trip, noticeId))
                                    }
                                )
                            }
                            composable(
                                route = AppRoutes.HOST_MANAGE,
                                arguments = listOf(navArgument("tripId") { type = NavType.StringType })
                            ) { entry ->
                                HostManageScreen(
                                    tripId = entry.arguments?.getString("tripId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    onOpenChat = { navController.navigate(AppRoutes.chatRoom(it)) },
                                    onOpenRoute = { navController.navigate(AppRoutes.courseRoute(it)) },
                                    // 18-1 · 18-2 (정본 §6-1)
                                    onOpenTripStatus = { navController.navigate(AppRoutes.tripStatus(it)) },
                                    onOpenMeetingEdit = { navController.navigate(AppRoutes.meetingEdit(it)) }
                                )
                            }
                            composable(
                                route = AppRoutes.FEED_WRITE,
                                arguments = listOf(
                                    navArgument("step") {
                                        type = NavType.IntType
                                        defaultValue = 1
                                    },
                                    optionalArgument("roomId")
                                )
                            ) { entry ->
                                FeedWriteScreen(
                                    initialStep = entry.arguments?.getInt("step") ?: 1,
                                    requestedRoomId = entry.arguments?.getString("roomId")?.toLongOrNull(),
                                    onBack = { navController.popBackStack() },
                                    onPostCreated = { postId ->
                                        navController.navigate(AppRoutes.feedDetail(postId)) {
                                            popUpTo(AppRoutes.FEED) {
                                                inclusive = false
                                            }
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                            composable(AppRoutes.SEARCH) {
                                SearchScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenRoom = { navController.navigate(AppRoutes.tripDetail("room-$it")) },
                                    // 12-1 검색 결과 — 검색어를 확정하면 코스·모집 탭을 가진 결과 화면으로 간다
                                    onSubmitQuery = { navController.navigate(AppRoutes.searchResults(it)) }
                                )
                            }
                            composable(
                                route = AppRoutes.SEARCH_RESULTS,
                                arguments = listOf(
                                    navArgument("keyword") {
                                        type = NavType.StringType
                                        nullable = true
                                        defaultValue = null
                                    }
                                )
                            ) { entry ->
                                SearchResultsScreen(
                                    keyword = entry.arguments?.getString("keyword").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    onOpenCourse = { navController.navigate(AppRoutes.courseDetail("srv-$it")) },
                                    onOpenRoom = { navController.navigate(AppRoutes.tripDetail("room-$it")) }
                                )
                            }
                            composable(
                                AppRoutes.TRIP_CONFIRMED,
                                listOf(optionalArgument("tripId"))
                            ) { entry ->
                                TripConfirmedScreen(
                                    tripId = entry.arguments?.getString("tripId"),
                                    onBack = { navController.popBackStack() },
                                    onOpenChat = { roomId -> navController.navigate(AppRoutes.tripDay("room-$roomId")) }
                                )
                            }
                            composable(AppRoutes.CHAT_MENU) { entry ->
                                // 신고 시트는 배경에 이 방을 깔아야 한다 — 안 넘기면 배경이 빈 상태로 남는다.
                                val chatMenuThreadId = entry.arguments?.getString("threadId").orEmpty()
                                ChatMenuScreen(
                                    threadId = chatMenuThreadId,
                                    onBack = { navController.popBackStack() },
                                    onOpenSpecialMessages = { navController.navigate(AppRoutes.specialMessages()) },
                                    // 20-1c — "이 모임의 알림만 끄기" 가 전역 방해금지 화면으로 갔었다 (정본 §6-1)
                                    onOpenNotificationSettings = { threadId ->
                                        navController.navigate(AppRoutes.roomNotification(threadId))
                                    },
                                    onOpenNotices = { navController.navigate(AppRoutes.noticeHistory(it)) },
                                    onOpenRoute = { navController.navigate(AppRoutes.courseRoute(it)) },
                                    // 20-1 「모집 상세」 — 15 모집 상세로 간다.
                                    onOpenTrip = { navController.navigate(AppRoutes.tripDetail(it)) }
                                )
                            }
                            composable(
                                route = AppRoutes.CHAT_ATTACH,
                                arguments = listOf(
                                    navArgument("threadId") {
                                        type = NavType.StringType
                                        nullable = true
                                        defaultValue = null
                                    }
                                )
                            ) { entry ->
                                // 캡처 라우트는 threadId 없이 들어온다 — 그때는 배경 채팅방이 빈 상태다
                                val attachThreadId = entry.arguments?.getString("threadId")
                                ChatAttachmentScreen(
                                    onBack = { navController.popBackStack() },
                                    isOnline = isOnline,
                                    threadId = attachThreadId,
                                    // 20-2 타일 6개는 각자의 작성 화면으로 간다 (ATTACH-COMPOSER-CANON §0).
                                    // 예전에는 여섯 개 전부 21 특수 메시지 견본으로 갔다.
                                    onOpenComposer = { route -> navController.navigate(route) }
                                )
                            }
                            attachComposerDestinations(navController, tourismRepository)
                            gapDestinations(navController)
                            composable(AppRoutes.FRIENDS) {
                                FriendsScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenDex = { navController.navigate(AppRoutes.FRIEND_DEX) },
                                    // 27-2a 친구 정리 (정본 §6-5)
                                    onOpenFriendManage = { userId, nickname, subtitle ->
                                        navController.navigate(
                                            AppRoutes.friendManage(userId, nickname, subtitle)
                                        )
                                    }
                                )
                            }
                            composable(AppRoutes.TRIP_MESSAGE) {
                                TripMessageScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenFeedWrite = { navController.navigate(AppRoutes.feedWrite()) },
                                    onOpenCoursePublish = { navController.navigate(AppRoutes.COURSE_PUBLISH) },
                                    onOpenDex = { navController.navigate(AppRoutes.FRIEND_DEX) },
                                    // 27-4 코스 평가 — 27-1 이 유일한 진입점이다 (정본 §6-4)
                                    onOpenCourseRating = { navController.navigate(AppRoutes.courseRating(it)) }
                                )
                            }
                            composable(AppRoutes.REPORT, listOf(optionalArgument("feedId"))) { entry ->
                                // 30-2 는 피드 전용이다 — 대상 피드를 안 넘기면 신고할 것이 없다(정본 §2).
                                ReportScreen(
                                    onBack = { navController.popBackStack() },
                                    feedId = entry.arguments?.getString("feedId")?.toLongOrNull()
                                )
                            }
                            composable(AppRoutes.BLOCKED_USERS) {
                                BlockedUsersScreen(
                                    onBack = { navController.popBackStack() },
                                    // 29-1a — 차단 해제는 되돌리기 어려운 행동이라 확인을 먼저 지난다
                                    onOpenUnblockConfirm = { userId, nickname ->
                                        navController.navigate(AppRoutes.unblockConfirm(userId, nickname))
                                    }
                                )
                            }
                            composable(AppRoutes.COURSE_PUBLISH) {
                                CoursePublishScreen(
                                    onBack = { navController.popBackStack() },
                                    // 공개 등록 API 가 아직 없다 — 목록으로 돌아가는 것까지만 한다(§4 BE 요청)
                                    onPublished = { navController.popBackStack() }
                                )
                            }
                            composable(AppRoutes.TRIP_DAY) { entry ->
                                val threadId = entry.arguments?.getString("threadId").orEmpty()
                                TripDayScreen(
                                    threadId = threadId,
                                    onBack = { navController.popBackStack() },
                                    onOpenMenu = { navController.navigate(AppRoutes.chatMenu(threadId)) },
                                    onOpenAttachment = { navController.navigate(AppRoutes.chatAttach(threadId)) },
                                    onOpenRoute = { navController.navigate(AppRoutes.courseRoute(threadId)) }
                                )
                            }
                            composable(AppRoutes.NOTIFICATION_DETAIL) {
                                NotificationDetailScreen(onBack = { navController.popBackStack() })
                            }
                            composable(AppRoutes.ACCOUNT_DELETE) {
                                AccountDeleteScreen(
                                    onBack = { navController.popBackStack() },
                                    onDelete = {
                                        appScope.launch {
                                            runCatching { authDependencies.accountService.withdraw() }
                                                .onSuccess {
                                                    authenticationComplete = false
                                                    navController.navigate(AppRoutes.HOME) {
                                                        popUpTo(navController.graph.findStartDestination().id)
                                                    }
                                                }
                                        }
                                    }
                                )
                            }
                            composable(AppRoutes.SYSTEM_MAINTENANCE) {
                                SystemNoticeScreen(
                                    mode = SystemNoticeMode.Maintenance,
                                    onRetry = { navController.navigate(AppRoutes.HOME) },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(AppRoutes.SYSTEM_ERROR) {
                                SystemNoticeScreen(
                                    mode = SystemNoticeMode.Error,
                                    onRetry = { navController.navigate(AppRoutes.HOME) },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(AppRoutes.TERMS_DETAIL) { entry ->
                                TermsDetailScreen(
                                    documentKey = entry.arguments?.getString("document").orEmpty(),
                                    source = entry.arguments?.getString("source").orEmpty(),
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(AppRoutes.QA_SPLASH) { StartupSplashScreen() }
                            composable(AppRoutes.QA_DESIGN_SYSTEM) { QaDesignSystemOverviewScreen() }
                            composable(AppRoutes.QA_STATES) { QaComponentStatesScreen() }
                            composable(AppRoutes.QA_LEAVE, listOf(optionalArgument("threadId"))) { entry ->
                                QaLeaveAlertScreen(
                                    onDismiss = { navController.popBackStack() },
                                    backdropThreadId = entry.arguments?.getString("threadId")
                                        ?: OVERLAY_BACKDROP_THREAD_ID
                                )
                            }
                            // 31-1 — 같은 화면을 참가자 역할로 연다. 문구와 확인 버튼이 달라진다.
                            composable(AppRoutes.QA_LEAVE_MEMBER, listOf(optionalArgument("threadId"))) { entry ->
                                QaLeaveAlertScreen(
                                    onDismiss = { navController.popBackStack() },
                                    backdropThreadId = entry.arguments?.getString("threadId")
                                        ?: OVERLAY_BACKDROP_THREAD_ID,
                                    host = false
                                )
                            }
                            composable(AppRoutes.QA_APPLY) { entry ->
                                TripDetailScreen(
                                    tripId = entry.arguments?.getString("tripId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    onOpenChatRoom = { navController.navigate(AppRoutes.chatRoom(it)) },
                                    showApplicationSheetInitially = true
                                )
                            }
                            // 20-1a 멤버 액션 — 20-1 목록 위에 액션 시트가 열린 상태로 시작한다
                            composable(AppRoutes.QA_MEMBER_ACTIONS) { entry ->
                                // 신고 시트는 배경에 이 방을 깔아야 한다 — 안 넘기면 배경이 빈 상태로 남는다.
                                val chatMenuThreadId = entry.arguments?.getString("threadId").orEmpty()
                                ChatMenuScreen(
                                    threadId = chatMenuThreadId,
                                    onBack = { navController.popBackStack() },
                                    onOpenSpecialMessages = { navController.navigate(AppRoutes.specialMessages()) },
                                    onOpenNotificationSettings = { threadId ->
                                        navController.navigate(AppRoutes.roomNotification(threadId))
                                    },
                                    onOpenNotices = { navController.navigate(AppRoutes.noticeHistory(it)) },
                                    onOpenRoute = { navController.navigate(AppRoutes.courseRoute(it)) },
                                    // 20-1 「모집 상세」 — 15 모집 상세로 간다.
                                    onOpenTrip = { navController.navigate(AppRoutes.tripDetail(it)) },
                                    initialSheet = ChatMenuSheet.MemberActions
                                )
                            }
                            // 20-1b 내보내기 사유 — 시트가 열린 상태로 시작한다
                            composable(AppRoutes.QA_MEMBER_REMOVE) { entry ->
                                // 신고 시트는 배경에 이 방을 깔아야 한다 — 안 넘기면 배경이 빈 상태로 남는다.
                                val chatMenuThreadId = entry.arguments?.getString("threadId").orEmpty()
                                ChatMenuScreen(
                                    threadId = chatMenuThreadId,
                                    onBack = { navController.popBackStack() },
                                    onOpenSpecialMessages = { navController.navigate(AppRoutes.specialMessages()) },
                                    onOpenNotificationSettings = { threadId ->
                                        navController.navigate(AppRoutes.roomNotification(threadId))
                                    },
                                    onOpenNotices = { navController.navigate(AppRoutes.noticeHistory(it)) },
                                    onOpenRoute = { navController.navigate(AppRoutes.courseRoute(it)) },
                                    // 20-1 「모집 상세」 — 15 모집 상세로 간다.
                                    onOpenTrip = { navController.navigate(AppRoutes.tripDetail(it)) },
                                    initialSheet = ChatMenuSheet.MemberRemove
                                )
                            }
                            composable(AppRoutes.QA_PROFILE_TASTE_EDIT) {
                                ProfileEditScreen(
                                    userProfile = userProfile,
                                    onBack = { navController.popBackStack() },
                                    showTasteSheetInitially = true
                                )
                            }
                            composable(AppRoutes.FEED_COMMENTS) { entry ->
                                FeedCommentsScreen(
                                    postId = entry.arguments?.getString("postId").orEmpty(),
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(AppRoutes.MOCK_AUTH) {
                                AuthFlowScreen(
                                    providedDependencies = authDependencies,
                                    onExit = { navController.popBackStack() },
                                    onComplete = {
                                        navController.navigate(AppRoutes.HOME) {
                                            popUpTo(navController.graph.findStartDestination().id)
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                            composable(AppRoutes.MOCK_AUTH_STEP) { entry ->
                                AuthFlowScreen(
                                    providedDependencies = authDependencies,
                                    initialStepKey = entry.arguments?.getString("startStep"),
                                    onExit = { navController.popBackStack() },
                                    onComplete = {
                                        navController.navigate(AppRoutes.HOME) {
                                            popUpTo(navController.graph.findStartDestination().id)
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                    if (!bypassAuthentication && !authenticationComplete) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .statusBarsPadding()
                                .navigationBarsPadding()
                        ) {
                            AuthFlowScreen(
                                providedDependencies = authDependencies,
                                allowExit = false,
                                onComplete = { authenticationComplete = true }
                            )
                        }
                    }
                    if (networkExperience == OfflineExperience.NoCache) {
                        OfflineNoCacheScreen(onRetry = networkMonitor::retry)
                    }
                    AnimatedVisibility(
                        visible = showStartupSplash,
                        exit = fadeOut(
                            animationSpec = tween(
                                durationMillis = STARTUP_SPLASH_TRANSITION_MILLIS,
                                easing = FastOutSlowInEasing
                            )
                        ) + scaleOut(
                            targetScale = 1.012f,
                            animationSpec = tween(
                                durationMillis = STARTUP_SPLASH_TRANSITION_MILLIS,
                                easing = FastOutSlowInEasing
                            )
                        )
                    ) {
                        StartupSplashScreen()
                    }
                }
            }
        }
    }
}

internal data class QaStartRequest(private val key: String, private val identifier: String?) {
    val startsInExploreMap: Boolean = key in setOf("exploremap", "map")

    /** `profile:62` 처럼 대상 유저를 지정해 25 로 바로 들어올 때 쓴다.
     *  인앱 진입은 도감·피드 작성자 등에서 대상을 들고 오고, 화면으로 바로 여는
     *  라이브 캡처·QA 는 이 값으로 대상을 지정한다. */
    val profileUserId: Long? =
        if (key in setOf("profile", "publicprofile", "profileback", "publicprofileback")) {
            identifier?.toLongOrNull()
        } else {
            null
        }
    val offlineExperienceOverride: OfflineExperience? = when (key) {
        "offline" -> OfflineExperience.NoCache
        "offlinecached", "offlinechat" -> OfflineExperience.Cached
        else -> null
    }

    fun toRoute(): String? {
        // 화면을 직접 여는 진입 경로다. 대상이 지정되지 않으면 그 화면은 빈 상태로 열린다 —
        // 예전처럼 목데이터 식별자를 기본값으로 끼워 넣지 않는다.
        val courseId = identifier.orEmpty()
        val tripId = identifier.orEmpty()
        val chatId = identifier.orEmpty()
        val draftKey = identifier ?: "new"
        // 24 피드 글쓰기는 `room-101` 형태로 기록할 여행을 받는다 (다른 방 라우트와 같은 표기다)
        val feedWriteRoomId = identifier?.serverRoomIdOrNull()

        return when (key) {
            "", "home" -> AppRoutes.HOME

            "dsoverview" -> AppRoutes.QA_DESIGN_SYSTEM

            "splash" -> AppRoutes.QA_SPLASH

            "explore", "exploremap", "map" -> AppRoutes.EXPLORE

            "meetings" -> AppRoutes.MEETINGS

            "meetingsapplied", "chatlistapplied" -> AppRoutes.MEETINGS_APPLIED

            // 화면기획 19는 하단 탭바가 있는 "모임" 탭 화면이다
            "chatlist" -> AppRoutes.MEETINGS

            "feed" -> AppRoutes.FEED

            "my" -> AppRoutes.MY

            "profile", "publicprofile" -> AppRoutes.PROFILE

            // 25-1 · 카드 뒷면 (캡처 전용)
            "profileback", "profile-back", "publicprofileback" -> AppRoutes.PROFILE_CARD_BACK

            "profileedit", "profile-edit", "editprofile", "edit-profile" -> AppRoutes.PROFILE_EDIT

            "myfeed", "my-feed" -> AppRoutes.MY_FEED

            "dex", "frienddex" -> AppRoutes.FRIEND_DEX

            "settings" -> AppRoutes.SETTINGS

            "customer", "customercenter", "customer-center" -> AppRoutes.CUSTOMER_CENTER

            // 캡처 도구의 moyeo_screen="oss-licenses"/"oss-license-detail"은 하이픈이 제거되어 들어온다
            "osslicenses" -> AppRoutes.OSS_LICENSES

            "osslicensedetail" -> AppRoutes.ossLicenseDetail(
                identifier ?: OssLicenseCatalog.items.firstOrNull()?.slug.orEmpty()
            )

            "notifications", "notification", "notif" -> AppRoutes.NOTIFICATIONS

            "search" -> AppRoutes.SEARCH

            // 12-1 검색 결과. 검색어는 `searchresults:주왕산` 처럼 식별자로 넘긴다.
            "searchresults" -> AppRoutes.searchResults(identifier.orEmpty())

            "auth", "onboarding", "mockauth", "signup" -> AppRoutes.MOCK_AUTH

            "onb1" -> AppRoutes.mockAuth("onb-1")

            "onb2" -> AppRoutes.mockAuth("onb-2")

            "onb3" -> AppRoutes.mockAuth("onb-3")

            "login" -> AppRoutes.mockAuth("login")

            "emailauth" -> AppRoutes.mockAuth("email")

            "nickname", "prof1" -> AppRoutes.mockAuth("nickname")

            "profilebasic", "prof3" -> AppRoutes.mockAuth("profile-basic")

            "profiletaste", "prof4" -> AppRoutes.mockAuth("profile-taste")

            "profiletasteedit", "tasteedit" -> AppRoutes.QA_PROFILE_TASTE_EDIT

            "profileimage", "prof2" -> AppRoutes.mockAuth("profile-image")

            "terms" -> AppRoutes.mockAuth("terms")

            "course", "coursedetail" -> AppRoutes.courseDetail(courseId)

            "trip", "tripdetail", "recruitment", "recruitmentdetail", "detail" -> AppRoutes.tripDetail(tripId)

            "apply" -> AppRoutes.qaApply(tripId)

            "create", "createrecruitment", "createreview" -> AppRoutes.createRecruitment(courseId)

            // 17-1 은 초안 식별자 자리에 **코스 id** 를 받는다 (`customcourse:81`) —
            // 그 등록 코스를 불러온 상태로 열어 빈 에디터가 찍히지 않게 한다.
            "customcourse" -> AppRoutes.customCourse("new", courseId.toLongOrNull())

            "placesearch" -> AppRoutes.placeSearch(draftKey)

            "placedetail" -> AppRoutes.placeDetail(draftKey, identifier ?: "2299341")

            "createschedule" -> AppRoutes.createSchedule(draftKey)

            "createpeople" -> AppRoutes.createPeople(draftKey)

            // 17-4a/17-4b 인원수별 멘트 변형. 화면은 하나이고 최대 인원만 다르다 —
            // 4명 이하는 "말 트기 좋은 작은 그룹", 9명 이상은 친목 경고가 뜬다.
            // 캡처 도구의 moyeo_screen="create-people-small"/"create-people-large" 는
            // 하이픈이 제거되어 들어온다.
            "createpeoplesmall" -> AppRoutes.createPeople(draftKey, capacity = 4)

            "createpeoplelarge" -> AppRoutes.createPeople(draftKey, capacity = 10)

            "createmeet", "createmeetpoint" -> AppRoutes.createMeetPoint(draftKey)

            "createdetail" -> AppRoutes.createDetail(draftKey)

            "createsummary", "createsummarycustom", "createsummarylinked" -> AppRoutes.createSummary(draftKey)

            "termsdetail" -> AppRoutes.termsDetail("service", "signup")

            "termsprivacy" -> AppRoutes.termsDetail("privacy", "signup")

            "termslocation" -> AppRoutes.termsDetail("location", "signup")

            "termsmarketing" -> AppRoutes.termsDetail("marketing", "signup")

            "termssettings" -> AppRoutes.termsDetail(identifier ?: "service", "settings")

            "courseedit", "courseeditcustom", "courseeditlinked", "courseeditlocked" -> AppRoutes.courseRoute(tripId)

            "noticehistory" -> AppRoutes.noticeHistory(tripId)

            "hostmanage", "host" -> AppRoutes.hostManage(tripId)

            "chat", "chatroom" -> AppRoutes.chatRoom(chatId)

            "offline", "offlinecached" -> AppRoutes.HOME

            "offlinechat" -> AppRoutes.chatRoom(chatId)

            "specialmessages", "msgs" -> AppRoutes.specialMessages(chatId.takeIf(String::isNotBlank))

            // `tripconfirmed:room-101` 처럼 확정된 방을 넘긴다. 없으면 내 모임에서 찾는다.
            "tripconfirmed", "confirmed" -> AppRoutes.tripConfirmed(identifier ?: tripId.takeIf(String::isNotBlank))

            "chatmenu" -> AppRoutes.chatMenu(chatId)

            // 캡처 도구의 moyeo_screen="member-actions"/"member-remove"는 하이픈이 제거되어 들어온다
            "memberactions" -> AppRoutes.qaMemberActions(chatId)

            "memberremove" -> AppRoutes.qaMemberRemove(chatId)

            "chatattach", "attachment" -> AppRoutes.chatAttach(chatId.takeIf(String::isNotBlank))

            // 20-2a~20-2f 첨부 작성 화면. 캡처 도구의 하이픈은 제거되어 들어온다.
            "attachphoto" -> AppRoutes.attachPhoto(chatId.takeIf(String::isNotBlank))

            "attachplace" -> AppRoutes.attachPlace(chatId.takeIf(String::isNotBlank))

            "attachmap" -> AppRoutes.attachMap(chatId.takeIf(String::isNotBlank))

            "attachpoll" -> AppRoutes.attachPoll(chatId.takeIf(String::isNotBlank))

            "attachsettlement" -> AppRoutes.attachSettlement(chatId.takeIf(String::isNotBlank))

            "attachnotice" -> AppRoutes.attachNotice(chatId.takeIf(String::isNotBlank))

            // ATTACH-COMPOSER-CANON §6 신설 화면. 캡처 도구의 하이픈은 제거되어 들어온다.
            "courserating" -> AppRoutes.courseRating(chatId.takeIf(String::isNotBlank))

            "tripstatus" -> AppRoutes.tripStatus(tripId.takeIf(String::isNotBlank))

            "meetingedit" -> AppRoutes.meetingEdit(tripId.takeIf(String::isNotBlank))

            "roomnotif" -> AppRoutes.roomNotification(chatId.takeIf(String::isNotBlank))

            // `noticeedit:room-22/7` 처럼 방과 공지를 함께 넘긴다
            "noticeedit" -> AppRoutes.noticeEdit(
                tripId = identifier?.substringBefore('/').orEmpty(),
                noticeId = identifier?.substringAfter('/', "")?.toLongOrNull() ?: 0L
            )

            // 내가 만든 것을 되돌리는 화면들 — 기획·웹·iOS 와 같은 라우트 이름을 쓴다.
            "feedactions" -> AppRoutes.feedActions(serverFeedRoute(identifier).orEmpty())

            "feeddelete" -> AppRoutes.feedDelete(serverFeedRoute(identifier).orEmpty())

            "feededit" -> AppRoutes.feedEdit(serverFeedRoute(identifier).orEmpty())

            "commentedit" -> AppRoutes.commentEdit(serverFeedRoute(identifier).orEmpty())

            "recruitedit" -> AppRoutes.recruitEdit(tripId)

            "coursetitleedit" -> AppRoutes.courseTitleEdit(tripId)

            "messagedelete" -> AppRoutes.messageDelete(chatId)

            "favoriterooms" -> AppRoutes.FAVORITE_ROOMS

            "applycancel" -> AppRoutes.applyCancel(tripId.takeIf(String::isNotBlank))

            // `unblockconfirm:62` — 대상이 없으면 확인 버튼만 잠긴 채 열린다
            "unblockconfirm" -> AppRoutes.unblockConfirm(identifier?.toLongOrNull() ?: 0L, "")

            "friendmanage" -> AppRoutes.friendManage(identifier?.toLongOrNull() ?: 0L, "", "")

            "kickhistory" -> AppRoutes.KICK_HISTORY

            "accountproviders" -> AppRoutes.ACCOUNT_PROVIDERS

            // 08-B 비밀번호 재설정 — 08-A 이메일 로그인에서 갈라지는 보조 화면이다
            "passwordreset" -> AppRoutes.mockAuth("password-reset")

            // 31-1 참가자 나가기. 31 은 화면을 새로 만들지 않고 역할로 갈랐다(정본 §6-5).
            "leavemember" -> AppRoutes.qaLeaveMember(chatId.takeIf(String::isNotBlank))

            "friends" -> AppRoutes.FRIENDS

            "tripmessage" -> AppRoutes.TRIP_MESSAGE

            // 30-2 는 피드 신고다 — 식별자는 방이 아니라 서버 피드 id(숫자)다.
            "report" -> AppRoutes.report(identifier?.trim()?.toLongOrNull())

            "leave" -> AppRoutes.qaLeave(chatId.takeIf(String::isNotBlank))

            "states" -> AppRoutes.QA_STATES

            "blocked", "blockedusers" -> AppRoutes.BLOCKED_USERS

            "coursepublish" -> AppRoutes.COURSE_PUBLISH

            "tripday" -> AppRoutes.tripDay(chatId)

            "notificationdetail", "notifdetail" -> AppRoutes.NOTIFICATION_DETAIL

            // 캡처 도구의 moyeo_screen="removal-reason"은 하이픈이 제거되어 들어온다
            "removalreason" -> AppRoutes.REMOVAL_REASON

            "accountdelete" -> AppRoutes.ACCOUNT_DELETE

            "systemmaintenance", "maintenance" -> AppRoutes.SYSTEM_MAINTENANCE

            "systemerror", "error500" -> AppRoutes.SYSTEM_ERROR

            // 라이브 캡처는 서버 feedId(숫자)를 넘긴다. 화면은 "srv-{id}" 형태만 실서버로 인식한다.
            "feedcomments" -> AppRoutes.feedComments(serverFeedRoute(identifier).orEmpty())

            "feeddetail", "feedpost" -> AppRoutes.feedDetail(serverFeedRoute(identifier).orEmpty())

            "feedwrite", "writefeed" -> AppRoutes.feedWrite(roomId = feedWriteRoomId)

            // 24-1~24-5 단계별 캡처 라우트. `feedwrite1:room-101` 처럼 기록할 여행을 지정할 수 있다 —
            // 지정이 없으면 다녀온 여행 중 첫 후보를 고른다(웹 캡처도 방 101 을 쓴다).
            "feedwrite1" -> AppRoutes.feedWrite(1, feedWriteRoomId)

            "feedwrite2" -> AppRoutes.feedWrite(2, feedWriteRoomId)

            "feedwrite3" -> AppRoutes.feedWrite(3, feedWriteRoomId)

            "feedwrite4" -> AppRoutes.feedWrite(4, feedWriteRoomId)

            "feedwrite5" -> AppRoutes.feedWrite(5, feedWriteRoomId)

            else -> null
        }
    }

    companion object {
        fun parse(value: String?): QaStartRequest {
            val rawValue = value?.trim().orEmpty()
            if (rawValue.isEmpty()) {
                return QaStartRequest(key = "", identifier = null)
            }
            val parts = rawValue.split(":", limit = 2)
            val key = parts.first()
                .replace("-", "")
                .replace("_", "")
                .lowercase()
            val identifier = parts.getOrNull(1)
                ?.takeIf { it.isNotBlank() }

            return QaStartRequest(key = key, identifier = identifier)
        }
    }
}

@Composable
private fun MoyeoBottomBar(currentRoute: String?, onDestinationClick: (BottomDestination) -> Unit) {
    val colors = MaterialTheme.colorScheme
    // `모임` 탭 알림 점 — 참여 중인 방에 **읽지 않은 메시지가 있을 때만** 켠다.
    // 근거는 `GET /chat-rooms/my` 의 `unreadMessageCount` 하나뿐이고, 이미 받아 둔 목록을 읽는다
    // (탭바가 따로 부르지 않는다 · 정본 R3). 목록이 아직 없으면 켜지 않는다 —
    // 모르는 상태를 "알림 있음" 으로 보이면 안 된다.
    // 웹·iOS 는 이 점을 **조건 없이 항상** 켜고 있었고, 안드로이드는 아예 없어 세 표면이 갈렸다.
    val meetingRooms = LocalTabDataStore.current.meetings.rooms
    val hasMeetingAlert = (meetingRooms as? ServerListState.Loaded)?.items
        ?.any { (it.unreadMessageCount ?: 0) > 0 } == true

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bottom-bar")
            .background(colors.surface)
    ) {
        HorizontalDivider(color = colors.outline.copy(alpha = 0.55f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(start = 8.dp, top = 6.dp, end = 8.dp, bottom = 8.dp)
        ) {
            bottomDestinations.forEach { destination ->
                val selected = currentRoute == destination.route
                val contentColor = if (selected) colors.primary else colors.onSurfaceVariant

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("bottom-${destination.route}")
                        .clickable { onDestinationClick(destination) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                                tint = contentColor,
                                modifier = Modifier.size(22.dp)
                            )
                            if (destination.tab == BottomTab.Meetings && hasMeetingAlert) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 3.dp, y = (-1).dp)
                                        .size(6.dp)
                                        .background(colors.primary, CircleShape)
                                        .testTag("bottom-meetings-alert")
                                )
                            }
                        }
                        Text(
                            text = destination.label,
                            color = contentColor,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

/**
 * 20-2a~20-2f 첨부 작성 화면 6종을 한 곳에 등록한다 (`ATTACH-COMPOSER-CANON.md`).
 *
 * 여섯 라우트가 `threadId` 선택 인자를 똑같이 쓴다 — 캡처 라우트는 인자 없이 들어오고,
 * 그때 화면은 그대로 열리되 보내기만 잠긴다(목데이터를 대신 그리지 않는다).
 */
private fun NavGraphBuilder.attachComposerDestinations(
    navController: NavHostController,
    tourismRepository: TourismContentRepository
) {
    val threadArgument = listOf(
        navArgument("threadId") {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }
    )

    fun NavBackStackEntry.threadId(): String? = arguments?.getString("threadId")

    composable(AppRoutes.ATTACH_PHOTO, threadArgument) { entry ->
        AttachPhotoScreen(threadId = entry.threadId(), onBack = { navController.popBackStack() })
    }
    composable(AppRoutes.ATTACH_PLACE, threadArgument) { entry ->
        AttachPlaceScreen(
            threadId = entry.threadId(),
            onBack = { navController.popBackStack() },
            repository = tourismRepository
        )
    }
    composable(AppRoutes.ATTACH_MAP, threadArgument) { entry ->
        AttachMapScreen(threadId = entry.threadId(), onBack = { navController.popBackStack() })
    }
    composable(AppRoutes.ATTACH_POLL, threadArgument) { entry ->
        AttachPollScreen(threadId = entry.threadId(), onBack = { navController.popBackStack() })
    }
    composable(AppRoutes.ATTACH_SETTLEMENT, threadArgument) { entry ->
        AttachSettlementScreen(threadId = entry.threadId(), onBack = { navController.popBackStack() })
    }
    composable(AppRoutes.ATTACH_NOTICE, threadArgument) { entry ->
        AttachNoticeScreen(threadId = entry.threadId(), onBack = { navController.popBackStack() })
    }
}

/** 선택 문자열 인자 하나를 선언한다 — 캡처 라우트는 인자 없이 들어온다. */
private fun optionalArgument(name: String) = navArgument(name) {
    type = NavType.StringType
    nullable = true
    defaultValue = null
}

/**
 * `ATTACH-COMPOSER-CANON.md` §6 — 버튼은 있는데 이어지는 화면이 없던 자리 12종.
 *
 * 첨부 작성 6종과 같은 규칙이다: 대상(방·공지·사용자)이 지정되지 않으면 화면은 그대로 열리고
 * 실행 버튼만 잠긴다. 예시 데이터를 대신 그리지 않는다(NO-MOCK-CANON R1).
 */
private fun NavGraphBuilder.gapDestinations(navController: NavHostController) {
    composable(AppRoutes.COURSE_RATING, listOf(optionalArgument("threadId"))) { entry ->
        CourseRatingScreen(
            threadId = entry.arguments?.getString("threadId"),
            onBack = { navController.popBackStack() }
        )
    }
    composable(AppRoutes.TRIP_STATUS, listOf(optionalArgument("tripId"))) { entry ->
        TripStatusScreen(
            tripId = entry.arguments?.getString("tripId").orEmpty(),
            onBack = { navController.popBackStack() },
            // 확정하면 참가자가 보는 20-4 확정 모먼트와 같은 화면으로 이어진다
            // 확정한 그 방의 확정 모먼트로 간다 — 다른 확정된 방을 찾아 헤매지 않는다
            onConfirmed = { navController.navigate(AppRoutes.tripConfirmed(entry.arguments?.getString("tripId"))) },
            onCancelled = { navController.popBackStack() }
        )
    }
    composable(AppRoutes.MEETING_EDIT, listOf(optionalArgument("tripId"))) { entry ->
        MeetingEditScreen(
            tripId = entry.arguments?.getString("tripId").orEmpty(),
            onBack = { navController.popBackStack() }
        )
    }
    composable(AppRoutes.ROOM_NOTIFICATION, listOf(optionalArgument("threadId"))) { entry ->
        RoomNotificationScreen(
            threadId = entry.arguments?.getString("threadId"),
            onBack = { navController.popBackStack() },
            onOpenAppNotificationSettings = { navController.navigate(AppRoutes.NOTIFICATION_DETAIL) }
        )
    }
    composable(
        AppRoutes.NOTICE_EDIT,
        listOf(
            optionalArgument("tripId"),
            navArgument("noticeId") {
                type = NavType.LongType
                defaultValue = 0L
            }
        )
    ) { entry ->
        NoticeEditScreen(
            tripId = entry.arguments?.getString("tripId").orEmpty(),
            noticeId = entry.arguments?.getLong("noticeId") ?: 0L,
            onBack = { navController.popBackStack() }
        )
    }
    // 내가 만든 것을 되돌리는 화면 7종 (2026-09-04 BE 회신으로 API 가 열렸다).
    // 캡처 라우트 이름은 기획·웹·iOS 와 **글자 그대로 같게** 맞춘다.
    composable(AppRoutes.FEED_ACTIONS, listOf(optionalArgument("feedId"))) { entry ->
        val feedId = entry.arguments?.getString("feedId").orEmpty()
        FeedActionsScreen(
            feedId = feedId.serverFeedIdOrZero(),
            onBack = { navController.popBackStack() },
            onEdit = { navController.navigate(AppRoutes.feedEdit(feedId)) },
            onDelete = { navController.navigate(AppRoutes.feedDelete(feedId)) }
        )
    }
    composable(AppRoutes.FEED_DELETE, listOf(optionalArgument("feedId"))) { entry ->
        FeedDeleteScreen(
            feedId = entry.arguments?.getString("feedId").orEmpty().serverFeedIdOrZero(),
            onBack = { navController.popBackStack() },
            onDeleted = { navController.navigate(AppRoutes.FEED) { popUpTo(AppRoutes.FEED) } }
        )
    }
    composable(AppRoutes.FEED_EDIT, listOf(optionalArgument("feedId"))) { entry ->
        val feedId = entry.arguments?.getString("feedId").orEmpty()
        FeedEditScreen(
            feedId = feedId.serverFeedIdOrZero(),
            onBack = { navController.popBackStack() },
            onDelete = { navController.navigate(AppRoutes.feedDelete(feedId)) }
        )
    }
    composable(AppRoutes.COMMENT_EDIT, listOf(optionalArgument("feedId"))) { entry ->
        CommentEditScreen(
            feedId = entry.arguments?.getString("feedId").orEmpty().serverFeedIdOrZero(),
            onBack = { navController.popBackStack() }
        )
    }
    composable(AppRoutes.RECRUIT_EDIT, listOf(optionalArgument("tripId"))) { entry ->
        RecruitEditScreen(
            tripId = entry.arguments?.getString("tripId").orEmpty(),
            onBack = { navController.popBackStack() }
        )
    }
    composable(AppRoutes.COURSE_TITLE_EDIT, listOf(optionalArgument("tripId"))) { entry ->
        val tripId = entry.arguments?.getString("tripId").orEmpty()
        CourseTitleEditScreen(
            tripId = tripId,
            onBack = { navController.popBackStack() },
            onOpenRoute = { navController.navigate(AppRoutes.courseRoute(tripId)) }
        )
    }
    composable(AppRoutes.MESSAGE_DELETE, listOf(optionalArgument("tripId"))) { entry ->
        val tripId = entry.arguments?.getString("tripId").orEmpty()
        MessageDeleteScreen(
            tripId = tripId,
            onBack = { navController.popBackStack() },
            onDeleted = { navController.navigate(AppRoutes.chatRoom(tripId)) }
        )
    }
    composable(AppRoutes.FAVORITE_ROOMS) {
        FavoriteRoomsScreen(
            onBack = { navController.popBackStack() },
            onOpenRoom = { navController.navigate(AppRoutes.tripDetail("room-$it")) }
        )
    }
    composable(AppRoutes.APPLY_CANCEL, listOf(optionalArgument("tripId"))) { entry ->
        ApplyCancelScreen(
            tripId = entry.arguments?.getString("tripId").orEmpty(),
            onBack = { navController.popBackStack() },
            onCancelled = { navController.popBackStack() }
        )
    }
    composable(
        AppRoutes.UNBLOCK_CONFIRM,
        listOf(
            navArgument("userId") {
                type = NavType.LongType
                defaultValue = 0L
            },
            optionalArgument("nickname")
        )
    ) { entry ->
        UnblockConfirmScreen(
            userId = entry.arguments?.getLong("userId") ?: 0L,
            nickname = entry.arguments?.getString("nickname").orEmpty(),
            onBack = { navController.popBackStack() },
            onUnblocked = { navController.popBackStack() }
        )
    }
    composable(
        AppRoutes.FRIEND_MANAGE,
        listOf(
            navArgument("userId") {
                type = NavType.LongType
                defaultValue = 0L
            },
            optionalArgument("nickname"),
            optionalArgument("subtitle")
        )
    ) { entry ->
        FriendManageScreen(
            userId = entry.arguments?.getLong("userId") ?: 0L,
            nickname = entry.arguments?.getString("nickname").orEmpty(),
            subtitle = entry.arguments?.getString("subtitle").orEmpty(),
            onBack = { navController.popBackStack() },
            onOpenProfile = { navController.navigate(AppRoutes.PROFILE) },
            onRemoved = { navController.popBackStack() }
        )
    }
    composable(AppRoutes.KICK_HISTORY) {
        KickHistoryScreen(onBack = { navController.popBackStack() })
    }
}
