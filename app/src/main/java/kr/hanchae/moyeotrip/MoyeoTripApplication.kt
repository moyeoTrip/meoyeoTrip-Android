@file:Suppress("DEPRECATION")

package kr.hanchae.moyeotrip

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.sdk.common.KakaoSdk
import io.sentry.android.core.SentryAndroid
import kr.hanchae.moyeotrip.notifications.MoyeoPushNotificationChannels
import kr.hanchae.moyeotrip.notifications.MoyeoPushTokenStore

class MoyeoTripApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MoyeoPushNotificationChannels.create(this)
        if (BuildConfig.FIREBASE_CONFIGURED) {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                MoyeoPushTokenStore.save(this, token)
            }
        }
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
