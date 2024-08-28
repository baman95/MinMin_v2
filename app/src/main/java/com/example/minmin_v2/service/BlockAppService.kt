package com.example.minmin_v2.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.minmin_v2.MainActivity
import com.example.minmin_v2.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BlockAppService : Service() {
    private val blockedApps = mutableSetOf<String>()
    private val CHANNEL_ID = "BlockAppServiceChannel"
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundService()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val packageName = intent?.getStringExtra("packageName")
        val blockDuration = intent?.getLongExtra("blockDuration", 0L)
        val appName = intent?.getStringExtra("appName")
        if (packageName != null && blockDuration != null) {
            if (blockDuration > 0) {
                blockedApps.add(packageName)
                showOverlay(appName ?: "this app", blockDuration)
                saveBlockedAppDataToFirebase(appName, blockDuration)
            } else {
                blockedApps.remove(packageName)
                removeOverlay()
                removeBlockedAppDataFromFirebase(appName)
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
                "Block App Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun startForegroundService() {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("App Blocking Service")
            .setContentText("Blocking selected apps")
            .setSmallIcon(R.drawable.ic_block) // Add an appropriate icon
            .build()

        startForeground(1, notification)
    }

    private fun showOverlay(appName: String, blockDuration: Long) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_block, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )

        // Setup overlay views
        val overlayTitle = overlayView.findViewById<TextView>(R.id.overlay_title)
        val overlayMessage = overlayView.findViewById<TextView>(R.id.overlay_message)
        val overlayTimer = overlayView.findViewById<TextView>(R.id.overlay_timer_value)
        val removeButton: Button = overlayView.findViewById(R.id.remove_block_button)

        overlayTitle.text = "Time's Up!"
        overlayMessage.text = "You've reached your usage limit for $appName. Take a break and enjoy some offline activities. We'll be here when you get back!"
        overlayTimer.text = formatDuration(blockDuration)

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
        if (::overlayView.isInitialized && overlayView.isAttachedToWindow) {
            windowManager.removeView(overlayView)
        }
    }

    private fun saveBlockedAppDataToFirebase(appName: String?, blockDuration: Long) {
        val user = auth.currentUser
        user?.let {
            val userEmail = it.email
            val userDocRef = firestore.collection("UserAppActivity").document(userEmail!!)
            userDocRef.update(mapOf("blockedApps.$appName" to blockDuration))
                .addOnSuccessListener {
                    Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun removeBlockedAppDataFromFirebase(appName: String?) {
        val user = auth.currentUser
        user?.let {
            val userEmail = it.email
            val userDocRef = firestore.collection("UserAppActivity").document(userEmail!!)
            userDocRef.update(mapOf("blockedApps.$appName" to null))
                .addOnSuccessListener {
                    Toast.makeText(this, "Data removed successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to remove data", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun formatDuration(durationInMillis: Long): String {
        val minutes = (durationInMillis / 1000) / 60
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        return String.format("%02d:%02d", hours, remainingMinutes)
    }
}
