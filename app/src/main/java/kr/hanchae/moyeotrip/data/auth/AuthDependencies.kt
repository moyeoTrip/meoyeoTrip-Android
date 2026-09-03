@file:Suppress("DEPRECATION")

package kr.hanchae.moyeotrip.data.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.sdk.user.UserApiClient
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kr.hanchae.moyeotrip.BuildConfig
import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
import kr.hanchae.moyeotrip.data.profile.HttpUserProfileRepository
import kr.hanchae.moyeotrip.data.profile.UserProfileRepository
import kr.hanchae.moyeotrip.data.terms.HttpTermsRepository
import kr.hanchae.moyeotrip.data.terms.TermsRepository
import kr.hanchae.moyeotrip.domain.auth.AuthGateway
import kr.hanchae.moyeotrip.domain.auth.AuthProvider
import kr.hanchae.moyeotrip.domain.auth.AuthSessionStore
import kr.hanchae.moyeotrip.domain.auth.EmailAuthRequest
import kr.hanchae.moyeotrip.domain.auth.IdentityToken
import kr.hanchae.moyeotrip.domain.auth.IdentityTokenProvider
import kr.hanchae.moyeotrip.notifications.MoyeoPushTokenStore

data class AuthDependencies(
    val identityTokenProvider: IdentityTokenProvider,
    val authGateway: AuthGateway,
    val sessionStore: AuthSessionStore,
    val accountService: AuthAccountService,
    val userProfileStore: kr.hanchae.moyeotrip.domain.auth.UserProfileStore,
    /**
     * 약관 조회. **가입 플로우에서 쓰려면 인증 없이 부를 수 있어야 한다.**
     *
     * `LocalServerData` 는 로그인 완료 상태에서만 non-null 이라 가입 중에는 손이 닿지 않는다.
     * 약관 엔드포인트(`GET /api/v1/terms`, `GET /api/v1/terms/{termId}`)는 서버가 공개로 열어뒀다
     * (SecurityConfig 허용 목록, 토큰 없이 200).
     */
    val terms: TermsRepository,
    /**
     * 06-1 취향 후보(`GET /api/v1/users/me/profile/options`).
     *
     * **토큰 없이 200 이다**(2026-08-30 실서버 확인 — 서버가 공개로 열었다). 그래서 가입 전에도 부를 수 있고,
     * 클라이언트가 스타일·지역 표를 거울처럼 들고 있을 이유가 없어졌다(정본 2-2).
     * 이 통로로는 [UserProfileRepository.options] 만 쓴다 — 나머지 메서드는 토큰이 필요하다.
     */
    val profileOptions: UserProfileRepository
) {
    companion object {
        fun appDefault(context: Context): AuthDependencies {
            // 데모 게이트웨이 분기는 없다 — 인증은 어떤 빌드에서도 실서버를 탄다.
            val rawAuthGateway = HttpAuthGateway(BuildConfig.AUTH_API_BASE_URL)
            val sessionStore = KeystoreAuthSessionStore(context.applicationContext)
            val userProfileStore = PersistedUserProfileStore(context.applicationContext)
            val authGateway = RefreshingAuthGateway(rawAuthGateway, sessionStore)
            val identityTokenProvider = identityProvider(context)
            // 토큰을 붙이지 않는 공개 클라이언트다 — 가입 중에는 세션이 없다.
            val publicClient = MoyeoApiClient(BuildConfig.AUTH_API_BASE_URL, accessToken = { null })
            return AuthDependencies(
                identityTokenProvider = identityTokenProvider,
                authGateway = authGateway,
                sessionStore = sessionStore,
                accountService = AuthAccountService(
                    authGateway = authGateway,
                    sessionStore = sessionStore,
                    identityTokenProvider = identityTokenProvider,
                    clearProviderSessions = { clearIdentityProviderSessions(context.applicationContext) },
                    clearUserProfile = userProfileStore::clear
                ),
                userProfileStore = userProfileStore,
                terms = HttpTermsRepository(publicClient),
                profileOptions = HttpUserProfileRepository(publicClient)
            )
        }

        private fun identityProvider(context: Context): IdentityTokenProvider {
            if (!BuildConfig.FIREBASE_CONFIGURED) {
                return MissingFirebaseConfigurationIdentityTokenProvider()
            }
            val app = FirebaseApp.initializeApp(context)
                ?: error("Firebase 설정을 초기화하지 못했어요.")
            return FirebaseIdentityTokenProvider(
                context = context,
                firebaseAuth = FirebaseAuth.getInstance(app),
                googleWebClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID.ifBlank {
                    val resourceId = context.resources.getIdentifier(
                        "default_web_client_id",
                        "string",
                        context.packageName
                    )
                    if (resourceId == 0) "" else context.getString(resourceId)
                },
                kakaoNativeAppKey = BuildConfig.KAKAO_NATIVE_APP_KEY,
                kakaoCustomTokenExchanger = KakaoCustomTokenClient(BuildConfig.AUTH_API_BASE_URL),
                fcmTokenProvider = {
                    runCatching { FirebaseMessaging.getInstance().token.awaitResult() }
                        .onSuccess { MoyeoPushTokenStore.save(context, it) }
                        .getOrNull()
                        ?: MoyeoPushTokenStore.read(context)
                }
            )
        }
    }
}

private suspend fun clearIdentityProviderSessions(context: Context) {
    runCatching { FirebaseAuth.getInstance().signOut() }
    runCatching {
        CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
    }
    suspendCancellableCoroutine { continuation ->
        UserApiClient.instance.logout {
            if (continuation.isActive) continuation.resume(Unit)
        }
    }
}

private class MissingFirebaseConfigurationIdentityTokenProvider : IdentityTokenProvider {
    override suspend fun acquire(provider: AuthProvider): IdentityToken = missingConfiguration()

    override suspend fun acquireEmail(request: EmailAuthRequest): IdentityToken = missingConfiguration()

    override suspend fun sendPasswordReset(email: String): Unit = missingConfiguration()

    private fun <T> missingConfiguration(): T = error(
        "Android용 google-services.json이 필요해요. Firebase Console에서 내려받아 app 폴더에 추가해 주세요."
    )
}

private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitResult(): T =
    kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> continuation.resume(result) }
        addOnFailureListener { error -> continuation.resumeWith(Result.failure(error)) }
        addOnCanceledListener { continuation.cancel() }
    }
