package kr.hanchae.moyeotrip.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kr.hanchae.moyeotrip.BuildConfig

internal object MoyeoImageRepository {
    /** 상대경로 썸네일 앞에 붙일 CDN 호스트. `MOYEO_CDN_BASE_URL` 로 바꿀 수 있다. */
    private val CDN_BASE_URL: String = BuildConfig.CDN_BASE_URL
    const val MAXIMUM_ATTEMPTS = 3

    private val memoryCache = object : LruCache<String, Bitmap>(24 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }
    private val downloadLocks = ConcurrentHashMap<String, Mutex>()

    internal fun cacheKey(url: String): String {
        val decodedName = runCatching { URI(url).path.substringAfterLast('/') }.getOrDefault("")
        val source = decodedName.ifBlank { "image" }
        return source.map { character ->
            if (character.isLetterOrDigit() || character in "-_.") character else '_'
        }.joinToString("")
    }

    /**
     * 서버가 주는 이미지 값을 **내려받을 수 있는 절대 URL** 로 만든다.
     *
     * `travel-courses` 계열(`GET /travel-courses/public` · `/{id}` · `places[].thumbnail`)은
     * 썸네일을 상대경로(`tourism/image/….webp`)로 주는데, `tourism-contents` 는 절대 URL 을 준다.
     * 상대경로를 그대로 내려받으려 하면 전부 실패해서 09 홈·12-1 검색·14 코스 상세의 사진이
     * 통째로 자리표시자로 떨어졌다. 상대경로는 CDN 호스트를 앞에 붙이고, 이미 절대 URL 인 값은
     * 그대로 통과시킨다 — 서버가 절대 URL 로 통일하면(§2-9) 이 함수는 저절로 무해해진다.
     *
     * 화면마다 흩어 놓지 않는다. 이미지를 실제로 내려받는 자리가 여기 하나뿐이라 여기서 한 번만 붙인다.
     */
    fun absoluteUrl(raw: String?): String? {
        val value = raw?.trim()?.takeIf(String::isNotEmpty) ?: return null
        if (value.startsWith("https://") || value.startsWith("http://")) return value
        // 스킴 없이 호스트만 온 값(`//host/path`)이나 데이터 URI 는 우리가 고칠 대상이 아니다.
        if (value.startsWith("//") || value.contains(':')) return null
        return CDN_BASE_URL.trimEnd('/') + "/" + value.trimStart('/')
    }

    /**
     * 이미 메모리에 올라와 있는 비트맵. 없으면 null — 네트워크도 디스크도 건드리지 않는다.
     *
     * 첫 프레임을 위한 값이다. 캐시에 있는데도 코루틴이 돌 때까지 자리표시자를 그리면
     * 탭을 오갈 때마다 기본 썸네일이 한 번 깜빡인다(정본 R5).
     */
    fun cachedOrNull(url: String?): Bitmap? = absoluteUrl(url)?.let { memoryCache.get(cacheKey(it)) }

    suspend fun load(context: Context, rawUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        val url = absoluteUrl(rawUrl) ?: return@withContext null
        val key = cacheKey(url)
        memoryCache.get(key)?.let { return@withContext it }

        val lock = downloadLocks.getOrPut(key) { Mutex() }
        lock.withLock {
            loadCachedOrDownload(context, url, key)
        }
    }

    private suspend fun loadCachedOrDownload(context: Context, url: String, key: String): Bitmap? {
        memoryCache.get(key)?.let { return it }
        val directory = File(context.cacheDir, "moyeo_images").apply { mkdirs() }
        val cacheFile = File(directory, key)
        if (cacheFile.isFile) {
            BitmapFactory.decodeFile(cacheFile.path)?.let { bitmap ->
                memoryCache.put(key, bitmap)
                return bitmap
            }
        }

        repeat(MAXIMUM_ATTEMPTS) { index ->
            val bitmap = runCatching { download(url, cacheFile) }.getOrNull()
            if (bitmap != null) {
                memoryCache.put(key, bitmap)
                return bitmap
            }
            if (index < MAXIMUM_ATTEMPTS - 1) delay((300L * (index + 1)).milliseconds)
        }
        return null
    }

    private fun download(url: String, cacheFile: File): Bitmap {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 15_000
        connection.readTimeout = 30_000
        connection.useCaches = true
        connection.setRequestProperty("Accept", "image/*")
        return try {
            check(connection.responseCode in 200..299) { "Image request failed: ${connection.responseCode}" }
            val bytes = connection.inputStream.use { it.readBytes() }
            val bitmap = requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            val temporaryFile = File(cacheFile.parentFile, "${cacheFile.name}.tmp")
            temporaryFile.writeBytes(bytes)
            if (!temporaryFile.renameTo(cacheFile)) {
                temporaryFile.copyTo(cacheFile, overwrite = true)
                temporaryFile.delete()
            }
            bitmap
        } finally {
            connection.disconnect()
        }
    }
}
