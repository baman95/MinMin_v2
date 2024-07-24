package com.example.minmin_v2.viewmodel

import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minmin_v2.service.NotificationListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {
    private val _notifications = MutableStateFlow<List<StatusBarNotification>>(emptyList())
    val notifications: StateFlow<List<StatusBarNotification>> = _notifications

    init {
        viewModelScope.launch {
            NotificationListener.notifications.collect { notificationList ->
                _notifications.value = notificationList
                logAllNotifications(notificationList)
            }
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
            Log.d("NotificationViewModel", "Notification: ${notification.packageName} - $titleText")
        }
    }
}
