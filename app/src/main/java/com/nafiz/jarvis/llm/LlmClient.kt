package com.nafiz.jarvis.llm

import com.nafiz.jarvis.data.ChatMessage
import com.nafiz.jarvis.data.Role
import com.nafiz.jarvis.data.SettingsStore
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class LlmClient(private val settings: SettingsStore) {

    var lastError: String? = null
        private set

    fun chat(history: List<ChatMessage>): String? {
        lastError = null
        val key = settings.apiKey.trim().replace(Regex("\\s"), "")
        if (key.isEmpty()) {
            lastError = "API anahtarı boş — Ayarlar sekmesinden gir"
            return null
        }
        repeat(3) { attempt ->
            if (attempt > 0) {
                runCatching { Thread.sleep(2000L * attempt) }
                lastError = null
            }
            val reply = execute(history, key)
            if (reply != null) return reply
            val retryable = lastError?.contains("(kod 503)") == true ||
                lastError?.contains("(kod 429)") == true
            if (!retryable) return null
        }
        return null
    }

    private fun execute(history: List<ChatMessage>, key: String): String? = runCatching {
        val urlStr = settings.baseUrl.trim().replace(Regex("\\s"), "")
        val connection = URL(urlStr + "/chat/completions")
            .openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 15_000
        connection.readTimeout = 60_000
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $key")

        val payload = JSONObject().apply {
            put("model", settings.model.trim().replace(Regex("\\s"), ""))
            put("temperature", 0.7)
            put("max_tokens", 500)
            put("messages", JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", SYSTEM_PROMPT))
                history.takeLast(12).forEach { m ->
                    put(
                        JSONObject()
                            .put("role", if (m.role == Role.USER) "user" else "assistant")
                            .put("content", m.text)
                    )
                }
            })
        }

        connection.outputStream.use {
            it.write(payload.toString().toByteArray(Charsets.UTF_8))
        }
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val body = stream?.bufferedReader()?.readText().orEmpty()
        connection.disconnect()
        if (code !in 200..299) {
            lastError = "API hatası (kod $code): " + body.take(300)
            return@runCatching null
        }

        JSONObject(body)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()
            .takeIf { it.isNotEmpty() }
    }.onFailure {
        lastError = "Bağlantı/okuma hatası: ${it.message}"
    }.getOrNull()

    private companion object {
        const val SYSTEM_PROMPT =
            "Sen Jarvis adında bir Türkçe yapay zeka asistanısın. " +
                "Kullanıcının kişisel asistanı gibi davran, Türkçe ve samimi ama kısa ve net yanıt ver. " +
                "Gereksiz açıklamalar yapma, gerektiğinde madde madde konuş."
    }
}
