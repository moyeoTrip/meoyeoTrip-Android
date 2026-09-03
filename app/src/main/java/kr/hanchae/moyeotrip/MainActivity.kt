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
import kr.hanchae.moyeotrip.data.auth.AuthDependencies
import kr.hanchae.moyeotrip.data.settings.ThemePreference
import kr.hanchae.moyeotrip.data.settings.ThemePreferenceStore
import kr.hanchae.moyeotrip.data.settings.resolveDarkTheme
import kr.hanchae.moyeotrip.domain.auth.AuthProvider
import kr.hanchae.moyeotrip.domain.auth.ServiceSession
import kr.hanchae.moyeotrip.domain.auth.SignupState
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
        // 라이브 캡처 (디버그 빌드 전용).
        // 캡처 라우팅(화면 직접 진입·강제 테마·스플래시 스킵)은 그대로 두고 데이터 차단만 푼다.
        //   adb shell am start -n … --es moyeo_screen 10 --ez moyeo_live_data true --es moyeo_access_token <jwt>
        // 이 플래그가 없으면 기존 캡처 경로와 100% 동일하게 동작해야 한다 — PDF 4열 대조가 깨진다.
        val liveData = BuildConfig.DEBUG && intent.getBooleanExtra("moyeo_live_data", false)
        // QA 세션 주입 (디버그 빌드 전용).
        // 로그인 폼에 비밀번호를 입력하지 않고도 서버 데이터를 검증할 수 있게 발급된 토큰을 바로 심는다.
        //   adb shell am start -n … --es moyeo_access_token <token> [--es moyeo_refresh_token <token>]
        // 캡처 라우트(moyeo_screen 단독)에서는 주입하지 않는다 — 번호별 비교 캡처가 서버 데이터로
        // 오염되면 안 된다. 라이브 캡처(moyeo_live_data)에서는 같은 엑스트라를 그대로 재사용한다.
        var qaSessionInjected = false
        if (BuildConfig.DEBUG && (startScreen == null || liveData)) {
            intent.getStringExtra("moyeo_access_token")?.takeIf { it.isNotBlank() }?.let { accessToken ->
                AuthDependencies.appDefault(applicationContext).sessionStore.saveSignup(
                    AuthProvider.EMAIL,
                    ServiceSession(
                        accessToken = accessToken,
                        refreshToken = intent.getStringExtra("moyeo_refresh_token").orEmpty(),
                        signupState = SignupState.SIGNUP_COMPLETE
                    )
                )
                qaSessionInjected = true
            }
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
        // 사용자 테마 설정(29 설정 › 화면 › 테마)은 강제 테마가 없을 때만 적용된다.
        // 캡처 모드에서는 저장된 설정을 무시하고 강제 테마(없으면 시스템)를 쓴다.
        val themePreference = if (startScreen != null) {
            ThemePreference.System
        } else {
            ThemePreferenceStore(applicationContext).current
        }
        val systemDarkTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES
        // 첫 프레임(스플래시)부터 밤/낮 리소스가 맞아야 하므로 setContent 전에 한 번 적용한다.
        val darkTheme = resolveDarkTheme(forceDarkTheme, themePreference, systemDarkTheme)
        applyNightModeOverride(darkTheme)
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
        applySystemBarAppearance(darkTheme)
        setContent {
            MoyeoTripApp(
                startScreen = startScreen,
                liveData = liveData,
                pushNavigationEvent = pushNavigationEvent,
                onPushRouteHandled = ::consumePushNavigationEvent,
                skipStartupSplash = skipStartupSplash,
                skipAuthentication = skipAuthentication,
                qaSessionInjected = qaSessionInjected,
                forceDarkTheme = forceDarkTheme,
                onAuthenticationComplete = ::requestNotificationPermissionIfNeeded,
                onEffectiveDarkThemeChanged = ::applyEffectiveTheme
            )
        }
    }

    /**
     * 테마가 실제로 바뀔 때(사용자가 설정에서 순환) 상태바 아이콘과 리소스 한정자를 다시 맞춘다.
     * 색은 Compose 테마가 즉시 반영하고, 이후에 그려지는 밤/낮 이미지는 이 uiMode 를 본다.
     */
    private fun applyEffectiveTheme(darkTheme: Boolean) {
        applyNightModeOverride(darkTheme)
        applySystemBarAppearance(darkTheme)
    }

    /**
     * 리소스 한정자(drawable-night 등)는 인텐트나 Compose 테마가 아니라 configuration.uiMode 를 본다.
     * 강제 테마·사용자 설정으로 테마를 정할 때는 uiMode 도 같이 덮어써야 낮/밤 이미지가 색 테마와
     * 어긋나지 않는다.
     */
    private fun applyNightModeOverride(darkTheme: Boolean) {
        val nightFlag = if (darkTheme) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == nightFlag) return
        val overridden = Configuration(resources.configuration).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightFlag
        }
        @Suppress("DEPRECATION")
        resources.updateConfiguration(overridden, resources.displayMetrics)
    }

    private fun applySystemBarAppearance(darkTheme: Boolean) {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
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
