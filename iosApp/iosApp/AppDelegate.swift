import UIKit
import Firebase
import FirebaseMessaging
import shared

class AppDelegate: UIResponder, UIApplicationDelegate, MessagingDelegate, UNUserNotificationCenterDelegate {

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {

        // 1. Initialize Firebase
        FirebaseApp.configure()

        // 2. Set Delegates
        Messaging.messaging().delegate = self
        UNUserNotificationCenter.current().delegate = self

        // 3. Request permissions
        let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
        UNUserNotificationCenter.current().requestAuthorization(options: authOptions) { _, _ in }

        application.registerForRemoteNotifications()

        return true
    }

    // MARK: - MessagingDelegate

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        print("FCM Token: \(String(describing: fcmToken))")
        // Forward to KMP Shared Module
        NotificationService.companion.setToken(token: fcmToken)
    }

    // MARK: - UNUserNotificationCenterDelegate

    // Foreground message handling
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {

        let userInfo = notification.request.content.userInfo
        let title = notification.request.content.title
        let body = notification.request.content.body

        // Convert [AnyHashable: Any] to [String: String]
        var data: [String: String] = [:]
        for (key, value) in userInfo {
            if let keyStr = key as? String, let valStr = value as? String {
                data[keyStr] = valStr
            }
        }

        // Dispatch to KMP Shared Module
        NotificationService.companion.dispatchMessage(title: title, body: body, data: data)

        completionHandler([[.banner, .badge, .sound]])
    }
}
