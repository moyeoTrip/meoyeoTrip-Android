import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
}

val localProperties = Properties().apply {
    rootProject.file("local.properties")
        .takeIf { it.isFile }
        ?.inputStream()
        ?.use(::load)
}

fun configuredProperty(name: String, defaultValue: String = ""): String = providers.gradleProperty(name)
    .orElse(providers.environmentVariable(name))
    .orElse(localProperties.getProperty(name, defaultValue))
    .get()

val hasFirebaseConfig = file("google-services.json").exists()
if (hasFirebaseConfig) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "kr.hanchae.moyeotrip"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "kr.hanchae.moyeotrip"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "kr.hanchae.moyeotrip.MoyeoTripTestRunner"

        val authApiBaseUrl = configuredProperty(
            "MOYEO_AUTH_API_BASE_URL",
            "https://moyeo-trip-api.jayden-bin.cc"
        )
        val authDemoMode = configuredProperty("MOYEO_AUTH_DEMO_MODE", "false").toBoolean()
        val googleWebClientId = configuredProperty("MOYEO_GOOGLE_WEB_CLIENT_ID")
        val kakaoNativeAppKey = configuredProperty("KAKAO_NATIVE_APP_KEY")
        val sentryDsn = configuredProperty("SENTRY_DSN")
        val sentryEnvironment = configuredProperty(
            "SENTRY_ENVIRONMENT",
            if (configuredProperty("CI", "false").toBoolean()) "ci" else "development"
        )
        buildConfigField("String", "AUTH_API_BASE_URL", "\"$authApiBaseUrl\"")
        buildConfigField("boolean", "AUTH_DEMO_MODE", authDemoMode.toString())
        buildConfigField("boolean", "FIREBASE_CONFIGURED", hasFirebaseConfig.toString())
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoNativeAppKey\"")
        buildConfigField("String", "SENTRY_DSN", "\"$sentryDsn\"")
        buildConfigField("String", "SENTRY_ENVIRONMENT", "\"$sentryEnvironment\"")
        manifestPlaceholders["kakaoRedirectScheme"] = if (kakaoNativeAppKey.isBlank()) {
            "kakao-not-configured"
        } else {
            "kakao$kakaoNativeAppKey"
        }
        manifestPlaceholders["usesCleartextTraffic"] = "false"
    }

    buildTypes {
        debug {
            manifestPlaceholders["usesCleartextTraffic"] = "true"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

ktlint {
    version.set(libs.versions.ktlint)
    android.set(true)
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)
    implementation(libs.google.id)
    implementation(libs.kakao.maps)
    implementation(libs.kakao.user)
    implementation(libs.sentry.android)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation("org.json:json:20250517")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
