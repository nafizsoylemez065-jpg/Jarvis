package com.nafiz.jarvis.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsStore(context: Context) {

    private val prefs = context.getSharedPreferences("jarvis_settings", Context.MODE_PRIVATE)

    var baseUrl: String
        get() = prefs.getString(KEY_BASE_URL, "https://api.mistral.ai/v1") ?: "https://api.mistral.ai/v1"
        set(value) = prefs.edit().putString(KEY_BASE_URL, value).apply()

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_KEY, value).apply()

    var model: String
        get() = prefs.getString(KEY_MODEL, "mistral-small-latest") ?: "mistral-small-latest"
        set(value) = prefs.edit().putString(KEY_MODEL, value).apply()

    var speakReplies: Boolean
        get() = prefs.getBoolean(KEY_SPEAK, true)
        set(value) = prefs.edit().putBoolean(KEY_SPEAK, value).apply()

    var ultronMode: Boolean
        get() = prefs.getBoolean(KEY_ULTRON, false)
        set(value) = prefs.edit().putBoolean(KEY_ULTRON, value).apply()

    private companion object {
        const val KEY_BASE_URL = "base_url"
        const val KEY_API_KEY = "api_key"
        const val KEY_MODEL = "model"
        const val KEY_SPEAK = "speak_replies"
        const val KEY_ULTRON = "ultron_mode"
    }
}

class ChatHistoryStore(context: Context) {

    private val prefs = context.getSharedPreferences("jarvis_history", Context.MODE_PRIVATE)

    fun load(): List<ChatMessage> = runCatching {
        val array = JSONArray(prefs.getString(KEY_MESSAGES, "[]") ?: "[]")
        (0 until array.length()).mapNotNull { i ->
            val o = array.getJSONObject(i)
            ChatMessage(
                role = if (o.getString("role") == "USER") Role.USER else Role.ASSISTANT,
                text = o.getString("text"),
                isCommand = o.optBoolean("isCommand"),
                time = o.optLong("time")
            )
        }
    }.getOrDefault(emptyList())

    fun save(messages: List<ChatMessage>) {
        val array = JSONArray()
        messages.takeLast(200).forEach { m ->
            array.put(
                JSONObject()
                    .put("role", m.role.name)
                    .put("text", m.text)
                    .put("isCommand", m.isCommand)
                    .put("time", m.time)
            )
        }
        prefs.edit().putString(KEY_MESSAGES, array.toString()).apply()
    }

    private companion object {
        const val KEY_MESSAGES = "messages"
    }
}

class NotesStore(context: Context) {

    private val prefs = context.getSharedPreferences("jarvis_notes", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("dd MMM HH:mm", Locale("tr"))

    fun add(note: String) {
        val array = JSONArray(prefs.getString(KEY_NOTES, "[]") ?: "[]")
        array.put(dateFormat.format(Date()) + " — " + note.trim())
        while (array.length() > 50) array.remove(0)
        prefs.edit().putString(KEY_NOTES, array.toString()).apply()
    }

    fun all(): List<String> = runCatching {
        val array = JSONArray(prefs.getString(KEY_NOTES, "[]") ?: "[]")
        (0 until array.length()).map { array.getString(it) }
    }.getOrDefault(emptyList())

    private companion object {
        const val KEY_NOTES = "notes"
    }
}
