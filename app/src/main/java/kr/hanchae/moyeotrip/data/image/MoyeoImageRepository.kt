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

internal object MoyeoImageRepository {
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

    suspend fun load(context: Context, url: String): Bitmap? = withContext(Dispatchers.IO) {
        if (!url.startsWith("https://") && !url.startsWith("http://")) return@withContext null
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
