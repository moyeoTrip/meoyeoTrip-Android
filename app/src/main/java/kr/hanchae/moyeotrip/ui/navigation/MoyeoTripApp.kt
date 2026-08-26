package kr.hanchae.moyeotrip.ui.navigation

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.data.social.DexCompanion
import kr.hanchae.moyeotrip.BuildConfig
import kr.hanchae.moyeotrip.data.CourseSource
import kr.hanchae.moyeotrip.data.MockTripRepository
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
import kr.hanchae.moyeotrip.data.tourism.FallbackTourismContentRepository
import kr.hanchae.moyeotrip.data.tourism.HttpTourismContentRepository
import kr.hanchae.moyeotrip.data.tourism.SampleTourismContentRepository
import kr.hanchae.moyeotrip.domain.auth.UserDisplayProfile
import kr.hanchae.moyeotrip.notifications.PushNavigationEvent
import kr.hanchae.moyeotrip.ui.LocalCaptureMode
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.LocalMapCaptureMode
import kr.hanchae.moyeotrip.ui.screens.AccountDeleteScreen
import kr.hanchae.moyeotrip.ui.screens.AuthFlowScreen
import kr.hanchae.moyeotrip.ui.screens.BlockedUsersScreen
import kr.hanchae.moyeotrip.ui.screens.ChatAttachmentScreen
import kr.hanchae.moyeotrip.ui.screens.ChatListScreen
import kr.hanchae.moyeotrip.ui.screens.ChatMenuScreen
import kr.hanchae.moyeotrip.ui.screens.ChatRoomScreen
import kr.hanchae.moyeotrip.ui.screens.CourseDetailScreen
import kr.hanchae.moyeotrip.ui.screens.CoursePublishScreen
import kr.hanchae.moyeotrip.ui.screens.CourseRouteScreen
import kr.hanchae.moyeotrip.ui.screens.CreateDetailScreen
import kr.hanchae.moyeotrip.ui.screens.CreateMeetPointScreen
import kr.hanchae.moyeotrip.ui.screens.CreatePeopleScreen
import kr.hanchae.moyeotrip.ui.screens.CreateScheduleScreen
import kr.hanchae.moyeotrip.ui.screens.CreateSummaryScreen
import kr.hanchae.moyeotrip.ui.screens.CustomCourseScreen
import kr.hanchae.moyeotrip.ui.screens.CustomerCenterScreen
import kr.hanchae.moyeotrip.ui.screens.ExploreScreen
import kr.hanchae.moyeotrip.ui.screens.FeedCommentsScreen
import kr.hanchae.moyeotrip.ui.screens.FeedDetailScreen
import kr.hanchae.moyeotrip.ui.screens.FeedScreen
import kr.hanchae.moyeotrip.ui.screens.FeedWriteScreen
import kr.hanchae.moyeotrip.ui.screens.FriendDexScreen
import kr.hanchae.moyeotrip.ui.screens.FriendsScreen
import kr.hanchae.moyeotrip.ui.screens.HomeScreen
import kr.hanchae.moyeotrip.ui.screens.HostManageScreen
import kr.hanchae.moyeotrip.ui.screens.MeetingChatTab
import kr.hanchae.moyeotrip.ui.screens.MeetingsScreen
import kr.hanchae.moyeotrip.ui.screens.MyFeedScreen
import kr.hanchae.moyeotrip.ui.screens.MyScreen
import kr.hanchae.moyeotrip.ui.screens.NoticeHistoryScreen
import kr.hanchae.moyeotrip.ui.screens.NotificationCenterScreen
import kr.hanchae.moyeotrip.ui.screens.NotificationDetailScreen
import kr.hanchae.moyeotrip.ui.screens.OfflineCachedBanner
import kr.hanchae.moyeotrip.ui.screens.OfflineNoCacheScreen
import kr.hanchae.moyeotrip.ui.screens.OssLicenseDetailScreen
import kr.hanchae.moyeotrip.ui.screens.OssLicensesScreen
import kr.hanchae.moyeotrip.ui.screens.PlaceDetailScreen
import kr.hanchae.moyeotrip.ui.screens.PlaceSearchScreen
import kr.hanchae.moyeotrip.ui.screens.ProfileEditScreen
import kr.hanchae.moyeotrip.ui.screens.ProfileCardScreen
import kr.hanchae.moyeotrip.ui.screens.QaComponentStatesScreen
import kr.hanchae.moyeotrip.ui.screens.QaDesignSystemOverviewScreen
import kr.hanchae.moyeotrip.ui.screens.QaLeaveAlertScreen
import kr.hanchae.moyeotrip.ui.screens.RecruitmentCourseSourceScreen
import kr.hanchae.moyeotrip.ui.screens.RemovalReasonScreen
import kr.hanchae.moyeotrip.ui.screens.ReportScreen
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
 * 실서버 의존성(`LocalServerData`)을 만들지 판정한다.
 *
 * 목 캡처 라우트(`moyeo_screen` 단독)와 인증 우회 실행은 네트워크를 타지 않는다.
 * 라이브 캡처(`moyeo_live_data`)는 **그 차단만** 푼다 — 데모 빌드는 서버가 없으니 그대로 막는다.
 */
