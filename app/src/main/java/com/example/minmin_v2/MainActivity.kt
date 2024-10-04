package com.example.minmin_v2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.minmin_v2.ui.Navigation
import com.example.minmin_v2.ui.theme.MinMin_v2Theme
import com.google.firebase.auth.FirebaseAuth



class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Overlay permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val usageStatsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (!hasUsageStatsPermission()) {
            Toast.makeText(this, "Usage stats permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")

        sharedPreferences = getSharedPreferences("minmin_prefs", Context.MODE_PRIVATE)
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        // Save the authentication state
        if (user != null) {
            sharedPreferences.edit().putBoolean("is_logged_in", true).apply()
        } else {
            sharedPreferences.edit().putBoolean("is_logged_in", false).apply()
        }

        // Request permissions
        requestPermissions()

        setContent {
            MinMin_v2Theme {
                val navController = rememberNavController()
                Surface(color = MaterialTheme.colorScheme.background) {
                    Navigation(navController = navController, sharedPreferences = sharedPreferences)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestPermissions() {
        // Request Overlay permission
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        }

        // Check and request Usage Stats permission
        if (!hasUsageStatsPermission()) {
            val usageStatsPermission = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            usageStatsPermissionLauncher.launch(usageStatsPermission)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun hasUsageStatsPermission(): Boolean {
        val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOpsManager.unsafeCheckOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }
}
