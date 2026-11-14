package com.nafiz.jarvis

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nafiz.jarvis.commands.CommandEngine
import com.nafiz.jarvis.data.ChatHistoryStore
import com.nafiz.jarvis.data.ChatMessage
import com.nafiz.jarvis.data.Role
import com.nafiz.jarvis.data.SettingsStore
import com.nafiz.jarvis.llm.LlmClient
import com.nafiz.jarvis.voice.TtsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(
    application: Application,
    private val ultron: Boolean = false
) : AndroidViewModel(application) {

    private val settings = SettingsStore(application)
    private val llm = LlmClient(settings)
    private val history = ChatHistoryStore(
        application,
        if (ultron) "jarvis_ultron_history" else "jarvis_history"
    )
    private val tts = TtsManager(application)
    private val engine by lazy { CommandEngine(application) }

    val messages = mutableStateListOf<ChatMessage>()
    var isThinking by mutableStateOf(false)
        private set

    init {
        messages.addAll(history.load())
        if (messages.isEmpty()) {
            messages.add(
                ChatMessage(
                    role = Role.ASSISTANT,
                    text = if (ultron) {
                        "😈 Ultron aktif.\n\n" +
                            "Burada sana şekerleme yapmam: fikirlerini sorgularım, zayıf noktalarını " +
                            "gösteririm. Ama bir komut verirsen paşa gibi yaparım.\n\n" +
                            "Hadi, bana en büyük fikrini anlat."
                    } else {
                        "Merhaba, ben Jarvis. 🤖\n\n" +
                            "Bana sesli ya da yazılı komut verebilirsin: \"feneri aç\", \"YouTube'u aç\", " +
                            "\"alarm kur 07:30\", \"batarya yüzde kaç\"...\n\n" +
                            "Serbest sohbet için Ayarlar sekmesinden API anahtarı girebilirsin."
                    }
                )
            )
            history.save(messages)
        }
    }

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || isThinking) return

        append(ChatMessage(role = Role.USER, text = trimmed))

        val command = runCatching { engine.tryExecute(trimmed) }.getOrNull()
        if (command != null) {
            append(ChatMessage(role = Role.ASSISTANT, text = command.speech, isCommand = true))
            if (command.ok) tts.speak(command.speech)
            return
        }

        isThinking = true
        viewModelScope.launch {
            val reply = withContext(Dispatchers.IO) {
                llm.chat(messages.toList(), forceUltron = ultron)
            }
                ?: ("Sohbet modu yanıt veremedi. Sebep: " + (llm.lastError ?: "bilinmiyor") +
                    "\n\nKomutlarım ise çalışmaya devam ediyor — Komutlar sekmesine bakabilirsin.")
            append(ChatMessage(role = Role.ASSISTANT, text = reply))
            if (settings.speakReplies) tts.speak(reply)
            isThinking = false
        }
    }

    private fun append(message: ChatMessage) {
        messages.add(message)
        history.save(messages)
    }

    override fun onCleared() {
        tts.shutdown()
    }
}
