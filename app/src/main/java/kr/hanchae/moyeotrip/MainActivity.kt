package kr.hanchae.moyeotrip

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import java.util.Locale
import kr.hanchae.moyeotrip.ui.navigation.MoyeoTripApp

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val configuration = Configuration(newBase.resources.configuration).apply {
            setLocale(Locale.KOREA)
        }
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                skipStartupSplash = skipStartupSplash,
                skipAuthentication = skipAuthentication
            )
        }
    }
}
