package kr.hanchae.moyeotrip.data.terms

import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
import kr.hanchae.moyeotrip.data.api.mapObjects
import kr.hanchae.moyeotrip.data.api.stringOrNull
import org.json.JSONObject

data class TermSummary(val termId: Long, val title: String, val required: Boolean)

data class TermDetail(
    val termId: Long,
    val title: String,
    val required: Boolean,
    val version: String?,
    /** 서버 약관 본문은 마크다운이다. */
    val content: String
)

interface TermsRepository {
    suspend fun terms(): List<TermSummary>

    suspend fun term(termId: Long): TermDetail
}

class HttpTermsRepository(private val client: MoyeoApiClient) : TermsRepository {
    override suspend fun terms(): List<TermSummary> = client.getArray("/api/v1/terms").mapObjects { term ->
        TermSummary(
            termId = term.getLong("termId"),
            title = term.getString("title"),
            required = term.optBoolean("required")
        )
    }

    override suspend fun term(termId: Long): TermDetail {
        val json: JSONObject = client.getObject("/api/v1/terms/$termId")
        return TermDetail(
            termId = json.getLong("termId"),
            title = json.getString("title"),
            required = json.optBoolean("required"),
            version = json.stringOrNull("version"),
            content = json.optString("content")
        )
    }
}
