package com.minzy.minmin_v2.data

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.concurrent.TimeUnit

class AppUsageRepository(private val context: Context) {

    fun getAppUsageStats(): List<UsageStats> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(1)

        return usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ).filter { it.totalTimeInForeground > 0 }
    }
}
