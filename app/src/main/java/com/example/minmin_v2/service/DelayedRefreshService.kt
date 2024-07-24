package com.example.minmin_v2.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.minmin_v2.R

class DelayedRefreshService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var delayEndTime: Long = 0L
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createOverlayView()
        startDelayLogic()
        Log.d("DelayedRefreshService", "Service started")
    }

    private fun startForegroundService() {
        val channelId = "DelayedRefreshServiceChannel"
        val channelName = "Delayed Refresh Service Channel"
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Delaying Refresh")
            .setContentText("Service is running...")
            .setSmallIcon(R.drawable.ic_delay)
            .build()

        startForeground(1, notification)
    }

    private fun createOverlayView() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_delay, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        windowManager.addView(overlayView, params)
        overlayView.visibility = View.GONE
    }

    private fun startDelayLogic() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (System.currentTimeMillis() < delayEndTime) {
                    showOverlay()
                } else {
                    hideOverlay()
                    stopSelf()
                }
                handler.postDelayed(this, 1000L)
            }
        }, 1000L)
    }

    private fun showOverlay() {
        if (overlayView.visibility != View.VISIBLE) {
            overlayView.visibility = View.VISIBLE
        }
    }

    private fun hideOverlay() {
        if (overlayView.visibility != View.GONE) {
            overlayView.visibility = View.GONE
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val delayDuration = intent?.getLongExtra("delayDuration", 0L) ?: 0L
        delayEndTime = System.currentTimeMillis() + delayDuration * 60 * 1000 // Convert minutes to milliseconds
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        windowManager.removeView(overlayView)
        Log.d("DelayedRefreshService", "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
