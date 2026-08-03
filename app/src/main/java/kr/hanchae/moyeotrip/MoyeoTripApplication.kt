package kr.hanchae.moyeotrip

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class MoyeoTripApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isNotBlank()) {
            KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        }
    }
}
