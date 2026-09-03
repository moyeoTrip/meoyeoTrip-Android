package kr.hanchae.moyeotrip.data.auth

import java.io.BufferedReader
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.hanchae.moyeotrip.domain.auth.AuthGateway
import kr.hanchae.moyeotrip.domain.auth.AuthProvider
import kr.hanchae.moyeotrip.domain.auth.IdentityToken
import kr.hanchae.moyeotrip.domain.auth.LoginResult
import kr.hanchae.moyeotrip.domain.auth.NicknameCandidate
import kr.hanchae.moyeotrip.domain.auth.NicknameCandidateResponse
import kr.hanchae.moyeotrip.domain.auth.ProfileImageCandidate
import kr.hanchae.moyeotrip.domain.auth.ProfileImageCandidates
import kr.hanchae.moyeotrip.domain.auth.ProfileImageSelectionResult
import kr.hanchae.moyeotrip.domain.auth.ServiceSession
import kr.hanchae.moyeotrip.domain.auth.SignupGateStage
import kr.hanchae.moyeotrip.domain.auth.SignupInput
import kr.hanchae.moyeotrip.domain.auth.SignupState
import org.json.JSONArray
import org.json.JSONObject

/**
 * 인증 API 오류 — 본문은 `{"code": 40918, "errorMessage": "…"}` 형태다.
 * [statusCode] 만으로는 갈라지지 않는 계약이 있어(409 하나에 40902·40918·40919 가 모두 걸린다)
 * 오류 코드도 같이 들고 다닌다.
 */
class AuthApiException(val statusCode: Int, override val message: String, val errorCode: Int? = null) :
    Exception(message) {
    /** 가입이 아직 안 끝났다는 신호. 토큰 재발급 대상이 아니다(정본 R1). */
    val signupGate: SignupGateStage? get() = SignupGateStage.ofCode(errorCode)

    /** 프로필 이미지 API 3종의 `40919` — 이미 끝난 단계다. 오류로 띄우지 않는다(정본 R2). */
    val profileImageAlreadySet: Boolean
        get() = errorCode == SignupGateStage.PROFILE_IMAGE_ALREADY_SET_CODE
}

