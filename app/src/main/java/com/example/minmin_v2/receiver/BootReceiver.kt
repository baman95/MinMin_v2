package com.example.minmin_v2.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.minmin_v2.service.BlockAppService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent?.action) {
            val serviceIntent = Intent(context, BlockAppService::class.java)
            context?.startForegroundService(serviceIntent)
            Log.d("BootReceiver", "Started BlockAppService on boot")
        }
    }
}
