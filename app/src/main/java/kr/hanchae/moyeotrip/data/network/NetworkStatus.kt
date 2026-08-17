package kr.hanchae.moyeotrip.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class OfflineExperience {
    Online,
    NoCache,
    Cached
}

internal fun offlineExperience(isOnline: Boolean, hasCachedContent: Boolean): OfflineExperience = when {
    isOnline -> OfflineExperience.Online
    hasCachedContent -> OfflineExperience.Cached
    else -> OfflineExperience.NoCache
}

class AndroidNetworkMonitor(context: Context) {
    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
    private val _isOnline = MutableStateFlow(connectivityManager.hasValidatedInternet())

    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = refresh()

        override fun onLost(network: Network) = refresh()

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) = refresh()
    }

    init {
        connectivityManager.registerDefaultNetworkCallback(callback)
    }

    private fun refresh() {
        _isOnline.value = connectivityManager.hasValidatedInternet()
    }

    fun close() {
        runCatching { connectivityManager.unregisterNetworkCallback(callback) }
    }
}

class OfflineCacheStore(context: Context) {
    private val preferences = context.getSharedPreferences("moyeo_offline_cache", Context.MODE_PRIVATE)

    val hasCachedContent: Boolean
        get() = preferences.getBoolean(KEY_HAS_CACHE, false)

    fun recordSuccessfulLoad() {
        preferences.edit().putBoolean(KEY_HAS_CACHE, true).apply()
    }

    companion object {
        private const val KEY_HAS_CACHE = "has_cached_content"
    }
}

private fun ConnectivityManager.hasValidatedInternet(): Boolean {
    val capabilities = getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
