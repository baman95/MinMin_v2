package com.example.minmin_v2.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.minmin_v2.R
import java.util.*

class BlockAppService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 1000L // 1 second
    private var blockedAppPackage: String? = null
    private var blockEndTime: Long = 0L
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createOverlayView()
        startBlockingLogic()
        Log.d("BlockAppService", "Service started")
    }

    private fun startForegroundService() {
        val channelId = "BlockAppServiceChannel"
        val channelName = "Block App Service Channel"
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Blocking App Usage")
            .setContentText("Service is running...")
            .setSmallIcon(R.drawable.ic_block)
            .build()

        startForeground(1, notification)
    }

    private fun createOverlayView() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_block, null)

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

    private fun startBlockingLogic() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (System.currentTimeMillis() < blockEndTime) {
                    if (blockedAppPackage != null && isAppRunning(blockedAppPackage!!)) {
                        showOverlay()
                    } else {
                        hideOverlay()
                    }
                } else {
                    hideOverlay()
                    stopSelf()
                }
                handler.postDelayed(this, checkInterval)
            }
        }, checkInterval)
    }

    private fun isAppRunning(packageName: String): Boolean {
        val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, -5)
        val endTime = System.currentTimeMillis()
        val startTime = calendar.timeInMillis

        val usageStatsList: List<UsageStats> = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )

        for (usageStats in usageStatsList) {
            if (usageStats.packageName == packageName && usageStats.totalTimeInForeground > 0) {
                return true
            }
        }
        return false
    }

    private fun showOverlay() {
        overlayView.visibility = View.VISIBLE
    }

    private fun hideOverlay() {
        overlayView.visibility = View.GONE
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        blockedAppPackage = intent?.getStringExtra("packageName")
        val blockDuration = intent?.getLongExtra("blockDuration", 0L) ?: 0L
        blockEndTime = System.currentTimeMillis() + blockDuration * 60 * 1000 // Convert minutes to milliseconds
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        windowManager.removeView(overlayView)
        Log.d("BlockAppService", "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
