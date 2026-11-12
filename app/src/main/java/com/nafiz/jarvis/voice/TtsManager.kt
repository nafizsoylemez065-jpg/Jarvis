package com.nafiz.jarvis.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsManager(context: Context) {

    private var ready = false
    private var tts: TextToSpeech? = TextToSpeech(context) { status ->
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            runCatching {
                tts?.language = Locale("tr")
                tts?.setSpeechRate(1.05f)
            }
        }
    }

    fun speak(text: String) {
        if (!ready) return
        runCatching {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis_utterance")
        }
    }

    fun shutdown() {
        runCatching {
            tts?.stop()
            tts?.shutdown()
        }
        tts = null
        ready = false
    }
}
