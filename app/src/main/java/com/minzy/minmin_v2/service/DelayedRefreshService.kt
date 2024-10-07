package com.minzy.minmin_v2.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.minzy.minmin_v2.MainActivity
import com.minzy.minmin_v2.R

class DelayedRefreshService : Service() {
    private val blockedApps = mutableMapOf<String, Long>()
    private val CHANNEL_ID = "DelayedRefreshServiceChannel"
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundService()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val packageName = intent?.getStringExtra("packageName")
        val blockDuration = intent?.getLongExtra("blockDuration", 0L)
        if (packageName != null && blockDuration != null) {
            if (blockDuration > 0) {
                blockedApps[packageName] = blockDuration
                showOverlay(packageName)
            } else {
                blockedApps.remove(packageName)
                removeOverlay()
            }
        } else {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(this, "Error: Missing package name or duration", Toast.LENGTH_SHORT).show()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Delayed Refresh Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun startForegroundService() {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Delayed Refresh Service")
            .setContentText("Blocking selected apps")
            .setSmallIcon(R.drawable.ic_block) // Add an appropriate icon
            .build()

        startForeground(1, notification)
    }

    private fun showOverlay(packageName: String) {
        if (overlayView != null) {
            removeOverlay()
        }

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_block, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        val removeButton: Button = overlayView!!.findViewById(R.id.remove_block_button)
        removeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            removeOverlay()
        }

        windowManager.addView(overlayView, params)
    }

    private fun removeOverlay() {
        if (overlayView != null && overlayView!!.isAttachedToWindow) {
            windowManager.removeView(overlayView)
            overlayView = null
        }
    }
}
