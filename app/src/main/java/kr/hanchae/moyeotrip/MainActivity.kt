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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import java.util.Locale
import kr.hanchae.moyeotrip.notifications.PUSH_ROUTE_EXTRA
import kr.hanchae.moyeotrip.notifications.pushRoute
import kr.hanchae.moyeotrip.ui.navigation.MoyeoTripApp

class MainActivity : ComponentActivity() {
    private var pushRoute by mutableStateOf<String?>(null)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun attachBaseContext(newBase: Context) {
        val configuration = Configuration(newBase.resources.configuration).apply {
            setLocale(Locale.KOREA)
        }
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pushRoute = intent.pushDestination()
        val startScreen = if (BuildConfig.DEBUG) {
            intent.getStringExtra("moyeo_screen")
        } else {
            null
        }
        val skipStartupSplash = BuildConfig.DEBUG && intent.getBooleanExtra("moyeo_skip_splash", false)
        val skipAuthentication = BuildConfig.DEBUG && intent.getBooleanExtra("moyeo_skip_auth", false)
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
        setContent {
            MoyeoTripApp(
                startScreen = startScreen,
                pushRoute = pushRoute,
                skipStartupSplash = skipStartupSplash,
                skipAuthentication = skipAuthentication,
                onAuthenticationComplete = ::requestNotificationPermissionIfNeeded
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pushRoute = intent.pushDestination()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun Intent.pushDestination(): String? {
        getStringExtra(PUSH_ROUTE_EXTRA)?.let { return it }
        val data = listOf("screen", "route", "destination")
            .mapNotNull { key -> getStringExtra(key)?.let { key to it } }
            .toMap()
        return data.takeIf { it.isNotEmpty() }?.let(::pushRoute)
    }
}
