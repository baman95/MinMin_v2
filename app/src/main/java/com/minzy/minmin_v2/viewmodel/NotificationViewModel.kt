package com.minzy.minmin_v2.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.minzy.minmin_v2.service.NotificationListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class NotificationData(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable?,
    val title: String,
    val text: String
)

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val _notifications = MutableStateFlow<Map<String, List<NotificationData>>>(emptyMap())
    val notifications: StateFlow<Map<String, List<NotificationData>>> = _notifications

    init {
        viewModelScope.launch {
            NotificationListener.notifications.collect { notificationList ->
                val groupedNotifications = notificationList.groupBy { it.packageName }.mapValues { entry ->
                    entry.value.map { notification ->
                        val context = getApplication<Application>().applicationContext
                        val pm = context.packageManager
                        val appName = getAppName(pm, notification.packageName)
                        val appIcon = getAppIcon(pm, notification.packageName)
                        val title = notification.notification.extras.get("android.title")?.toString() ?: "No Title"
                        val text = notification.notification.extras.get("android.text")?.toString() ?: "No Text"
                        NotificationData(notification.packageName, appName, appIcon, title, text)
                    }
                }
                _notifications.value = groupedNotifications
                logAllNotifications(notificationList)
            }
        }
    }

    private fun getAppName(pm: PackageManager, packageName: String): String {
        return try {
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName.split(".").last().capitalize()
        }
    }

    private fun getAppIcon(pm: PackageManager, packageName: String): Drawable? {
        return try {
            pm.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        } ?: run {
            // Default app icon in case of an error
            getApplication<Application>().getDrawable(android.R.drawable.sym_def_app_icon)
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
