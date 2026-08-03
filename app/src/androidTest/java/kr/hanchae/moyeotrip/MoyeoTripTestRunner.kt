package kr.hanchae.moyeotrip

import android.app.Activity
import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner

class MoyeoTripTestRunner : AndroidJUnitRunner() {
    override fun callActivityOnCreate(activity: Activity, bundle: Bundle?) {
        activity.intent.putExtra("moyeo_skip_auth", true)
        super.callActivityOnCreate(activity, bundle)
    }
}
