package com.minzy.minmin_v2.utils

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

object AppUtils {
    fun getAppNameFromPackageName(context: Context, packageName: String): String {
        return try {
            val applicationInfo = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    fun getAppIconFromPackageName(context: Context, packageName: String): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon)
        }
    }
}
