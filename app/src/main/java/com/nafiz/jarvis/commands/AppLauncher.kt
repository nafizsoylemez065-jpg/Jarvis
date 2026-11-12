package com.nafiz.jarvis.commands

import android.content.Context
import android.content.Intent
import android.net.Uri

data class AppInfo(val label: String, val packageName: String)

class AppLauncher(private val context: Context) {

    fun installedApps(): List<AppInfo> = runCatching {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        context.packageManager.queryIntentActivities(intent, 0)
            .map { AppInfo(it.loadLabel(context.packageManager).toString(), it.activityInfo.packageName) }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }.getOrDefault(emptyList())

    fun find(query: String): AppInfo? {
        val q = normalize(query)
        if (q.isEmpty()) return null
        val apps = installedApps()
        return apps.firstOrNull { normalize(it.label) == q }
            ?: apps.firstOrNull { normalize(it.label).startsWith(q) }
            ?: apps.firstOrNull { normalize(it.label).contains(q) }
            ?: apps.firstOrNull { q.contains(normalize(it.label)) && normalize(it.label).length > 2 }
    }

    fun launch(app: AppInfo): Boolean = runCatching {
        val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
            ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
    }.getOrDefault(false)

    companion object {
        fun normalize(text: String): String = text
            .lowercase(java.util.Locale("tr"))
            .replace(Regex("[^\\p{L}\\p{Nd}\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}

object Intents {

    private fun newTask(intent: Intent) = intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun dial(context: Context, number: String): Boolean = runCatching {
        context.startActivity(newTask(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))))
        true
    }.getOrDefault(false)

    fun webSearch(context: Context, query: String): Boolean = runCatching {
        context.startActivity(
            newTask(Intent(Intent.ACTION_WEB_SEARCH).putExtra("query", query))
        )
        true
    }.getOrDefault(false)

    fun map(context: Context, query: String): Boolean = runCatching {
        context.startActivity(
            newTask(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(query))))
        )
        true
    }.getOrDefault(false)

    fun openSettings(context: Context, action: String): Boolean = runCatching {
        context.startActivity(newTask(Intent(action)))
        true
    }.getOrDefault(false)

    fun setAlarm(context: Context, hour: Int, minute: Int): Boolean = runCatching {
        context.startActivity(
            newTask(
                Intent(android.provider.AlarmClock.ACTION_SET_ALARM).apply {
                    putExtra(android.provider.AlarmClock.EXTRA_HOUR, hour)
                    putExtra(android.provider.AlarmClock.EXTRA_MINUTES, minute)
                    putExtra(android.provider.AlarmClock.EXTRA_SKIP_UI, true)
                }
            )
        )
        true
    }.getOrDefault(false)

    fun setTimer(context: Context, seconds: Int, label: String?): Boolean = runCatching {
        context.startActivity(
            newTask(
                Intent(android.provider.AlarmClock.ACTION_SET_TIMER).apply {
                    putExtra(android.provider.AlarmClock.EXTRA_LENGTH, seconds)
                    putExtra(android.provider.AlarmClock.EXTRA_SKIP_UI, true)
                    if (!label.isNullOrBlank()) {
                        putExtra(android.provider.AlarmClock.EXTRA_DESCRIPTION, label)
                    }
                }
            )
        )
        true
    }.getOrDefault(false)

    fun isDialable(text: String): Boolean {
        val digits = text.filter { it.isDigit() }
        return digits.length >= 3
    }
}
