package kr.hanchae.moyeotrip.data.auth

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
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
import kr.hanchae.moyeotrip.domain.auth.EmailAuthRequest
import kr.hanchae.moyeotrip.domain.auth.IdentityToken
import kr.hanchae.moyeotrip.domain.auth.IdentityTokenProvider
import kr.hanchae.moyeotrip.domain.auth.SocialLoginCancelledException

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

    /**
     * 이미 로그인된 Firebase 사용자에게서 idToken 만 다시 받는다 — 로그인 UI 를 열지 않는다.
     *
     * `forceRefresh = false` 여도 캐시된 토큰이 만료됐으면 SDK 가 알아서 새로 받아온다.
     * 굳이 강제 갱신을 걸면 매번 네트워크를 한 번 더 타므로 다른 호출부와 같은 값을 쓴다.
     */
    override suspend fun currentIdentity(provider: AuthProvider): IdentityToken? {
        val user = firebaseAuth.currentUser ?: return null
        val idToken = user.getIdToken(false).await().token?.takeIf(String::isNotBlank) ?: return null
        return IdentityToken(provider = provider, idToken = idToken, fcmToken = fcmTokenProvider())
    }

    override suspend fun acquireEmail(request: EmailAuthRequest): IdentityToken {
        require(request.email.contains('@')) { "이메일 주소를 확인해 주세요." }
        require(request.password.length >= MIN_PASSWORD_LENGTH) { "비밀번호는 6자 이상 입력해 주세요." }
        val result = signInOrCreate(request.email, request.password)
        val user = requireNotNull(result.user) { "Firebase 사용자 정보를 받지 못했어요." }
        return IdentityToken(
            provider = AuthProvider.EMAIL,
            idToken = requireNotNull(user.getIdToken(false).await().token) { "Firebase ID Token을 받지 못했어요." },
            fcmToken = fcmTokenProvider()
        )
    }

    /**
     * 로그인해 보고, 계정이 없으면 그대로 만든다. 소셜 로그인과 같은 흐름이다 —
     * 사용자가 "로그인 / 새 계정 만들기"를 먼저 고르지 않는다.
     *
     * 이 Firebase 프로젝트는 **Email Enumeration Protection** 이 켜져 있어서 로그인 실패가
     * "계정 없음"인지 "비밀번호 틀림"인지 구분되지 않는다(둘 다 INVALID_LOGIN_CREDENTIALS).
     * 그래서 두 번째 호출로 가린다 — 계정 만들기가 `FirebaseAuthUserCollisionException`(EMAIL_EXISTS)
     * 으로 막히면 계정은 있고 비밀번호가 틀린 것이다.
     * (실측 2026-08-29: 없는 이메일 로그인 → INVALID_LOGIN_CREDENTIALS, 있는 이메일 가입 → EMAIL_EXISTS)
     */
    private suspend fun signInOrCreate(email: String, password: String): AuthResult = try {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
    } catch (signInError: FirebaseAuthException) {
        if (!signInError.isSignInMiss()) throw signInError
        try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        } catch (collision: FirebaseAuthUserCollisionException) {
            // 계정은 있었다 → 처음 실패는 비밀번호가 틀린 것이다.
            throw IllegalStateException("비밀번호가 올바르지 않아요. 비밀번호 재설정을 이용해주세요.", collision)
        }
    }

    /** 로그인 실패가 "계정이 없거나 비밀번호가 틀림"인지. 그 외(잠금·네트워크 등)는 그대로 올린다. */
    private fun FirebaseAuthException.isSignInMiss(): Boolean =
        this is FirebaseAuthInvalidUserException || this is FirebaseAuthInvalidCredentialsException

    override suspend fun sendPasswordReset(email: String) {
        require(email.contains('@')) { "이메일 주소를 확인해 주세요." }
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    private suspend fun acquireGoogleToken(): IdentityToken {
        require(googleWebClientId.isNotBlank()) { "Google Web Client ID 설정이 필요해요." }
        val option = GetSignInWithGoogleOption.Builder(googleWebClientId).build()
        val result = try {
            credentialManager.getCredential(
                context = context,
                request = GetCredentialRequest.Builder().addCredentialOption(option).build()
            )
        } catch (cancelled: GetCredentialCancellationException) {
            // 계정 선택 시트를 사용자가 닫은 것이다 — 실패로 올리면 화면이 오류를 그린다(정본 R1).
            throw SocialLoginCancelledException(cancelled)
        }
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
        val result = try {
            if (pending != null) {
                pending.await()
            } else {
                firebaseAuth.startActivityForSignInWithProvider(activity, provider.build()).await()
            }
        } catch (error: FirebaseAuthException) {
            // 애플은 웹 인증 창을 띄운다. 사용자가 그 창을 닫으면 SDK 가 취소 코드를 준다(정본 R1).
            if (error.isUserCancellation()) throw SocialLoginCancelledException(error)
            throw error
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

                // 취소는 실패가 아니다. SDK 원문("user canceled")이 화면에 새지 않도록 여기서 갈라낸다(정본 R1).
                error.isKakaoCancellation() -> continuation.resumeWithException(
                    SocialLoginCancelledException(error)
                )

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
                // 카카오톡 로그인을 사용자가 취소했으면 카카오계정 로그인으로 넘기지 않는다 —
                // 그만두겠다는 뜻인데 다른 로그인 창을 또 띄우면 빠져나갈 수 없다.
                if (error.isKakaoCancellation()) {
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

/** 카카오 SDK 의 취소 판정 — 로그인 창을 사용자가 뒤로 가기로 닫았을 때다(정본 R1). */
private fun Throwable?.isKakaoCancellation(): Boolean = this is ClientError && reason == ClientErrorCause.Cancelled

/**
 * 애플(웹 인증 창) 취소 판정.
 * SDK 가 예외 형이 아니라 오류 코드 문자열로만 구분해 주므로 코드로 본다.
 */
private fun FirebaseAuthException.isUserCancellation(): Boolean =
    errorCode in setOf("ERROR_WEB_CONTEXT_CANCELED", "ERROR_USER_CANCELLED")

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}
