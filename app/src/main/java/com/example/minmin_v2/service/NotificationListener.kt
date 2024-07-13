package com.example.minmin_v2.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotificationListener : NotificationListenerService() {

    private val _notifications = MutableStateFlow<List<StatusBarNotification>>(emptyList())
    val notifications: StateFlow<List<StatusBarNotification>> = _notifications

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let {
            Log.d("NotificationListener", "Notification Posted: ${it.packageName}")
            _notifications.value = _notifications.value + it
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn?.let {
            Log.d("NotificationListener", "Notification Removed: ${it.packageName}")
            _notifications.value = _notifications.value.filter { it != sbn }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("NotificationListener", "Listener Connected")
        _notifications.value = activeNotifications.toList()
    }
}
