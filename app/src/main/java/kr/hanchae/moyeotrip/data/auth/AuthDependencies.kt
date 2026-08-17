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
import kr.hanchae.moyeotrip.domain.auth.AuthGateway
import kr.hanchae.moyeotrip.domain.auth.AuthProvider
import kr.hanchae.moyeotrip.domain.auth.AuthSessionStore
import kr.hanchae.moyeotrip.domain.auth.DemoAuthGateway
import kr.hanchae.moyeotrip.domain.auth.EmailAuthRequest
import kr.hanchae.moyeotrip.domain.auth.IdentityToken
import kr.hanchae.moyeotrip.domain.auth.IdentityTokenProvider
import kr.hanchae.moyeotrip.domain.auth.InMemoryAuthSessionStore
import kr.hanchae.moyeotrip.domain.auth.MockIdentityTokenProvider
import kr.hanchae.moyeotrip.notifications.MoyeoPushTokenStore

data class AuthDependencies(
    val identityTokenProvider: IdentityTokenProvider,
    val authGateway: AuthGateway,
    val sessionStore: AuthSessionStore,
    val accountService: AuthAccountService,
    val userProfileStore: kr.hanchae.moyeotrip.domain.auth.UserProfileStore
) {
    companion object {
        fun appDefault(context: Context): AuthDependencies {
            val rawAuthGateway = if (BuildConfig.AUTH_DEMO_MODE) {
                DemoAuthGateway()
            } else {
                HttpAuthGateway(BuildConfig.AUTH_API_BASE_URL)
            }
            val sessionStore = if (BuildConfig.AUTH_DEMO_MODE) {
                InMemoryAuthSessionStore()
            } else {
                KeystoreAuthSessionStore(context.applicationContext)
            }
            val userProfileStore = if (BuildConfig.AUTH_DEMO_MODE) {
                InMemoryUserProfileStore()
            } else {
                PersistedUserProfileStore(context.applicationContext)
            }
            val authGateway = RefreshingAuthGateway(rawAuthGateway, sessionStore)
            val identityTokenProvider = identityProvider(context)
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
                userProfileStore = userProfileStore
            )
        }

        fun demo(): AuthDependencies {
            val gateway = DemoAuthGateway()
            val sessionStore = InMemoryAuthSessionStore()
            val profileStore = InMemoryUserProfileStore()
            val identityProvider = MockIdentityTokenProvider()
            return AuthDependencies(
                identityTokenProvider = identityProvider,
                authGateway = gateway,
                sessionStore = sessionStore,
                accountService = AuthAccountService(
                    authGateway = gateway,
                    sessionStore = sessionStore,
                    identityTokenProvider = identityProvider,
                    clearUserProfile = profileStore::clear
                ),
                userProfileStore = profileStore
            )
        }

        private fun identityProvider(context: Context): IdentityTokenProvider {
            if (BuildConfig.AUTH_DEMO_MODE || !BuildConfig.FIREBASE_CONFIGURED) {
                return if (BuildConfig.AUTH_DEMO_MODE) {
                    MockIdentityTokenProvider()
                } else {
                    MissingFirebaseConfigurationIdentityTokenProvider()
                }
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
                    runCatching { currentMessagingToken().awaitResult() }
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

@Suppress("DEPRECATION")
private fun currentMessagingToken() = FirebaseMessaging.getInstance().token

private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitResult(): T =
    kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> continuation.resume(result) }
        addOnFailureListener { error -> continuation.resumeWith(Result.failure(error)) }
        addOnCanceledListener { continuation.cancel() }
    }
