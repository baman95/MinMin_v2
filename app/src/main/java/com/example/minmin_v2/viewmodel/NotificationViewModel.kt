package com.example.minmin_v2.viewmodel

import android.service.notification.StatusBarNotification
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minmin_v2.service.NotificationListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {
    private val notificationListener = NotificationListener()
    private val _notifications = MutableStateFlow<List<StatusBarNotification>>(emptyList())
    val notifications: StateFlow<List<StatusBarNotification>> = _notifications

    init {
        viewModelScope.launch {
            notificationListener.notifications.collect { notifications ->
                _notifications.value = notifications
            }
        }
    }
}
