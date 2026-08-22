package kr.hanchae.moyeotrip

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import java.util.Locale
import kr.hanchae.moyeotrip.notifications.PUSH_ROUTE_EXTRA
import kr.hanchae.moyeotrip.notifications.PushNavigationEvent
import kr.hanchae.moyeotrip.notifications.consumePushNavigationEvent
import kr.hanchae.moyeotrip.notifications.nextPushNavigationEvent
import kr.hanchae.moyeotrip.notifications.pushRoute
import kr.hanchae.moyeotrip.ui.navigation.MoyeoTripApp

class MainActivity : ComponentActivity() {
    private var pushNavigationEvent by mutableStateOf<PushNavigationEvent?>(null)

    override fun attachBaseContext(newBase: Context) {
        val configuration = Configuration(newBase.resources.configuration).apply {
            setLocale(Locale.KOREA)
        }
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        acceptPushDestination(intent.pushDestination())
        val startScreen = if (BuildConfig.DEBUG) {
            intent.getStringExtra("moyeo_screen")
        } else {
            null
        }
        val skipStartupSplash = BuildConfig.DEBUG && intent.getBooleanExtra("moyeo_skip_splash", false)
        val skipAuthentication = BuildConfig.DEBUG && intent.getBooleanExtra("moyeo_skip_auth", false)
        // 캡처 도구가 테마를 지정한다: moyeo_force_theme=dark|light
        val forceDarkTheme = if (BuildConfig.DEBUG) {
            when (intent.getStringExtra("moyeo_force_theme")?.lowercase()) {
                "dark" -> true
                "light" -> false
                else -> null
            }
        } else {
            null
        }
        // 리소스 한정자(drawable-night 등)는 인텐트가 아니라 configuration.uiMode 를 본다.
        // 강제 테마일 때는 uiMode 도 같이 덮어써야 낮/밤 이미지가 색 테마와 어긋나지 않는다.
        if (forceDarkTheme != null) {
            val nightFlag = if (forceDarkTheme) {
                Configuration.UI_MODE_NIGHT_YES
            } else {
                Configuration.UI_MODE_NIGHT_NO
            }
            val overridden = Configuration(resources.configuration).apply {
                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightFlag
            }
            @Suppress("DEPRECATION")
            resources.updateConfiguration(overridden, resources.displayMetrics)
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = Color.rgb(248, 252, 249),
                darkScrim = Color.rgb(8, 23, 17)
            )
        )
        val darkTheme = forceDarkTheme ?: (
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES
            )
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
        setContent {
            MoyeoTripApp(
                startScreen = startScreen,
                pushNavigationEvent = pushNavigationEvent,
                onPushRouteHandled = ::consumePushNavigationEvent,
                skipStartupSplash = skipStartupSplash,
                skipAuthentication = skipAuthentication,
                forceDarkTheme = forceDarkTheme,
                onAuthenticationComplete = ::requestNotificationPermissionIfNeeded
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        acceptPushDestination(intent.pushDestination())
    }

    private fun acceptPushDestination(route: String?) {
        pushNavigationEvent = nextPushNavigationEvent(pushNavigationEvent, route)
    }

    private fun consumePushNavigationEvent(eventId: Long) {
        pushNavigationEvent = consumePushNavigationEvent(pushNavigationEvent, eventId)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun Intent.pushDestination(): String? {
        getStringExtra(PUSH_ROUTE_EXTRA)?.let { return it }
        val data = listOf("screen", "route", "destination")
            .mapNotNull { key -> getStringExtra(key)?.let { key to it } }
            .toMap()
        return data.takeIf { it.isNotEmpty() }?.let(::pushRoute)
    }

    private companion object {
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }
}
