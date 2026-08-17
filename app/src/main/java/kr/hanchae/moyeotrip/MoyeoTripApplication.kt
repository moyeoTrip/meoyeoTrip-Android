package kr.hanchae.moyeotrip

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import io.sentry.android.core.SentryAndroid
import kr.hanchae.moyeotrip.notifications.MoyeoPushNotificationChannels

class MoyeoTripApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MoyeoPushNotificationChannels.create(this)
        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isNotBlank()) {
            KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        }
        if (BuildConfig.SENTRY_DSN.isNotBlank()) {
            SentryAndroid.init(this) { options ->
                options.dsn = BuildConfig.SENTRY_DSN
                options.environment = BuildConfig.SENTRY_ENVIRONMENT
                options.isSendDefaultPii = false
                options.tracesSampleRate = if (BuildConfig.DEBUG) 0.0 else 0.1
            }
        }
    }
}
