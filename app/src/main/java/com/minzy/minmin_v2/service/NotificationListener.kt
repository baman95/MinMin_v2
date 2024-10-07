package com.minzy.minmin_v2.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class NotificationListener : NotificationListenerService() {

    companion object {
        private val _notifications = MutableSharedFlow<List<StatusBarNotification>>(replay = 1)
        val notifications: SharedFlow<List<StatusBarNotification>> = _notifications
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("NotificationListener", "NotificationListener connected")
        val currentNotifications = activeNotifications.toList()
        _notifications.tryEmit(currentNotifications)
        logAllNotifications(currentNotifications)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let {
            Log.d("NotificationListener", "Notification posted: ${it.packageName}")
            val currentNotifications = activeNotifications.toList()
            _notifications.tryEmit(currentNotifications)
            logAllNotifications(currentNotifications)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn?.let {
            Log.d("NotificationListener", "Notification removed: ${it.packageName}")
            val currentNotifications = activeNotifications.toList()
            _notifications.tryEmit(currentNotifications)
            logAllNotifications(currentNotifications)
        }
    }

    private fun logAllNotifications(notifications: List<StatusBarNotification>) {
        notifications.forEach { notification ->
            val title = notification.notification.extras.get("android.title")
            val titleText = when (title) {
                is String -> title
                is android.text.SpannableString -> title.toString()
                else -> "Unknown Title"
            }
            Log.d("NotificationListener", "Notification: ${notification.packageName} - $titleText")
        }
    }
}