class HttpAuthGateway(
    baseUrl: String,
    private val connectionFactory: (URL) -> HttpURLConnection = { url ->
        url.openConnection() as HttpURLConnection
    }
) : AuthGateway {
    private val rootUrl = baseUrl.trimEnd('/')

    override suspend fun login(identity: IdentityToken): LoginResult {
        val json = request(
            method = "POST",
            path = "/api/v1/auth/login",
            body = identity.toLoginJson()
        )
        return LoginResult(
            accessToken = json.nullableString("accessToken"),
            refreshToken = json.nullableString("refreshToken"),
            isNewUser = json.getBoolean("isNewUser"),
            signupState = json.signupState(),
            providerType = AuthProvider.valueOf(json.getString("providerType"))
        )
    }

    override suspend fun nicknameCandidates(): NicknameCandidateResponse {
        val json = request(method = "POST", path = "/api/v1/auth/nickname-candidates")
        return NicknameCandidateResponse(
            selectionToken = json.getString("selectionToken"),
            candidates = json.getJSONArray("candidates").mapObjects { candidate ->
                NicknameCandidate(
                    nickname = candidate.getString("nickname"),
                    adjective = candidate.getString("adjective"),
                    animal = candidate.getString("animal"),
                    color = candidate.getString("color"),
                    description = candidate.optString(
                        "description",
                        "함께 천천히 경북을 둘러보는 여행자예요"
                    )
                )
            }
        )
    }

    override suspend fun signup(identity: IdentityToken, input: SignupInput): ServiceSession {
        val body = signupRequestBody(identity, input)
        val json = request(method = "POST", path = "/api/v1/auth/signup", body = body)
        return ServiceSession(
            accessToken = json.getString("accessToken"),
            refreshToken = json.getString("refreshToken"),
            signupState = json.signupState()
        )
    }

    override suspend fun profileImages(accessToken: String): ProfileImageCandidates {
        val json = request(
            method = "GET",
            path = "/api/v1/users/me/profile-images",
            accessToken = accessToken
        )
        return json.toProfileImageCandidates()
    }

    override suspend fun generateProfileImage(accessToken: String): ProfileImageCandidates {
        val json = request(
            method = "POST",
            path = "/api/v1/users/me/profile-images",
            accessToken = accessToken,
            readTimeoutMillis = PROFILE_IMAGE_READ_TIMEOUT_MILLIS
        )
        return ProfileImageCandidates(
            candidates = listOf(json.getJSONObject("candidate").toProfileImageCandidate()),
            generationCount = json.getInt("generationCount"),
            remainingGenerationCount = json.getInt("remainingGenerationCount"),
            signupState = json.signupState()
        )
    }

    override suspend fun selectProfileImage(accessToken: String, profileImageId: Long): ProfileImageSelectionResult {
        val json = request(
            method = "PUT",
            path = "/api/v1/users/me/profile-image",
            body = JSONObject().put("profileImageId", profileImageId),
            accessToken = accessToken
        )
        return ProfileImageSelectionResult(
            selectedImage = json.getJSONObject("selectedImage").toProfileImageCandidate(),
            signupState = json.signupState()
        )
    }

    override suspend fun linkedProviders(accessToken: String): Set<AuthProvider> {
        val json = request(
            method = "GET",
            path = "/api/v1/auth/providers",
            accessToken = accessToken
        )
        return json.toProviders()
    }

    override suspend fun linkProvider(accessToken: String, identity: IdentityToken): Set<AuthProvider> {
        val json = request(
            method = "POST",
            path = "/api/v1/auth/providers",
            body = identity.toLoginJson(),
            accessToken = accessToken
        )
        return json.toProviders()
    }

    override suspend fun refresh(refreshToken: String): ServiceSession {
        val json = request(
            method = "POST",
            path = "/api/v1/auth/refresh",
            body = JSONObject().put("refreshToken", refreshToken)
        )
        return ServiceSession(
            accessToken = json.getString("accessToken"),
            refreshToken = json.getString("refreshToken"),
            signupState = json.signupState()
        )
    }

    override suspend fun withdraw(accessToken: String) {
        request(
            method = "DELETE",
            path = "/api/v1/users/me",
            accessToken = accessToken
        )
    }

    private suspend fun request(
        method: String,
        path: String,
        body: JSONObject? = null,
        accessToken: String? = null,
        readTimeoutMillis: Int = READ_TIMEOUT_MILLIS
    ): JSONObject = withContext(Dispatchers.IO) {
        val connection = connectionFactory(URL("$rootUrl$path"))
        try {
            connection.requestMethod = method
            connection.connectTimeout = CONNECT_TIMEOUT_MILLIS
            connection.readTimeout = readTimeoutMillis
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            if (!accessToken.isNullOrBlank()) {
                connection.setRequestProperty("Authorization", "Bearer $accessToken")
            }
            if (body != null) {
                connection.doOutput = true
                connection.outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                    writer.write(body.toString())
                }
            }

            val statusCode = connection.responseCode
            val responseText = if (statusCode == HttpURLConnection.HTTP_NO_CONTENT) {
                ""
            } else {
                connection.responseStream(statusCode)?.readTextSafely().orEmpty()
            }
            if (statusCode !in 200..299) {
                throw AuthApiException(
                    statusCode,
                    responseText.apiErrorMessage(statusCode),
                    responseText.apiErrorCode()
                )
            }
            if (responseText.isBlank()) JSONObject() else JSONObject(responseText)
        } finally {
            connection.disconnect()
        }
    }

    private fun IdentityToken.toLoginJson(): JSONObject = JSONObject().apply {
        put("idToken", idToken)
        fcmToken?.takeIf(String::isNotBlank)?.let { put("fcmToken", it) }
    }

    companion object {
        private const val CONNECT_TIMEOUT_MILLIS = 8_000
        private const val READ_TIMEOUT_MILLIS = 15_000
        private const val PROFILE_IMAGE_READ_TIMEOUT_MILLIS = 180_000
    }
}

