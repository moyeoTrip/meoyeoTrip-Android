package kr.hanchae.moyeotrip.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import kr.hanchae.moyeotrip.BuildConfig
import kr.hanchae.moyeotrip.data.CourseSource
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.data.auth.AuthDependencies
import kr.hanchae.moyeotrip.data.network.AndroidNetworkMonitor
import kr.hanchae.moyeotrip.data.network.OfflineCacheStore
import kr.hanchae.moyeotrip.data.network.OfflineExperience
import kr.hanchae.moyeotrip.data.network.offlineExperience
import kr.hanchae.moyeotrip.data.tourism.FallbackTourismContentRepository
import kr.hanchae.moyeotrip.data.tourism.HttpTourismContentRepository
import kr.hanchae.moyeotrip.data.tourism.SampleTourismContentRepository
import kr.hanchae.moyeotrip.notifications.PushNavigationEvent
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
import kr.hanchae.moyeotrip.ui.screens.PlaceDetailScreen
import kr.hanchae.moyeotrip.ui.screens.PlaceSearchScreen
import kr.hanchae.moyeotrip.ui.screens.ProfileEditScreen
import kr.hanchae.moyeotrip.ui.screens.ProfileScreen
import kr.hanchae.moyeotrip.ui.screens.QaComponentStatesScreen
import kr.hanchae.moyeotrip.ui.screens.QaDesignSystemOverviewScreen
import kr.hanchae.moyeotrip.ui.screens.QaLeaveAlertScreen
import kr.hanchae.moyeotrip.ui.screens.RecruitmentCourseSourceScreen
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

private const val STARTUP_SPLASH_HOLD_MILLIS = 1_150L
private const val STARTUP_SPLASH_TRANSITION_MILLIS = 420

@Composable
fun MoyeoTripApp(
    startScreen: String? = null,
    pushNavigationEvent: PushNavigationEvent? = null,
    skipStartupSplash: Boolean = false,
    skipAuthentication: Boolean = false,
    /** 번호별 비교 캡처는 다크/라이트 두 테마를 모두 찍는다. null이면 시스템 설정을 따른다. */
    forceDarkTheme: Boolean? = null,
    onAuthenticationComplete: () -> Unit = {},
    onPushRouteHandled: (Long) -> Unit = {}
) {
    MoyeoTripTheme(darkTheme = forceDarkTheme ?: isSystemInDarkTheme()) {
        val currentDensity = LocalDensity.current
        val context = LocalContext.current
        CompositionLocalProvider(
            LocalDensity provides Density(currentDensity.density, fontScale = 1f)
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
            val networkExperience = qaStartRequest.offlineExperienceOverride
                ?: offlineExperience(detectedOnline, cacheStore.hasCachedContent)
            val isOnline = networkExperience == OfflineExperience.Online
            val authDependencies = remember(context) { AuthDependencies.appDefault(context) }
            val tourismRepository = remember(authDependencies, startScreen) {
                if (startScreen != null) {
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
            val userProfile by authDependencies.userProfileStore.profile.collectAsState()
            val bypassAuthentication = skipAuthentication || startScreen != null
            var authenticationComplete by remember(authDependencies) { mutableStateOf(false) }
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
            val showBottomBar = bottomDestinations.any { it.route == currentRoute }
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
                            OfflineCachedBanner()
                        }
                    },
                    bottomBar = {
                        if (showBottomBar) {
                            MoyeoBottomBar(
                                currentRoute = currentRoute,
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
                                startInMap = qaStartsInExploreMap
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
                                onOpenProfile = { navController.navigate(AppRoutes.PROFILE) },
                                onOpenMyFeed = { navController.navigate(AppRoutes.MY_FEED) },
                                onOpenFriendDex = { navController.navigate(AppRoutes.FRIEND_DEX) },
                                onOpenSettings = { navController.navigate(AppRoutes.SETTINGS) },
                                onOpenCustomerCenter = { navController.navigate(AppRoutes.CUSTOMER_CENTER) },
                                onOpenFriends = { navController.navigate(AppRoutes.FRIENDS) },
                                onOpenCoursePublish = { navController.navigate(AppRoutes.COURSE_PUBLISH) }
                            )
                        }
                        composable(AppRoutes.PROFILE) {
                            ProfileScreen(
                                userProfile = userProfile,
                                onBack = { navController.popBackStack() },
                                onOpenProfileEdit = { navController.navigate(AppRoutes.PROFILE_EDIT) },
                                onOpenFriendDex = { navController.navigate(AppRoutes.FRIEND_DEX) },
                                onOpenSettings = { navController.navigate(AppRoutes.SETTINGS) }
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
                            FriendDexScreen(onBack = { navController.popBackStack() })
                        }
                        composable(AppRoutes.SETTINGS) {
                            SettingsScreen(
                                onBack = { navController.popBackStack() },
                                accountService = authDependencies.accountService,
                                onOpenNotificationDetail = { navController.navigate(AppRoutes.NOTIFICATION_DETAIL) },
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
                                onOpenAttachment = { navController.navigate(AppRoutes.CHAT_ATTACH) }
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
                                onOpenTripMessage = { navController.navigate(AppRoutes.TRIP_MESSAGE) }
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
                                onOpenNotificationSettings = { navController.navigate(AppRoutes.NOTIFICATION_DETAIL) },
                                onOpenReport = { navController.navigate(AppRoutes.REPORT) },
                                onOpenNotices = { navController.navigate(AppRoutes.noticeHistory(it)) },
                                onOpenRoute = { navController.navigate(AppRoutes.courseRoute(it)) }
                            )
                        }
                        composable(AppRoutes.CHAT_ATTACH) {
                            ChatAttachmentScreen(
                                onBack = { navController.popBackStack() },
                                onOpenSpecialMessages = { navController.navigate(AppRoutes.SPECIAL_MESSAGES) },
                                isOnline = isOnline
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
                                onOpenAttachment = { navController.navigate(AppRoutes.CHAT_ATTACH) },
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

internal data class QaStartRequest(private val key: String, private val identifier: String?) {
    val startsInExploreMap: Boolean = key in setOf("exploremap", "map")
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

            "chatlist" -> AppRoutes.CHAT_LIST

            "feed" -> AppRoutes.FEED

            "my" -> AppRoutes.MY

            "profile", "publicprofile" -> AppRoutes.PROFILE

            "profileedit", "profile-edit", "editprofile", "edit-profile" -> AppRoutes.PROFILE_EDIT

            "myfeed", "my-feed" -> AppRoutes.MY_FEED

            "dex", "frienddex" -> AppRoutes.FRIEND_DEX

            "settings" -> AppRoutes.SETTINGS

            "customer", "customercenter", "customer-center" -> AppRoutes.CUSTOMER_CENTER

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

            "chatattach", "attachment" -> AppRoutes.CHAT_ATTACH

            "friends" -> AppRoutes.FRIENDS

            "tripmessage" -> AppRoutes.TRIP_MESSAGE

            "report" -> AppRoutes.REPORT

            "leave" -> AppRoutes.QA_LEAVE

            "states" -> AppRoutes.QA_STATES

            "blocked", "blockedusers" -> AppRoutes.BLOCKED_USERS

            "coursepublish" -> AppRoutes.COURSE_PUBLISH

            "tripday" -> AppRoutes.tripDay(chatId)

            "notificationdetail", "notifdetail" -> AppRoutes.NOTIFICATION_DETAIL

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
