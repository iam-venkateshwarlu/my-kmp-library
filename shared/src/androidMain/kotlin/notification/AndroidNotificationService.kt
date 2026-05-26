package notification

import com.google.firebase.messaging.FirebaseMessaging

/**
 * Android `actual` implementation of [NotificationService].
 *
 * Uses the Firebase Messaging SDK to:
 *  - retrieve the FCM device token
 *  - register a foreground message listener (see [MyFirebaseMessagingService])
 *
 * Background / terminated notifications are handled automatically by the
 * Firebase SDK and the OS notification tray — no extra code needed here.
 */
actual class NotificationService actual constructor() {

    /**
     * Fetches the current FCM registration token asynchronously.
     * The token is logged in Logcat with the tag "FCMToken" for easy retrieval
     * during development.
     */
    actual fun getToken(onResult: (String?) -> Unit) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    android.util.Log.d("FCMToken", "Token: $token")
                    onResult(token)
                } else {
                    android.util.Log.e("FCMToken", "Failed to get token", task.exception)
                    onResult(null)
                }
            }
    }

    /**
     * Registers a foreground message listener.
     *
     * The actual message interception happens inside [MyFirebaseMessagingService].
     * This function exposes a callback hook so the ViewModel can react to
     * foreground notifications without touching Android platform code directly.
     *
     * Usage: the [MyFirebaseMessagingService] posts events to [messageListeners];
     * [listenForMessages] simply adds the ViewModel callback to that list.
     */
    actual fun listenForMessages(onEvent: (PushNotificationEvent) -> Unit) {
        messageListeners.add(onEvent)
    }

    companion object {
        /**
         * Global listeners list — [MyFirebaseMessagingService.onMessageReceived]
         * iterates this list to deliver foreground events to all registered consumers.
         */
        internal val messageListeners = mutableListOf<(PushNotificationEvent) -> Unit>()

        /** Called by [MyFirebaseMessagingService] when a foreground message arrives. */
        fun dispatchMessage(event: PushNotificationEvent) {
            messageListeners.forEach { it(event) }
        }
    }
}
