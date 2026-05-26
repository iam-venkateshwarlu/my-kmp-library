package notification

/**
 * KMP expect declaration for the platform-specific notification service.
 *
 * Each platform provides an `actual` implementation:
 *  - Android → wraps Firebase Messaging SDK
 *  - iOS     → stub (APNs + Firebase iOS SDK wired in Xcode Swift layer)
 */
expect class NotificationService() {

    /**
     * Retrieves the current FCM device registration token.
     *
     * @param onResult Called with the token string, or `null` on failure.
     */
    fun getToken(onResult: (String?) -> Unit)

    /**
     * Registers a listener that is invoked whenever a foreground FCM message arrives.
     *
     * NOTE: Background / terminated notifications are handled entirely by the OS
     * (system tray on Android, APNs on iOS) and do NOT call this callback.
     *
     * @param onEvent Called with the parsed [PushNotificationEvent].
     */
    fun listenForMessages(onEvent: (PushNotificationEvent) -> Unit)
}