internal fun injectsServerData(
    startScreen: String?,
    skipAuthentication: Boolean,
    liveCapture: Boolean,
    demoMode: Boolean
): Boolean {
    if (demoMode) return false
    if (liveCapture) return true
    return startScreen == null && !skipAuthentication
}

/**
 * 기기 상태(최근 검색어)·실제 빌드 버전 대신 화면기획 목데이터를 그릴지.
 * 라이브 캡처는 실데이터를 보러 찍는 것이므로 목데이터 치환을 끈다.
 */
internal fun usesPlanningMockData(captureMode: Boolean, liveCapture: Boolean): Boolean = captureMode && !liveCapture

/**
 * 35·36·37 의 강제 오프라인 플래그는 목 캡처용으로 남겨 두고, 라이브 캡처에서는
 * 실제 차단(`adb shell svc wifi disable` · `svc data disable`) 결과를 그대로 그린다.
 */
internal fun resolveNetworkExperience(
    forcedOverride: OfflineExperience?,
    liveCapture: Boolean,
    detectedOnline: Boolean,
    hasCachedContent: Boolean
): OfflineExperience = forcedOverride.takeIf { !liveCapture }
    ?: offlineExperience(detectedOnline, hasCachedContent)

@Composable
fun MoyeoTripApp(
    startScreen: String? = null,
    /**
     * 라이브 캡처(`moyeo_live_data`, 디버그 전용). 캡처 라우팅은 그대로 두고 **데이터 차단만** 푼다.
     * false 면 기존 목 캡처 경로와 100% 동일하다.
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
            // QA 캡처(moyeo_screen)로 들어온 실행에서는 실지도 대신 목업 지도를 그린다 — 타일 로딩이
            // 비결정적이라 번호별 비교 캡처가 깨진다.
            // 라이브 캡처에서도 목업 지도를 유지한다 — 타일이 비결정적이고 x86_64 는 SDK 미지원이다.
            LocalMapCaptureMode provides captureMode,
            // 저장된 최근 검색어·테마 설정·실제 빌드 버전 대신 화면기획 목데이터를 보여줘야 하는지.
            // 라이브 캡처는 실데이터를 보러 찍는 것이므로 목데이터 치환을 끈다.
            LocalCaptureMode provides usesPlanningMockData(captureMode, liveCapture)
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
                liveCapture = liveCapture,
                detectedOnline = detectedOnline,
                hasCachedContent = cacheStore.hasCachedContent
            )
            val isOnline = networkExperience == OfflineExperience.Online
            val authDependencies = remember(context) { AuthDependencies.appDefault(context) }
            val tourismRepository = remember(authDependencies, startScreen, liveCapture) {
                if (startScreen != null && !liveCapture) {
                    SampleTourismContentRepository
                } else {
                    FallbackTourismContentRepository(
                        primary = HttpTourismContentRepository(
                            baseUrl = BuildConfig.AUTH_API_BASE_URL,
                            accessToken = { authDependencies.sessionStore.current.accessToken }
                        ),
                        fallback = SampleTourismContentRepository
                    )
                }
            }
            val appScope = rememberCoroutineScope()
            val storedUserProfile by authDependencies.userProfileStore.profile.collectAsState()
            // 프로필 카드(25)가 누구의 카드인지. 도감에서 눌러 들어오면 그 동행자 정보를 그대로 넘겨
            // 카드 앞면의 '나와 N회 동행'과 뒷면의 '함께한 여행'을 추가 호출 없이 채운다.
            var profileCardTarget by remember { mutableStateOf<DexCompanion?>(null) }
            // 목 캡처는 기기에 저장된 표시용 프로필(닉네임·프로필 사진)을 읽지 않는다.
            // 로그인 실행이나 라이브 캡처가 남긴 값이 25·26·28 번호별 비교 캡처를 오염시킨다
            // (docs/alignment/클라이언트-전용-기능.md §5 "캡처 모드에서는 기기 상태를 쓰지 않는다").
            val userProfile = if (usesPlanningMockData(captureMode, liveCapture)) {
                UserDisplayProfile()
            } else {
                storedUserProfile
            }
            val bypassAuthentication = skipAuthentication || startScreen != null
            var authenticationComplete by remember(authDependencies) {
                // 라이브 캡처는 인증 UI 를 거치지 않으므로 이미 심어진 세션을 로그인 완료로 본다
                // (`LocalServerData` 는 로그인 완료 상태에서만 내려간다).
                val liveSessionReady = liveCapture && authDependencies.sessionStore.current.accessToken != null
                mutableStateOf(qaSessionInjected || liveSessionReady)
            }
            // 목 캡처 라우트(startScreen)·데모 모드에서는 아예 만들지 않는다 — 네트워크 회귀 금지.
            // 라이브 캡처는 이 차단만 푼다(라우팅·강제 테마·목업 지도는 캡처와 동일).
            val serverDataDependencies = remember(authDependencies, startScreen, liveCapture) {
                if (!injectsServerData(startScreen, skipAuthentication, liveCapture, BuildConfig.AUTH_DEMO_MODE)) {
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
                        }
                    )
                }
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
            val topSafePadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 12.dp

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
                LocalServerData provides serverDataDependencies?.takeIf { authenticationComplete }
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
                            if (networkExperience == OfflineExperience.Cached &&
                                currentRoute?.startsWith("chat/") != true
                            ) {
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
                                    onOpenRoom = {
                                        navController.navigate(
                                            if (it ==
                                                "chat-cheongsong-juwangsan"
                                            ) {
                                                AppRoutes.tripDay(it)
                                            } else {
                                                AppRoutes.chatRoom(it)
                                            }
                                        )
                                    },
                                    onOpenTrip = { navController.navigate(AppRoutes.tripDetail(it)) },
                                    onOpenSpecialMessages = { navController.navigate(AppRoutes.SPECIAL_MESSAGES) }
                                )
                            }
                            composable(AppRoutes.MEETINGS_APPLIED) {
                                MeetingsScreen(
                                    onOpenRoom = { navController.navigate(AppRoutes.chatRoom(it)) },
                                    onOpenTrip = { navController.navigate(AppRoutes.tripDetail(it)) },
                                    initialTab = MeetingChatTab.Applied
                                )
                            }
                            composable(AppRoutes.FEED) {
                                FeedScreen(
                                    onOpenPost = { navController.navigate(AppRoutes.feedDetail(it)) },
                                    onWritePost = { navController.navigate(AppRoutes.feedWrite()) }
                                )
                            }
                            composable(AppRoutes.MY) {
                                MyScreen(
                                    userProfile = userProfile,
                                    onOpenTrip = { navController.navigate(AppRoutes.tripDetail(it)) },
                                    onOpenCourse = { navController.navigate(AppRoutes.courseDetail(it)) },
                                    // 내 카드로는 열지 못한다 — GET /users/me/profile 응답에 userId 가 없어
                                    // 공개 프로필 API 를 내 계정으로 호출할 수 없다(BE 에 요청함).
                                    // 그래서 내 프로필 요약은 28 프로필 수정으로 보낸다.
                                    onOpenProfile = { navController.navigate(AppRoutes.PROFILE_EDIT) },
                                    onOpenMyFeed = { navController.navigate(AppRoutes.MY_FEED) },
                                    onOpenFriendDex = { navController.navigate(AppRoutes.FRIEND_DEX) },
                                    onOpenSettings = { navController.navigate(AppRoutes.SETTINGS) },
                                    onOpenCustomerCenter = { navController.navigate(AppRoutes.CUSTOMER_CENTER) },
                                    onOpenFriends = { navController.navigate(AppRoutes.FRIENDS) },
                                    onOpenCoursePublish = { navController.navigate(AppRoutes.COURSE_PUBLISH) }
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
                                    onOpenTrip = { navController.navigate(AppRoutes.tripDetail(it)) },
                                    onCreateRecruitment = { navController.navigate(AppRoutes.createRecruitment(it)) }
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
                                    }
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
                            composable(AppRoutes.SPECIAL_MESSAGES) {
                                SpecialMessagesScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenTripConfirmed = { navController.navigate(AppRoutes.TRIP_CONFIRMED) }
                                )
                            }
                            composable(AppRoutes.NOTIFICATIONS) {
                                NotificationCenterScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenTrip = { navController.navigate(AppRoutes.tripDetail(it)) },
                                    onOpenPost = { navController.navigate(AppRoutes.feedDetail(it)) },
                                    onOpenCourse = { navController.navigate(AppRoutes.courseDetail(it)) },
                                    onOpenTripConfirmed = { navController.navigate(AppRoutes.TRIP_CONFIRMED) },
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
                            composable(AppRoutes.CUSTOM_COURSE) { entry ->
                                CustomCourseScreen(
                                    draftId = entry.arguments?.getString("draftId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    onOpenPlaceSearch = { navController.navigate(AppRoutes.placeSearch(it)) },
                                    onContinue = { navController.navigate(AppRoutes.createSchedule(it)) }
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
                            composable(AppRoutes.CREATE_PEOPLE) { entry ->
                                CreatePeopleScreen(
                                    draftId = entry.arguments?.getString("draftId").orEmpty(),
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
                                    onCreated = { trip ->
                                        navController.navigate(AppRoutes.hostManage(trip.id)) {
                                            popUpTo(AppRoutes.CREATE_RECRUITMENT) { inclusive = true }
                                        }
                                    },
                                    // 실서버에 만든 방은 roomId 로 15 모집 상세를 연다 (18 모집 관리는 목데이터 전용 화면이다)
                                    onCreatedRoom = { roomId ->
                                        navController.navigate(AppRoutes.tripDetail("room-$roomId")) {
                                            popUpTo(AppRoutes.CREATE_RECRUITMENT) { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable(AppRoutes.COURSE_ROUTE) { entry ->
                                CourseRouteScreen(
                                    tripId = entry.arguments?.getString("tripId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    onOpenMeetingPoint = {},
                                    onOpenNotices = { navController.navigate(AppRoutes.noticeHistory(it)) }
                                )
                            }
                            composable(AppRoutes.NOTICE_HISTORY) { entry ->
                                NoticeHistoryScreen(
                                    tripId = entry.arguments?.getString("tripId").orEmpty(),
                                    onBack = { navController.popBackStack() }
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
                                    onOpenRoute = { navController.navigate(AppRoutes.courseRoute(it)) }
                                )
                            }
                            composable(
                                route = AppRoutes.FEED_WRITE,
                                arguments = listOf(
                                    navArgument("step") {
                                        type = NavType.IntType
                                        defaultValue = 1
                                    }
                                )
                            ) { entry ->
                                FeedWriteScreen(
                                    initialStep = entry.arguments?.getInt("step") ?: 1,
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
                                    onOpenCourse = { navController.navigate(AppRoutes.courseDetail(it)) },
                                    initialQuery = if (startScreen?.substringBefore(":") == "search") {
                                        "경주 단풍"
                                    } else {
                                        ""
                                    }
                                )
                            }
                            composable(AppRoutes.TRIP_CONFIRMED) {
                                TripConfirmedScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenChat = {
                                        navController.navigate(AppRoutes.tripDay("chat-cheongsong-juwangsan"))
                                    }
                                )
                            }
                            composable(AppRoutes.CHAT_MENU) { entry ->
                                ChatMenuScreen(
                                    threadId = entry.arguments?.getString("threadId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    onOpenSpecialMessages = { navController.navigate(AppRoutes.SPECIAL_MESSAGES) },
                                    onOpenNotificationSettings = {
                                        navController.navigate(AppRoutes.NOTIFICATION_DETAIL)
                                    },
                                    onOpenReport = { navController.navigate(AppRoutes.REPORT) },
                                    onOpenNotices = { navController.navigate(AppRoutes.noticeHistory(it)) },
                                    onOpenRoute = { navController.navigate(AppRoutes.courseRoute(it)) }
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
                                // 캡처 라우트는 threadId 없이 들어온다 — 배경·동작 모두 기존 목데이터 방이다
                                val attachThreadId = entry.arguments?.getString("threadId")
                                ChatAttachmentScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenSpecialMessages = { navController.navigate(AppRoutes.SPECIAL_MESSAGES) },
                                    isOnline = isOnline,
                                    threadId = attachThreadId
                                )
                            }
                            composable(AppRoutes.FRIENDS) {
                                FriendsScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenDex = { navController.navigate(AppRoutes.FRIEND_DEX) }
                                )
                            }
                            composable(AppRoutes.TRIP_MESSAGE) {
                                TripMessageScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenFeedWrite = { navController.navigate(AppRoutes.feedWrite()) },
                                    onOpenCoursePublish = { navController.navigate(AppRoutes.COURSE_PUBLISH) },
                                    onOpenDex = { navController.navigate(AppRoutes.FRIEND_DEX) }
                                )
                            }
                            composable(AppRoutes.REPORT) {
                                ReportScreen(onBack = { navController.popBackStack() })
                            }
                            composable(AppRoutes.BLOCKED_USERS) {
                                BlockedUsersScreen(onBack = { navController.popBackStack() })
                            }
                            composable(AppRoutes.COURSE_PUBLISH) {
                                CoursePublishScreen(
                                    onBack = { navController.popBackStack() },
                                    onPublished = {
                                        navController.navigate(AppRoutes.courseDetail("cheongsong-juwangsan"))
                                    }
                                )
                            }
                            composable(AppRoutes.TRIP_DAY) { entry ->
                                val threadId = entry.arguments?.getString("threadId").orEmpty()
                                TripDayScreen(
                                    threadId = threadId,
                                    onBack = { navController.popBackStack() },
                                    onOpenMenu = { navController.navigate(AppRoutes.chatMenu(threadId)) },
                                    onOpenAttachment = {
                                        navController.navigate(AppRoutes.chatAttach(threadId))
                                    },
                                    onOpenRoute = {
                                        val tripId = MockTripRepository.findThread(threadId).tripId
                                        if (tripId != null) navController.navigate(AppRoutes.courseRoute(tripId))
                                    }
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
                            composable(AppRoutes.QA_LEAVE) {
                                QaLeaveAlertScreen(onDismiss = { navController.popBackStack() })
                            }
                            composable(AppRoutes.QA_APPLY) { entry ->
                                TripDetailScreen(
                                    tripId = entry.arguments?.getString("tripId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    onOpenChatRoom = { navController.navigate(AppRoutes.chatRoom(it)) },
                                    showApplicationSheetInitially = true
                                )
                            }
                            composable(AppRoutes.QA_MEMBER_ACTIONS) { entry ->
                                ChatMenuScreen(
                                    threadId = entry.arguments?.getString("threadId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    onOpenSpecialMessages = { navController.navigate(AppRoutes.SPECIAL_MESSAGES) },
                                    onOpenNotificationSettings = {
                                        navController.navigate(AppRoutes.NOTIFICATION_DETAIL)
                                    },
                                    onOpenReport = { navController.navigate(AppRoutes.REPORT) },
                                    onOpenNotices = { navController.navigate(AppRoutes.noticeHistory(it)) },
                                    onOpenRoute = { navController.navigate(AppRoutes.courseRoute(it)) },
                                    showActionsSheetInitially = true
                                )
                            }
                            composable(AppRoutes.QA_MEMBER_REMOVE) { entry ->
                                ChatMenuScreen(
                                    threadId = entry.arguments?.getString("threadId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    onOpenSpecialMessages = { navController.navigate(AppRoutes.SPECIAL_MESSAGES) },
                                    onOpenNotificationSettings = {
                                        navController.navigate(AppRoutes.NOTIFICATION_DETAIL)
                                    },
                                    onOpenReport = { navController.navigate(AppRoutes.REPORT) },
                                    onOpenNotices = { navController.navigate(AppRoutes.noticeHistory(it)) },
                                    onOpenRoute = { navController.navigate(AppRoutes.courseRoute(it)) },
                                    showRemoveSheetInitially = true
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
                                val mockAuthDependencies = remember { AuthDependencies.demo() }
                                AuthFlowScreen(
                                    providedDependencies = mockAuthDependencies,
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
                                val mockAuthDependencies = remember { AuthDependencies.demo() }
                                AuthFlowScreen(
                                    providedDependencies = mockAuthDependencies,
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
                        OfflineNoCacheScreen(onRetry = {})
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
     *  인앱 진입은 도감·피드 작성자 등에서 대상을 들고 오지만, 화면으로 바로 여는
     *  라이브 캡처·QA 에는 그 값이 없어 목데이터로 떨어졌다. */
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
        val courseId = identifier ?: "cheongsong-juwangsan"
        val tripId = identifier ?: "trip-cheongsong-juwangsan"
        // 모집 관리(18)의 기본 대상만 화면기획과 같은 경주 단풍·야경이다
        val hostManageTripId = identifier ?: "trip-gyeongju-night"
        val chatId = identifier ?: "chat-cheongsong-juwangsan"

        return when (key) {
            "", "home" -> AppRoutes.HOME

            "dsoverview" -> AppRoutes.QA_DESIGN_SYSTEM

            "splash" -> AppRoutes.QA_SPLASH

            "explore", "exploremap", "map" -> AppRoutes.EXPLORE

            "meetings" -> AppRoutes.MEETINGS

            "meetingsapplied", "chatlistapplied" -> {
                MockTripRepository.ensureQaApplications()
                AppRoutes.MEETINGS_APPLIED
            }

            // 화면기획 19는 하단 탭바가 있는 "모임" 탭 화면이고, 신청중 세그먼트에 2건이 있다
            "chatlist" -> {
                MockTripRepository.ensureQaApplications()
                AppRoutes.MEETINGS
            }

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

            "customcourse" -> AppRoutes.customCourse(MockTripRepository.beginRecruitmentDraft(courseId).id)

            "placesearch" -> AppRoutes.placeSearch(MockTripRepository.beginRecruitmentDraft(courseId).id)

            "placedetail" -> AppRoutes.placeDetail(
                MockTripRepository.beginRecruitmentDraft(courseId).id,
                identifier ?: "2299341"
            )

            "createschedule" -> AppRoutes.createSchedule(MockTripRepository.beginRecruitmentDraft(courseId).id)

            "createpeople" -> AppRoutes.createPeople(MockTripRepository.beginRecruitmentDraft(courseId).id)

            "createmeet", "createmeetpoint" -> AppRoutes.createMeetPoint(
                MockTripRepository.beginRecruitmentDraft(courseId).id
            )

            "createdetail" -> AppRoutes.createDetail(MockTripRepository.beginRecruitmentDraft(courseId).id)

            "createsummary", "createsummarycustom" -> recruitmentSummaryRoute(courseId, CourseSource.Custom)

            "createsummarylinked" -> recruitmentSummaryRoute(courseId, CourseSource.Linked)

            "termsdetail" -> AppRoutes.termsDetail("service", "signup")

            "termsprivacy" -> AppRoutes.termsDetail("privacy", "signup")

            "termslocation" -> AppRoutes.termsDetail("location", "signup")

            "termsmarketing" -> AppRoutes.termsDetail("marketing", "signup")

            "termssettings" -> AppRoutes.termsDetail(identifier ?: "service", "settings")

            "courseedit", "courseeditcustom" -> AppRoutes.courseRoute("trip-cheongsong-juwangsan")

            // 화면기획 18-2/18-3은 같은 주왕산 코스를 잠금 상태만 바꿔 보여준다
            "courseeditlinked" -> AppRoutes.courseRoute("trip-cheongsong-juwangsan-linked")

            "courseeditlocked" -> AppRoutes.courseRoute("trip-cheongsong-juwangsan-locked")

            "noticehistory" -> AppRoutes.noticeHistory(tripId)

            "hostmanage", "host" -> AppRoutes.hostManage(hostManageTripId)

            "chat", "chatroom" -> AppRoutes.chatRoom(chatId)

            "offline", "offlinecached" -> AppRoutes.HOME

            "offlinechat" -> AppRoutes.chatRoom(chatId)

            "specialmessages", "msgs" -> AppRoutes.SPECIAL_MESSAGES

            "tripconfirmed", "confirmed" -> AppRoutes.TRIP_CONFIRMED

            "chatmenu" -> AppRoutes.chatMenu(chatId)

            // 캡처 도구의 moyeo_screen="member-actions"/"member-remove"는 하이픈이 제거되어 들어온다
            "memberactions" -> AppRoutes.qaMemberActions(chatId)

            "memberremove" -> AppRoutes.qaMemberRemove(chatId)

            "chatattach", "attachment" -> AppRoutes.chatAttach()

            "friends" -> AppRoutes.FRIENDS

            "tripmessage" -> AppRoutes.TRIP_MESSAGE

            "report" -> AppRoutes.REPORT

            "leave" -> AppRoutes.QA_LEAVE

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

            "feedcomments" -> AppRoutes.feedComments(identifier ?: "feed-1")

            "feeddetail", "feedpost" -> AppRoutes.feedDetail(identifier ?: "feed-1")

            "feedwrite", "writefeed" -> AppRoutes.feedWrite()

            // 24-1~24-5 단계별 캡처 라우트
            "feedwrite1" -> AppRoutes.feedWrite(1)

            "feedwrite2" -> AppRoutes.feedWrite(2)

            "feedwrite3" -> AppRoutes.feedWrite(3)

            "feedwrite4" -> AppRoutes.feedWrite(4)

            "feedwrite5" -> AppRoutes.feedWrite(5)

            else -> null
        }
    }

    companion object {
        private fun recruitmentSummaryRoute(courseId: String, source: CourseSource): String {
            val draft = MockTripRepository.beginRecruitmentDraft(courseId)
            MockTripRepository.updateRecruitmentDraft(draft.copy(courseSource = source))
            return AppRoutes.createSummary(draft.id)
        }

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
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                            tint = contentColor,
                            modifier = Modifier.size(22.dp)
                        )
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
