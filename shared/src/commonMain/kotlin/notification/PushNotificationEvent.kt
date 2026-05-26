package notification

/**
 * Represents an incoming Firebase Cloud Messaging (FCM) push notification event.
 *
 * This is a shared, platform-agnostic data model used across the KMP common layer.
 *
 * @param title  Notification title (from FCM payload).
 * @param body   Notification body text (from FCM payload).
 * @param data   Optional key-value data map sent with the FCM message.
 */
data class PushNotificationEvent(
    val title: String,
    val body: String,
    val data: Map<String, String> = emptyMap()
)