internal fun signupRequestFields(identity: IdentityToken, input: SignupInput): Map<String, String> = buildMap {
    put("idToken", identity.idToken)
    put("nicknameSelectionToken", input.nicknameSelectionToken)
    put("nickname", input.nickname)
    put("gender", input.gender.apiValue)
    put("birthDate", input.birthDate)
    // 공백만 있는 토큰은 서버가 400 40016 으로 막는다. 없으면 필드 자체를 빼는 것이 계약이다(정본 R6).
    identity.fcmToken?.takeIf(String::isNotBlank)?.let { put("fcmToken", it) }
}

/**
 * 회원가입 요청 본문.
 *
 * `agreedTermIds` 는 배열이라 [signupRequestFields] 의 `Map<String, String>` 으로는 담을 수 없다.
 * 문자열 필드는 그대로 재사용하고 배열만 여기서 얹는다.
 *
 * 취향 두 필드는 **선택 항목**이다. 고르지 않았으면 빈 배열 대신 필드를 아예 빼서
 * "선택 안 함"과 "빈 목록으로 덮어쓰기"가 서버에서 갈리지 않게 한다 — 서버는 값이 있을 때만 id 를 검증한다.
 */
internal fun signupRequestBody(identity: IdentityToken, input: SignupInput): JSONObject =
    JSONObject(signupRequestFields(identity, input)).apply {
        put("agreedTermIds", JSONArray(input.agreedTermIds))
        if (input.travelStyleIds.isNotEmpty()) put("travelStyleIds", JSONArray(input.travelStyleIds))
        if (input.interestedRegionIds.isNotEmpty()) {
            put("interestedRegionIds", JSONArray(input.interestedRegionIds))
        }
    }

private fun HttpURLConnection.responseStream(statusCode: Int): InputStream? =
    if (statusCode in 200..299) inputStream else errorStream

private fun InputStream.readTextSafely(): String = bufferedReader().use(BufferedReader::readText)

private fun String.apiErrorMessage(statusCode: Int): String {
    if (isBlank()) return "서버 요청에 실패했어요. ($statusCode)"
    val json = runCatching { JSONObject(this) }.getOrNull()
    return json?.nullableString("errorMessage")
        ?: json?.nullableString("message")
        ?: json?.nullableString("detail")
        ?: json?.nullableString("error")
        ?: "서버 요청에 실패했어요. ($statusCode)"
}

/** 오류 본문의 `code`. 409 하나에 40902·40918·40919 가 몰려 있어 상태 코드만으로는 갈라지지 않는다. */
private fun String.apiErrorCode(): Int? = runCatching { JSONObject(this) }
    .getOrNull()
    ?.takeIf { it.has("code") && !it.isNull("code") }
    ?.optInt("code")

private fun JSONObject.nullableString(key: String): String? =
    if (!has(key) || isNull(key)) null else getString(key).takeIf(String::isNotBlank)

private fun JSONObject.signupState(): SignupState = SignupState.valueOf(getString("signupState"))

private fun JSONObject.toProfileImageCandidates(): ProfileImageCandidates = ProfileImageCandidates(
    candidates = getJSONArray("candidates").mapObjects(JSONObject::toProfileImageCandidate),
    generationCount = getInt("generationCount"),
    remainingGenerationCount = getInt("remainingGenerationCount"),
    signupState = signupState()
)

private fun JSONObject.toProfileImageCandidate(): ProfileImageCandidate = ProfileImageCandidate(
    profileImageId = getLong("profileImageId"),
    profileImageUrl = getString("profileImageUrl"),
    selected = getBoolean("selected")
)

private fun JSONObject.toProviders(): Set<AuthProvider> = getJSONArray("providers")
    .mapStrings(AuthProvider::valueOf)

private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
    List(length()) { index -> transform(getJSONObject(index)) }

private fun <T> JSONArray.mapStrings(transform: (String) -> T): Set<T> =
    List(length()) { index -> transform(getString(index)) }.toSet()
