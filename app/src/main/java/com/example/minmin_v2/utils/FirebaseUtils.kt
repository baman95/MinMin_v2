package com.example.minmin_v2.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object FirebaseUtils {
    private const val TAG = "FirebaseUtils"

    private val firestoreInstance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    suspend fun saveAppUsageToFirebase(
        context: Context,
        userEmail: String,
        appUsageMap: Map<String, List<AppUsageInfo>>,
        totalDailyLimit: Long,
        blockedApps: Map<String, Long>
    ) {
        try {
            val weekData = mutableMapOf<String, Any>()

            appUsageMap.forEach { (day, appUsageList) ->
                val dayData = mutableMapOf<String, Any>()

                val appData = appUsageList.associate {
                    it.appName to mapOf(
                        "usageTime" to it.usageTime / 60000, // convert to minutes
                        "isBlocked" to (blockedApps[it.packageName] != null),
                        "blockTimeLimit" to (blockedApps[it.packageName] ?: 0L) // Block time in minutes
                    )
                }

                dayData["appData"] = appData
                dayData["totalDailyLimit"] = totalDailyLimit
                dayData["totalDailyUsage"] = appUsageList.sumOf { it.usageTime } / 60000 // Total usage time in minutes
                dayData["dayOfWeek"] = day
                dayData["date"] = getDateStringForDay(day)

                weekData[day] = dayData
            }

            // Save week data using the user's email as the document ID
            firestoreInstance.collection("UserAppActivity")
                .document(userEmail)
                .collection("weeks")
                .document("week_${getCurrentWeekOfYear()}")
                .set(weekData)
                .await()

            Log.d(TAG, "App usage data saved successfully for user: $userEmail")
            Toast.makeText(context, "App usage data saved successfully", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e(TAG, "Error saving app usage data to Firebase: ${e.message}")
            Toast.makeText(context, "Failed to save app usage data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentWeekOfYear(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.WEEK_OF_YEAR)}_${calendar.get(Calendar.YEAR)}"
    }

    private fun getDateStringForDay(day: String): String {
        val dayMap = mapOf(
            "Sun" to Calendar.SUNDAY,
            "Mon" to Calendar.MONDAY,
            "Tue" to Calendar.TUESDAY,
            "Wed" to Calendar.WEDNESDAY,
            "Thu" to Calendar.THURSDAY,
            "Fri" to Calendar.FRIDAY,
            "Sat" to Calendar.SATURDAY
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayMap[day] ?: Calendar.SUNDAY)
        }

        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    suspend fun fetchAndSavePast7DaysData(
        context: Context,
        userEmail: String,
        blockedApps: Map<String, Long>,
        totalDailyLimit: Long
    ) {
        try {
            val calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val startTime = calendar.timeInMillis

            val appUsageMap = AppUsageUtil.getAppUsageStatsForWeek(context, calendar.get(Calendar.YEAR), calendar.get(Calendar.WEEK_OF_YEAR))

            saveAppUsageToFirebase(context, userEmail, appUsageMap, totalDailyLimit, blockedApps)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching and saving past 7 days of data: ${e.message}")
            Toast.makeText(context, "Error fetching data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
