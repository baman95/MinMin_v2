package com.example.minmin_v2.utils

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

data class AppUsageInfo(val appName: String, val packageName: String, val usageTime: Long)

object AppUsageUtil {
    suspend fun getAppUsageStats(context: Context): List<AppUsageInfo> = withContext(Dispatchers.IO) {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val endTime = System.currentTimeMillis()
        val startTime = calendar.timeInMillis

        val usageStatsList: List<UsageStats> = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )

        val appUsageList = mutableListOf<AppUsageInfo>()
        val packageManager = context.packageManager

        for (usageStats in usageStatsList) {
            try {
                val appInfo: ApplicationInfo = packageManager.getApplicationInfo(usageStats.packageName, 0)
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                val usageTime = usageStats.totalTimeInForeground
                if (usageTime > 5 * 60 * 1000) { // Only include apps used for more than 5 minutes
                    appUsageList.add(AppUsageInfo(appName, usageStats.packageName, usageTime))
                }
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e("AppUsageUtil", "Error retrieving app info for package: ${usageStats.packageName}", e)
            }
        }

        appUsageList.sortedByDescending { it.usageTime }
    }
}
