package kr.hanchae.moyeotrip.data.notifications

import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
import kr.hanchae.moyeotrip.data.api.longOrNull
import kr.hanchae.moyeotrip.data.api.mapObjects
import kr.hanchae.moyeotrip.data.api.stringOrNull
import kr.hanchae.moyeotrip.data.rooms.RoomKickHistory
import org.json.JSONArray
import org.json.JSONObject

data class ServerNotification(
    val notificationId: Long,
    val type: String,
    val content: String,
    val chatRoomId: Long?,
    val referenceId: Long?,
    val read: Boolean,
    val createdAt: String
)

data class NotificationsPage(
    val notifications: List<ServerNotification>,
    val nextLastId: Long?,
    val hasNext: Boolean,
    val unreadCount: Int
)

data class NotificationSettings(
    val doNotDisturbEnabled: Boolean,
    val doNotDisturbStartTime: String?,
    val doNotDisturbEndTime: String?,
    val doNotDisturbDays: List<String>
)

data class NotificationSettingsUpdate(
    val chatNotificationMode: String,
    val recruitmentDeadlineEnabled: Boolean,
    val socialActivityEnabled: Boolean,
    val marketingEnabled: Boolean,
    val doNotDisturbEnabled: Boolean,
    val doNotDisturbStartTime: String?,
    val doNotDisturbEndTime: String?,
    val doNotDisturbDays: List<String>
)

interface NotificationRepository {
    suspend fun notifications(size: Int = 50, unreadOnly: Boolean = false): NotificationsPage

    suspend fun markRead(notificationId: Long)

    suspend fun markAllRead()

    suspend fun settings(): NotificationSettings

    suspend fun updateSettings(update: NotificationSettingsUpdate): NotificationSettings

    suspend fun kickHistory(notificationId: Long): RoomKickHistory
}

class HttpNotificationRepository(private val client: MoyeoApiClient) : NotificationRepository {
    override suspend fun notifications(size: Int, unreadOnly: Boolean): NotificationsPage {
        val json = client.getObject("/api/v1/notifications?size=$size&unreadOnly=$unreadOnly")
        return NotificationsPage(
            notifications = json.optJSONArray("notifications").orEmpty().mapObjects(JSONObject::toNotification),
            nextLastId = json.longOrNull("nextLastId"),
            hasNext = json.optBoolean("hasNext"),
            unreadCount = json.optInt("unreadCount")
        )
    }

    override suspend fun markRead(notificationId: Long) {
        client.send("PUT", "/api/v1/notifications/$notificationId/read")
    }

    override suspend fun markAllRead() {
        client.send("PUT", "/api/v1/notifications/read-all")
    }

    override suspend fun settings(): NotificationSettings =
        client.getObject("/api/v1/notifications/settings").toSettings()

    override suspend fun updateSettings(update: NotificationSettingsUpdate): NotificationSettings {
        val body = JSONObject()
            .put("chatNotificationMode", update.chatNotificationMode)
            .put("recruitmentDeadlineEnabled", update.recruitmentDeadlineEnabled)
            .put("socialActivityEnabled", update.socialActivityEnabled)
            .put("marketingEnabled", update.marketingEnabled)
            .put("doNotDisturbEnabled", update.doNotDisturbEnabled)
            .put("doNotDisturbStartTime", update.doNotDisturbStartTime)
            .put("doNotDisturbEndTime", update.doNotDisturbEndTime)
            .put("doNotDisturbDays", JSONArray(update.doNotDisturbDays))
        return client.sendForObject("PUT", "/api/v1/notifications/settings", body).toSettings()
    }

    override suspend fun kickHistory(notificationId: Long): RoomKickHistory {
        val json = client.getObject("/api/v1/notifications/$notificationId/kick-history")
        return RoomKickHistory(
            kickHistoryId = json.getLong("kickHistoryId"),
            roomId = json.getLong("roomId"),
            roomTitle = json.getString("roomTitle"),
            reason = json.optString("reason"),
            kickedAt = json.optString("kickedAt")
        )
    }
}

private fun JSONArray?.orEmpty(): JSONArray = this ?: JSONArray()

private fun JSONObject.toNotification() = ServerNotification(
    notificationId = getLong("notificationId"),
    type = optString("type"),
    content = optString("content"),
    chatRoomId = longOrNull("chatRoomId"),
    referenceId = longOrNull("referenceId"),
    read = optBoolean("read"),
    createdAt = optString("createdAt")
)

private fun JSONObject.toSettings() = NotificationSettings(
    doNotDisturbEnabled = optBoolean("doNotDisturbEnabled"),
    doNotDisturbStartTime = stringOrNull("doNotDisturbStartTime"),
    doNotDisturbEndTime = stringOrNull("doNotDisturbEndTime"),
    doNotDisturbDays = optJSONArray("doNotDisturbDays")?.let { days ->
        List(days.length()) { index -> days.getString(index) }
    }.orEmpty()
)
