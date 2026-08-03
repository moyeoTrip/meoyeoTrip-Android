package kr.hanchae.moyeotrip

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kr.hanchae.moyeotrip.data.auth.KeystoreAuthSessionStore
import kr.hanchae.moyeotrip.domain.auth.AuthProvider
import kr.hanchae.moyeotrip.domain.auth.ServiceSession
import kr.hanchae.moyeotrip.domain.auth.SignupState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeystoreAuthSessionStoreTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var store: KeystoreAuthSessionStore

    @Before
    fun setUp() {
        store = KeystoreAuthSessionStore(context)
        store.clear()
    }

    @After
    fun tearDown() {
        store.clear()
    }

    @Test
    fun encryptedSessionSurvivesStoreRecreationAndCompletesProfile() {
        store.saveSignup(
            AuthProvider.KAKAO,
            ServiceSession("access-test", "refresh-test", SignupState.PROFILE_IMAGE_REQUIRED)
        )

        val restored = KeystoreAuthSessionStore(context)
        assertEquals("access-test", restored.current.accessToken)
        assertEquals("refresh-test", restored.current.refreshToken)
        assertEquals(AuthProvider.KAKAO, restored.current.provider)
        assertEquals(SignupState.PROFILE_IMAGE_REQUIRED, restored.current.signupState)

        restored.completeProfile()
        assertEquals(SignupState.SIGNUP_COMPLETE, KeystoreAuthSessionStore(context).current.signupState)

        restored.clear()
        assertNull(KeystoreAuthSessionStore(context).current.accessToken)
    }
}
