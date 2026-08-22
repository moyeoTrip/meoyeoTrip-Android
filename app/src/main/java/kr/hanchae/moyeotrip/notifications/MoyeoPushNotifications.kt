@file:Suppress("DEPRECATION")

package kr.hanchae.moyeotrip.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kr.hanchae.moyeotrip.MainActivity
import kr.hanchae.moyeotrip.R

const val PUSH_ROUTE_EXTRA = "moyeo_push_route"
const val PUSH_CHANNEL_ID = "moyeo_updates"

internal fun pushRoute(data: Map<String, String>): String = when (
    (data["screen"] ?: data["route"] ?: data["destination"]).orEmpty().lowercase()
) {
    "notification", "notifications", "notification-center" -> "notifications"
    "explore", "search", "course" -> "explore"
    "meeting", "meetings", "chat", "chatroom" -> "meetings"
    "feed", "post" -> "feed"
    "my", "profile", "settings" -> "my"
    else -> "home"
}

data class PushNavigationEvent(val id: Long, val route: String)

internal fun nextPushNavigationEvent(current: PushNavigationEvent?, route: String?): PushNavigationEvent? = route?.let {
    PushNavigationEvent(id = (current?.id ?: 0L) + 1L, route = it)
}

internal fun consumePushNavigationEvent(current: PushNavigationEvent?, handledEventId: Long): PushNavigationEvent? =
    current?.takeUnless { it.id == handledEventId }

object MoyeoPushNotificationChannels {
    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                PUSH_CHANNEL_ID,
                context.getString(R.string.push_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.push_channel_description)
            }
        )
    }
}

object MoyeoPushTokenStore {
    private const val PREFERENCES = "moyeo_push"
    private const val TOKEN = "fcm_token"
    private const val PENDING_REGISTRATION = "pending_registration"

    fun save(context: Context, token: String) {
        if (token.isBlank()) return
        val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val pendingRegistration = tokenRegistrationPending(
            storedToken = preferences.getString(TOKEN, null),
            incomingToken = token,
            wasPending = preferences.getBoolean(PENDING_REGISTRATION, false)
        )
        preferences
            .edit()
            .putString(TOKEN, token)
            .putBoolean(PENDING_REGISTRATION, pendingRegistration)
            .apply()
    }

    fun read(context: Context): String? = context
        .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        .getString(TOKEN, null)

    fun needsRegistration(context: Context): Boolean = context
        .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        .getBoolean(PENDING_REGISTRATION, false)

    fun markRegistered(context: Context, token: String) {
        val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        if (preferences.getString(TOKEN, null) != token) return
        preferences.edit().putBoolean(PENDING_REGISTRATION, false).apply()
    }
}

internal fun tokenRegistrationPending(storedToken: String?, incomingToken: String, wasPending: Boolean): Boolean =
    storedToken != incomingToken || wasPending

class MoyeoFirebaseMessagingService : FirebaseMessagingService() {
    @Suppress("OVERRIDE_DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        MoyeoPushTokenStore.save(this, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        MoyeoPushNotificationChannels.create(this)
        val title = message.notification?.title ?: message.data["title"] ?: getString(R.string.app_name)
        val body = message.notification?.body ?: message.data["body"] ?: return
        val route = pushRoute(message.data)
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(PUSH_ROUTE_EXTRA, route)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            route.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, PUSH_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_moyeo_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(getColor(R.color.forest_green))
            .build()
        getSystemService(NotificationManager::class.java)
            .notify(message.messageId?.hashCode() ?: System.currentTimeMillis().toInt(), notification)
    }
}
