package com.example.minmin_v2.utils

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import java.util.Calendar
import java.util.Locale

object AppUsageUtil {
    private const val TAG = "AppUsageUtil"

    fun getAppUsageStats(context: Context, startTime: Long, endTime: Long): List<AppUsageInfo> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        val packageManager = context.packageManager
        val appUsageInfoList = mutableListOf<AppUsageInfo>()

        for (usageStat in usageStats) {
            try {
                if (usageStat.totalTimeInForeground >= 2 * 60 * 1000) { // Filter apps with less than 2 minutes
                    val appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(usageStat.packageName, 0)).toString()
                    appUsageInfoList.add(AppUsageInfo(
                        packageName = usageStat.packageName,
                        appName = appName,
                        usageTime = usageStat.totalTimeInForeground
                    ))
                }
            } catch (e: PackageManager.NameNotFoundException) {
                Log.w(TAG, "Package not found: ${usageStat.packageName}")
            }
        }

        return appUsageInfoList
    }

    fun getAppUsageStatsForWeek(context: Context, year: Int, week: Int): Map<String, List<AppUsageInfo>> {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.WEEK_OF_YEAR, week)

        val startTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val endTime = calendar.timeInMillis

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val usageStatsMap = mutableMapOf<String, List<AppUsageInfo>>()

        for (i in 0 until 7) {
            val dayStartTime = startTime + i * 24 * 60 * 60 * 1000
            val dayEndTime = dayStartTime + 24 * 60 * 60 * 1000
            val usageStats = getAppUsageStats(context, dayStartTime, dayEndTime)
            val dayLabel = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) ?: "Day $i"
            usageStatsMap[dayLabel] = usageStats
        }

        return usageStatsMap
    }
}

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val usageTime: Long
)
