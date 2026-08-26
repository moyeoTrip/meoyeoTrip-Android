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

val keystoreProperties = Properties().apply {
    rootProject.file("keystore.properties")
        .takeIf { it.isFile }
        ?.inputStream()
        ?.use(::load)
}

fun signingProperty(name: String): String? = (
    providers.gradleProperty(name).orNull
        ?: providers.environmentVariable(name).orNull
        ?: keystoreProperties.getProperty(name)
    )?.takeIf { it.isNotBlank() }

val releaseStoreFile = signingProperty("MOYEO_RELEASE_STORE_FILE")
val releaseStorePassword = signingProperty("MOYEO_RELEASE_STORE_PASSWORD")
val releaseKeyAlias = signingProperty("MOYEO_RELEASE_KEY_ALIAS")
val releaseKeyPassword = signingProperty("MOYEO_RELEASE_KEY_PASSWORD")
val hasReleaseSigning = releaseStoreFile != null &&
    releaseStorePassword != null &&
    releaseKeyAlias != null &&
    releaseKeyPassword != null

val hasFirebaseConfig = file("google-services.json").exists()
if (hasFirebaseConfig) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "kr.hanchae.moyeotrip"
    // 자체 네이티브 빌드가 없으면 AGP 가 NDK 를 해석하지 않아 llvm-strip 을 못 찾고,
    // release 빌드가 "Unable to strip ... missing strip tool" 로 .so 스트립을 조용히 건너뛴다.
    // 지금 의존성들은 벤더가 이미 스트립해 배포하므로 실제 용량 변화는 없지만,
    // 나중에 스트립 안 된 .so 가 딸려 들어와도 그대로 실려 나가지 않도록 NDK 를 지정해 둔다.
    // (이 버전 NDK 가 설치돼 있어야 한다. 빌드 환경을 늘릴 때 함께 챙길 것)
    ndkVersion = "27.1.12297006"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "kr.hanchae.moyeotrip"
        minSdk = 26
        targetSdk = 36
        versionCode = configuredProperty("MOYEO_VERSION_CODE", "1").toInt()
        versionName = configuredProperty("MOYEO_VERSION_NAME", "1.0")

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

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            manifestPlaceholders["usesCleartextTraffic"] = "true"
        }
        release {
            signingConfig = signingConfigs.findByName("release")
            // 네이티브 크래시/ANR 심볼. 메타데이터는 Play 가 APK 를 만들 때 떼어내므로
            // 사용자 다운로드 용량에는 영향이 없다.
            //
            // 주의: 지금은 이 설정이 아무것도 만들어내지 못한다. 이 앱의 네이티브 코드는 전부
            // 서드파티 prebuilt 이고(libK3fAndroid.so=카카오맵, libsentry.so) 벤더가 .symtab 과
            // .debug_* 를 제거한 채 배포한다 (llvm-nm 기준 .symtab 심볼 0개, .dynsym 만 31,444개).
            // 그래서 Play Console 의 "디버그 기호가 업로드되지 않았습니다" 경고는 사라지지 않는다.
            // 출시를 막는 경고는 아니다. 자체 네이티브 코드가 생기면 그때부터 효과가 있다.
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    androidResources {
        // 앱 자체 문자열은 한국어뿐이다. 라이브러리가 끌고 오는 나머지 번역을 제거한다.
        localeFilters += listOf("ko")
    }

    packaging {
        resources {
            excludes += listOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/*.version",
                "/META-INF/com/android/build/gradle/*",
                "DebugProbesKt.bin",
                "kotlin-tooling-metadata.json"
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

// release 산출물을 만들려는데 서명 정보가 없으면 조용히 미서명 AAB 가 나온다. 빌드 시작 시점에 경고한다.
if (!hasReleaseSigning &&
    gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }
) {
    logger.warn(
        "[MoyeoTrip] release 서명 정보가 없어 미서명 산출물이 생성된다. " +
            "keystore.properties 또는 MOYEO_RELEASE_* 환경변수를 설정할 것 (Play 업로드 불가)."
    )
}
