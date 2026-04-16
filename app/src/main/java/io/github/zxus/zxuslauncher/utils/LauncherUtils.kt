package io.github.zxus.zxuslauncher.utils

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent

object LauncherUtils {
    fun openWebSearch(context: Context) {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, "")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    @SuppressLint("WrongConstant")
    fun expandNotifications(context: Context) {
        try {
            val statusBarService = context.getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val method = statusBarManager.getMethod("expandNotificationsPanel")
            method.invoke(statusBarService)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
