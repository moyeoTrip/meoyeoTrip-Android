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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.delay
import kr.hanchae.moyeotrip.data.auth.AuthDependencies
import kr.hanchae.moyeotrip.ui.screens.AuthFlowScreen
import kr.hanchae.moyeotrip.ui.screens.ChatListScreen
import kr.hanchae.moyeotrip.ui.screens.ChatRoomScreen
import kr.hanchae.moyeotrip.ui.screens.CourseDetailScreen
import kr.hanchae.moyeotrip.ui.screens.CreateRecruitmentScreen
import kr.hanchae.moyeotrip.ui.screens.CustomerCenterScreen
import kr.hanchae.moyeotrip.ui.screens.ExploreScreen
import kr.hanchae.moyeotrip.ui.screens.FeedDetailScreen
import kr.hanchae.moyeotrip.ui.screens.FeedScreen
import kr.hanchae.moyeotrip.ui.screens.FeedWriteScreen
import kr.hanchae.moyeotrip.ui.screens.FriendDexScreen
import kr.hanchae.moyeotrip.ui.screens.HomeScreen
import kr.hanchae.moyeotrip.ui.screens.HostManageScreen
import kr.hanchae.moyeotrip.ui.screens.MeetingsScreen
import kr.hanchae.moyeotrip.ui.screens.MyFeedScreen
import kr.hanchae.moyeotrip.ui.screens.MyScreen
import kr.hanchae.moyeotrip.ui.screens.NotificationCenterScreen
import kr.hanchae.moyeotrip.ui.screens.ProfileEditScreen
import kr.hanchae.moyeotrip.ui.screens.ProfileScreen
import kr.hanchae.moyeotrip.ui.screens.SearchScreen
import kr.hanchae.moyeotrip.ui.screens.SettingsScreen
import kr.hanchae.moyeotrip.ui.screens.SpecialMessagesScreen
import kr.hanchae.moyeotrip.ui.screens.StartupSplashScreen
import kr.hanchae.moyeotrip.ui.screens.TripDetailScreen
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
fun MoyeoTripApp(startScreen: String? = null, skipStartupSplash: Boolean = false, skipAuthentication: Boolean = false) {
    MoyeoTripTheme {
        val currentDensity = LocalDensity.current
        val context = LocalContext.current
        CompositionLocalProvider(
            LocalDensity provides Density(currentDensity.density, fontScale = 1f)
        ) {
            var showStartupSplash by remember { mutableStateOf(!skipStartupSplash) }
            val authDependencies = remember(context) { AuthDependencies.appDefault(context) }
            val userProfile by authDependencies.userProfileStore.profile.collectAsState()
            val bypassAuthentication = skipAuthentication || startScreen != null
            var authenticationComplete by remember(authDependencies) {
                mutableStateOf(authDependencies.sessionStore.current.isAuthenticated)
            }
            LaunchedEffect(authDependencies, authenticationComplete) {
                if (authenticationComplete) {
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
            val qaStartRequest = remember(startScreen) { QaStartRequest.parse(startScreen) }
            val qaStartRoute = remember(qaStartRequest) { qaStartRequest?.toRoute() }
            val qaStartsInExploreMap = qaStartRequest?.startsInExploreMap == true
            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            val showBottomBar = bottomDestinations.any { it.route == currentRoute }
            val topSafePadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 12.dp

            LaunchedEffect(skipStartupSplash) {
                if (!skipStartupSplash) {
                    delay(STARTUP_SPLASH_HOLD_MILLIS)
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
                    NavHost(
                        navController = navController,
                        startDestination = AppRoutes.HOME,
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(top = topSafePadding)
                    ) {
                        composable(AppRoutes.HOME) {
                            HomeScreen(
                                onOpenCourse = { navController.navigate(AppRoutes.courseDetail(it)) },
                                onOpenExplore = { navController.navigate(AppRoutes.EXPLORE) },
                                onOpenNotifications = { navController.navigate(AppRoutes.NOTIFICATIONS) },
                                onCreateRecruitment = { navController.navigate(AppRoutes.createRecruitment(it)) },
                                onOpenMockAuth = { navController.navigate(AppRoutes.MOCK_AUTH) }
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
                                onOpenRoom = { navController.navigate(AppRoutes.chatRoom(it)) },
                                onOpenSpecialMessages = { navController.navigate(AppRoutes.SPECIAL_MESSAGES) }
                            )
                        }
                        composable(AppRoutes.FEED) {
                            FeedScreen(
                                onOpenPost = { navController.navigate(AppRoutes.feedDetail(it)) },
                                onWritePost = { navController.navigate(AppRoutes.FEED_WRITE) }
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
                                onOpenCustomerCenter = { navController.navigate(AppRoutes.CUSTOMER_CENTER) }
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
                                onBack = { navController.popBackStack() }
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
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(AppRoutes.SPECIAL_MESSAGES) {
                            SpecialMessagesScreen(onBack = { navController.popBackStack() })
                        }
                        composable(AppRoutes.NOTIFICATIONS) {
                            NotificationCenterScreen(
                                onBack = { navController.popBackStack() },
                                onOpenTrip = { navController.navigate(AppRoutes.tripDetail(it)) },
                                onOpenPost = { navController.navigate(AppRoutes.feedDetail(it)) },
                                onOpenCourse = { navController.navigate(AppRoutes.courseDetail(it)) }
                            )
                        }
                        composable(
                            route = AppRoutes.CREATE_RECRUITMENT,
                            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
                        ) { entry ->
                            CreateRecruitmentScreen(
                                courseId = entry.arguments?.getString("courseId").orEmpty(),
                                onBack = { navController.popBackStack() },
                                onOpenChat = { navController.navigate(AppRoutes.chatRoom(it)) },
                                onOpenManage = { navController.navigate(AppRoutes.hostManage(it)) }
                            )
                        }
                        composable(
                            route = AppRoutes.HOST_MANAGE,
                            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
                        ) { entry ->
                            HostManageScreen(
                                tripId = entry.arguments?.getString("tripId").orEmpty(),
                                onBack = { navController.popBackStack() },
                                onOpenChat = { navController.navigate(AppRoutes.chatRoom(it)) }
                            )
                        }
                        composable(AppRoutes.FEED_WRITE) {
                            FeedWriteScreen(
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
                                onOpenCourse = { navController.navigate(AppRoutes.courseDetail(it)) }
                            )
                        }
                        composable(AppRoutes.MOCK_AUTH) {
                            AuthFlowScreen(
                                providedDependencies = AuthDependencies.demo(),
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
                if (!showStartupSplash && !bypassAuthentication && !authenticationComplete) {
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

    fun toRoute(): String? {
        val courseId = identifier ?: "cheongsong-juwangsan"
        val tripId = identifier ?: "trip-cheongsong-juwangsan"
        val chatId = identifier ?: "chat-cheongsong-juwangsan"

        return when (key) {
            "", "home" -> AppRoutes.HOME
            "explore", "exploremap", "map" -> AppRoutes.EXPLORE
            "meetings" -> AppRoutes.MEETINGS
            "chatlist" -> AppRoutes.CHAT_LIST
            "feed" -> AppRoutes.FEED
            "my" -> AppRoutes.MY
            "profile" -> AppRoutes.PROFILE
            "profileedit", "profile-edit", "editprofile", "edit-profile" -> AppRoutes.PROFILE_EDIT
            "myfeed", "my-feed" -> AppRoutes.MY_FEED
            "dex", "frienddex" -> AppRoutes.FRIEND_DEX
            "settings" -> AppRoutes.SETTINGS
            "customer", "customercenter", "customer-center" -> AppRoutes.CUSTOMER_CENTER
            "notifications", "notification" -> AppRoutes.NOTIFICATIONS
            "search" -> AppRoutes.SEARCH
            "auth", "onboarding", "login", "terms", "mockauth", "signup" -> AppRoutes.MOCK_AUTH
            "course", "coursedetail" -> AppRoutes.courseDetail(courseId)
            "trip", "tripdetail", "recruitment", "recruitmentdetail", "apply" -> AppRoutes.tripDetail(tripId)
            "create", "createrecruitment" -> AppRoutes.createRecruitment(courseId)
            "hostmanage", "host" -> AppRoutes.hostManage(tripId)
            "chat", "chatroom" -> AppRoutes.chatRoom(chatId)
            "specialmessages" -> AppRoutes.SPECIAL_MESSAGES
            "feeddetail", "feedpost" -> AppRoutes.feedDetail(identifier ?: "feed-1")
            "feedwrite", "writefeed" -> AppRoutes.FEED_WRITE
            else -> null
        }
    }

    companion object {
        fun parse(value: String?): QaStartRequest? {
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
