package com.example.cryptoapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import notification.NotificationService
import notification.PushNotificationEvent

/**
 * Firebase Cloud Messaging service for the Crypto App.
 *
 * Responsibilities:
 *  1. [onNewToken]        — Called when FCM issues a new registration token.
 *                           Send this to your backend to target this device.
 *  2. [onMessageReceived] — Called for EVERY message when the app is in the
 *                           foreground, and for data-only messages in any state.
 *                           Notification messages when app is in background /
 *                           killed are shown automatically by the system.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG            = "FCMService"
        private const val CHANNEL_ID     = "crypto_push_channel"
        private const val CHANNEL_NAME   = "Crypto Notifications"
        private const val NOTIF_ID       = 1001
    }

    // ── Token lifecycle ────────────────────────────────────────────────────────

    /**
     * Invoked when FCM generates a new or refreshed token.
     * Log it during development; in production, POST it to your backend.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        // TODO: send token to your backend server
    }

    // ── Message handling ───────────────────────────────────────────────────────

    /**
     * Invoked when:
     *  - App is in the FOREGROUND (notification + data messages).
     *  - App is in the BACKGROUND / KILLED (data-only messages only —
     *    notification messages are handled by the system tray automatically).
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM message received from: ${message.from}")

        val title = message.notification?.title ?: message.data["title"] ?: "Crypto App"
        val body  = message.notification?.body  ?: message.data["body"]  ?: ""
        val data  = message.data.toMap()

        val event = PushNotificationEvent(title = title, body = body, data = data)

        // 1️⃣  Notify any in-app ViewModel listeners (foreground banner)
        NotificationService.dispatchMessage(event)

        // 2️⃣  Show a system notification so it's also visible in the status bar
        showSystemNotification(title, body)
    }

    // ── System notification helper ─────────────────────────────────────────────

    private fun showSystemNotification(title: String, body: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel (required on API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Push notifications from Crypto App"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tapping the notification opens MainActivity
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIF_ID, notification)
    }
}
