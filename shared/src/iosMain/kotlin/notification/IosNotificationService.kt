package notification

/**
 * iOS `actual` implementation for [NotificationService].
 *
 * On iOS, Firebase SDK is typically integrated directly into the Xcode project
 * (via CocoaPods or SPM). This class provides hooks for the Swift layer to
 * pass tokens and messages into the KMP shared state.
 */
actual class NotificationService actual constructor() {

    /**
     * Returns the token that was previously set via [setToken].
     */
    actual fun getToken(onResult: (String?) -> Unit) {
        onResult(fcmToken)
    }

    /**
     * Registers a listener for foreground messages.
     * The Swift layer should call [dispatchMessage] when a message arrives.
     */
    actual fun listenForMessages(onEvent: (PushNotificationEvent) -> Unit) {
        messageListeners.add(onEvent)
    }

    companion object {
        private var fcmToken: String? = null
        private val messageListeners = mutableListOf<(PushNotificationEvent) -> Unit>()

        /**
         * Called from Swift (e.g., MessagingDelegate) when a new FCM token is obtained.
         */
        fun setToken(token: String?) {
            fcmToken = token
        }

        /**
         * Called from Swift when a message arrives while the app is in the foreground.
         * This helper takes primitive types to make calls from Swift easier.
         */
        fun dispatchMessage(title: String, body: String, data: Map<String, String>) {
            val event = PushNotificationEvent(
                title = title,
                body = body,
                data = data
            )
            messageListeners.forEach { it(event) }
        }
    }
}
