// File: BlockAppService.kt
package com.minzy.minmin_v2.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.minzy.minmin_v2.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class BlockAppService : Service() {

    private val blockedApps = mutableSetOf<String>()
    private val CHANNEL_ID = "BlockAppServiceChannel"
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var blockDuration: Long = 0L
    private var blockEndTime: Long = 0L
    private var handler: Handler? = null
    private var runnable: Runnable? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundService()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        handler = Handler(Looper.getMainLooper())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val packageName = intent?.getStringExtra("packageName")
        blockDuration = intent?.getLongExtra("blockDuration", 0L) ?: 0L
        val appName = intent?.getStringExtra("appName")
        if (packageName != null && blockDuration > 0L) {
            blockedApps.add(packageName)
            blockEndTime = System.currentTimeMillis() + blockDuration
            showOverlay(appName ?: "this app")
            saveBlockedAppDataToFirebase(appName, blockDuration)
            startBlockTimer()
        } else if (packageName != null) {
            blockedApps.remove(packageName)
            removeOverlay()
            removeBlockedAppDataFromFirebase(appName)
            stopSelf()
        } else {
            // Handle the case where packageName is null
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
        runnable?.let { handler?.removeCallbacks(it) }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Block App Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun startForegroundService() {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("App Blocking Service")
            .setContentText("Blocking selected apps")
            .setSmallIcon(R.drawable.ic_block) // Ensure you have an appropriate icon
            .build()

        startForeground(1, notification)
    }

    private fun showOverlay(appName: String) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_block, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        // Setup overlay views
        val overlayTitle = overlayView?.findViewById<TextView>(R.id.overlay_title)
        val overlayMessage = overlayView?.findViewById<TextView>(R.id.overlay_message)
        val overlayTimer = overlayView?.findViewById<TextView>(R.id.overlay_timer_value)
        val removeButton: Button? = overlayView?.findViewById(R.id.remove_block_button)

        overlayTitle?.text = "Time's Up!"
        overlayMessage?.text = "You've reached your usage limit for $appName. Take a break and enjoy some offline activities. We'll be here when you get back!"

        // Update the timer every second
        runnable = object : Runnable {
            override fun run() {
                val remainingTime = blockEndTime - System.currentTimeMillis()
                if (remainingTime > 0) {
                    overlayTimer?.text = formatDuration(remainingTime)
                    handler?.postDelayed(this, 1000L)
                } else {
                    removeOverlay()
                    stopSelf()
                }
            }
        }
        handler?.post(runnable!!)

        removeButton?.setOnClickListener {
            removeOverlay()
            stopSelf()
        }

        try {
            windowManager.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeOverlay() {
        try {
            overlayView?.let {
                if (it.isAttachedToWindow) {
                    windowManager.removeView(it)
                    overlayView = null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveBlockedAppDataToFirebase(appName: String?, blockDuration: Long) {
        val user = auth.currentUser
        user?.let {
            val userEmail = it.email ?: return
            val userDocRef = firestore.collection("UserAppActivity").document(userEmail)
            val blockedAppData = mapOf("blockedApps.$appName" to blockDuration)
            userDocRef.update(blockedAppData)
                .addOnSuccessListener {
                    // Data saved successfully
                }
                .addOnFailureListener {
                    // Handle failure
                }
        }
    }

    private fun removeBlockedAppDataFromFirebase(appName: String?) {
        val user = auth.currentUser
        user?.let {
            val userEmail = it.email ?: return
            val userDocRef = firestore.collection("UserAppActivity").document(userEmail)
            val updates = mapOf<String, Any>("blockedApps.$appName" to FieldValue.delete())
            userDocRef.update(updates)
                .addOnSuccessListener {
                    // Data removed successfully
                }
                .addOnFailureListener {
                    // Handle failure
                }
        }
    }

    private fun startBlockTimer() {
        // Timer logic is handled in the showOverlay runnable
    }

    private fun formatDuration(durationInMillis: Long): String {
        val totalSeconds = durationInMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
