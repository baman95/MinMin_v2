package com.minzy.minmin_v2.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.minzy.minmin_v2.service.BlockAppService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val serviceIntent = Intent(context, BlockAppService::class.java)
        context?.startForegroundService(serviceIntent)
        Log.d("AlarmReceiver", "Alarm triggered, started BlockAppService")
    }
}
