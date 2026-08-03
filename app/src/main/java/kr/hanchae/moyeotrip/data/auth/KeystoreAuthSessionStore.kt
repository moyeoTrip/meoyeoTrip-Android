package kr.hanchae.moyeotrip.data.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kr.hanchae.moyeotrip.domain.auth.AuthProvider
import kr.hanchae.moyeotrip.domain.auth.AuthSessionState
import kr.hanchae.moyeotrip.domain.auth.AuthSessionStore
import kr.hanchae.moyeotrip.domain.auth.LoginResult
import kr.hanchae.moyeotrip.domain.auth.ServiceSession
import kr.hanchae.moyeotrip.domain.auth.SignupState

class KeystoreAuthSessionStore(context: Context) : AuthSessionStore {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override val current: AuthSessionState
        get() = runCatching {
            AuthSessionState(
                accessToken = decrypt(preferences.getString(KEY_ACCESS_TOKEN, null)),
                refreshToken = decrypt(preferences.getString(KEY_REFRESH_TOKEN, null)),
                signupState = preferences.getString(KEY_SIGNUP_STATE, null)?.let(SignupState::valueOf),
                provider = preferences.getString(KEY_PROVIDER, null)?.let(AuthProvider::valueOf)
            )
        }.getOrElse {
            clear()
            AuthSessionState()
        }

    override fun saveLogin(provider: AuthProvider, result: LoginResult) {
        save(
            AuthSessionState(
                accessToken = result.accessToken,
                refreshToken = result.refreshToken,
                signupState = result.signupState,
                provider = provider
            )
        )
    }

    override fun saveSignup(provider: AuthProvider, session: ServiceSession) {
        save(
            AuthSessionState(
                accessToken = session.accessToken,
                refreshToken = session.refreshToken,
                signupState = session.signupState,
                provider = provider
            )
        )
    }

    override fun completeProfile() {
        save(current.copy(signupState = SignupState.SIGNUP_COMPLETE))
    }

    override fun clear() {
        preferences.edit().clear().apply()
    }

    private fun save(state: AuthSessionState) {
        preferences.edit()
            .putString(KEY_ACCESS_TOKEN, encrypt(state.accessToken))
            .putString(KEY_REFRESH_TOKEN, encrypt(state.refreshToken))
            .putString(KEY_SIGNUP_STATE, state.signupState?.name)
            .putString(KEY_PROVIDER, state.provider?.name)
            .apply()
    }

    private fun encrypt(value: String?): String? {
        value ?: return null
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return listOf(cipher.iv, encrypted).joinToString(SEPARATOR) {
            Base64.encodeToString(it, Base64.NO_WRAP)
        }
    }

    private fun decrypt(value: String?): String? {
        value ?: return null
        val parts = value.split(SEPARATOR, limit = 2)
        require(parts.size == 2)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            secretKey(),
            GCMParameterSpec(GCM_TAG_LENGTH_BITS, Base64.decode(parts[0], Base64.NO_WRAP))
        )
        return cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)).toString(Charsets.UTF_8)
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            generateKey()
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "moyeo_auth_session"
        const val KEY_ALIAS = "moyeo_auth_session_key"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_SIGNUP_STATE = "signup_state"
        const val KEY_PROVIDER = "provider"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH_BITS = 128
        const val SEPARATOR = ":"
    }
}
