package kr.hanchae.moyeotrip.data.auth

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kr.hanchae.moyeotrip.domain.auth.AuthProvider
import kr.hanchae.moyeotrip.domain.auth.EmailAuthAction
import kr.hanchae.moyeotrip.domain.auth.EmailAuthRequest
import kr.hanchae.moyeotrip.domain.auth.IdentityToken
import kr.hanchae.moyeotrip.domain.auth.IdentityTokenProvider

class FirebaseIdentityTokenProvider(
    private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val googleWebClientId: String,
    private val kakaoNativeAppKey: String,
    private val kakaoCustomTokenExchanger: KakaoCustomTokenExchanger,
    private val fcmTokenProvider: suspend () -> String? = { null }
) : IdentityTokenProvider {
    private val credentialManager = CredentialManager.create(context)

    override suspend fun acquire(provider: AuthProvider): IdentityToken = when (provider) {
        AuthProvider.GOOGLE -> acquireGoogleToken()
        AuthProvider.KAKAO -> acquireKakaoToken()
        AuthProvider.APPLE -> acquireAppleToken()
        AuthProvider.EMAIL -> error("이메일 로그인 정보를 입력해 주세요.")
    }

    override suspend fun acquireEmail(request: EmailAuthRequest): IdentityToken {
        require(request.email.contains('@')) { "이메일 주소를 확인해 주세요." }
        require(request.password.length >= MIN_PASSWORD_LENGTH) { "비밀번호는 6자 이상 입력해 주세요." }
        val result = when (request.action) {
            EmailAuthAction.SIGN_IN -> firebaseAuth.signInWithEmailAndPassword(request.email, request.password).await()

            EmailAuthAction.CREATE_ACCOUNT ->
                firebaseAuth.createUserWithEmailAndPassword(request.email, request.password).await()
        }
        val user = requireNotNull(result.user) { "Firebase 사용자 정보를 받지 못했어요." }
        return IdentityToken(
            provider = AuthProvider.EMAIL,
            idToken = requireNotNull(user.getIdToken(false).await().token) { "Firebase ID Token을 받지 못했어요." },
            fcmToken = fcmTokenProvider()
        )
    }

    override suspend fun sendPasswordReset(email: String) {
        require(email.contains('@')) { "이메일 주소를 확인해 주세요." }
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    private suspend fun acquireGoogleToken(): IdentityToken {
        require(googleWebClientId.isNotBlank()) { "Google Web Client ID 설정이 필요해요." }
        val option = GetSignInWithGoogleOption.Builder(googleWebClientId).build()
        val result = credentialManager.getCredential(
            context = context,
            request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        )
        val credential = result.credential
        require(
            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) { "Google 계정 정보를 확인하지 못했어요." }
        val googleToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val authResult = firebaseAuth.signInWithCredential(GoogleAuthProvider.getCredential(googleToken, null)).await()
        val user = requireNotNull(authResult.user) { "Firebase 사용자 정보를 받지 못했어요." }
        return IdentityToken(
            provider = AuthProvider.GOOGLE,
            idToken = requireNotNull(user.getIdToken(false).await().token) { "Firebase ID Token을 받지 못했어요." },
            fcmToken = fcmTokenProvider()
        )
    }

    private suspend fun acquireKakaoToken(): IdentityToken {
        require(kakaoNativeAppKey.isNotBlank()) { "Kakao Native App Key 설정이 필요해요." }
        val kakaoToken = acquireKakaoOAuthToken()
        val firebaseCustomToken = kakaoCustomTokenExchanger.exchange(kakaoToken.accessToken)
        val result = firebaseAuth.signInWithCustomToken(firebaseCustomToken).await()
        val user = requireNotNull(result.user) { "Firebase 사용자 정보를 받지 못했어요." }
        return IdentityToken(
            provider = AuthProvider.KAKAO,
            idToken = requireNotNull(user.getIdToken(false).await().token) { "Firebase ID Token을 받지 못했어요." },
            fcmToken = fcmTokenProvider()
        )
    }

    private suspend fun acquireAppleToken(): IdentityToken {
        val activity = context.findActivity()
            ?: error("Apple 로그인을 표시할 Activity를 찾지 못했어요.")
        val provider = OAuthProvider.newBuilder("apple.com")
        val pending = firebaseAuth.pendingAuthResult
        val result = if (pending != null) {
            pending.await()
        } else {
            firebaseAuth.startActivityForSignInWithProvider(activity, provider.build()).await()
        }
        val user = requireNotNull(result.user) { "Firebase 사용자 정보를 받지 못했어요." }
        return IdentityToken(
            provider = AuthProvider.APPLE,
            idToken = requireNotNull(user.getIdToken(false).await().token) { "Firebase ID Token을 받지 못했어요." },
            fcmToken = fcmTokenProvider()
        )
    }

    private suspend fun acquireKakaoOAuthToken(): OAuthToken = suspendCancellableCoroutine { continuation ->
        val callback: (OAuthToken?, Throwable?) -> Unit = callback@{ token, error ->
            if (!continuation.isActive) return@callback
            when {
                token != null -> continuation.resume(token)
                error != null -> continuation.resumeWithException(error)
                else -> continuation.resumeWithException(IllegalStateException("카카오 인증 결과를 받지 못했어요."))
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) talkLogin@{ token, error ->
                if (token != null) {
                    callback(token, null)
                    return@talkLogin
                }
                if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                    callback(null, error)
                    return@talkLogin
                }
                UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> continuation.resume(result) }
        addOnFailureListener { error -> continuation.resumeWithException(error) }
        addOnCanceledListener { continuation.cancel() }
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}
