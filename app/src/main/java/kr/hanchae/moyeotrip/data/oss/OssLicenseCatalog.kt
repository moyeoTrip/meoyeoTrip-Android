package kr.hanchae.moyeotrip.data.oss

import org.json.JSONObject

/**
 * 29-4 / 29-4a 가 보여주는 오픈소스 고지 항목 한 건.
 *
 * [licenseTextId] 가 없는 항목은 오픈소스가 아닌 자체 배포 SDK(카카오 지도 등)다 —
 * 전문을 지어내지 않고 라이선스 이름과 원문 URL만 보여준다(changeLog17).
 */
data class OssLicense(
    val name: String,
    val version: String,
    val license: String,
    val url: String,
    val licenseTextId: String? = null,
    val note: String? = null
) {
    /** 상세 화면 라우트 인자. 이름에 공백이 있어 그대로 쓰지 않는다. */
    val slug: String = name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
}

/**
 * 앱에 내장한 오픈소스 고지 정본(서버 호출 없음 · 오프라인 동작).
 *
 * 데이터는 `src/main/resources/oss/` 에 있고 워크스페이스 `docs/oss/oss-licenses.json` 의
 * **android 배열**을 그대로 옮긴 것이다. 라이선스 식별자는 추정하지 않는다.
 */
object OssLicenseCatalog {
    private const val CATALOG_PATH = "oss/oss-licenses.json"
    private const val LICENSE_TEXT_DIRECTORY = "oss/license-texts"

    val items: List<OssLicense> by lazy { parse(readResource(CATALOG_PATH).orEmpty()) }

    fun find(slug: String?): OssLicense? = items.firstOrNull { it.slug == slug } ?: items.firstOrNull()

    /** 라이선스 전문. 자체 배포 SDK 처럼 전문이 없는 항목은 null 이다. */
    fun licenseText(item: OssLicense): String? = item.licenseTextId
        ?.let { readResource("$LICENSE_TEXT_DIRECTORY/$it.txt") }
        ?.trimEnd()
        // 원문 파일의 첫 빈 줄만 걷어낸다 — 가운데 정렬된 제목의 들여쓰기는 원문 그대로 둔다
        ?.dropWhile { it == '\n' }
        ?.takeIf(String::isNotBlank)

    fun parse(json: String): List<OssLicense> = runCatching {
        val array = JSONObject(json).getJSONArray("items")
        (0 until array.length()).mapNotNull { index ->
            val entry = array.optJSONObject(index) ?: return@mapNotNull null
            val name = entry.optString("name").takeIf(String::isNotBlank) ?: return@mapNotNull null
            OssLicense(
                name = name,
                version = entry.optString("version"),
                license = entry.optString("license"),
                url = entry.optString("url"),
                licenseTextId = entry.optString("licenseTextId").takeIf(String::isNotBlank),
                note = entry.optString("note").takeIf(String::isNotBlank)
            )
        }
    }.getOrDefault(emptyList())

    private fun readResource(path: String): String? =
        javaClass.classLoader?.getResourceAsStream(path)?.use { it.readBytes().toString(Charsets.UTF_8) }
}
