package com.nafiz.jarvis.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.nafiz.jarvis.commands.CommandEngine
import com.nafiz.jarvis.data.ChatHistoryStore
import com.nafiz.jarvis.data.ChatMessage
import com.nafiz.jarvis.data.Role
import com.nafiz.jarvis.data.SettingsStore
import com.nafiz.jarvis.llm.LlmClient
import com.nafiz.jarvis.ui.theme.JarvisDark
import com.nafiz.jarvis.ui.theme.JarvisTextDim
import com.nafiz.jarvis.voice.TtsManager
import com.nafiz.jarvis.voice.VoiceInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val UltronRed = Color(0xFFEF4444)
val UltronOrange = Color(0xFFF97316)

@Composable
fun UltronScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    val app = remember { context.applicationContext }

    val settings = remember { SettingsStore(app) }
    val llm = remember { LlmClient(settings) }
    val history = remember { ChatHistoryStore(app, "jarvis_ultron_history") }
    val tts = remember { TtsManager(app) }
    val engine = remember { CommandEngine(app) }
    val scope = rememberCoroutineScope()

    val messages = remember { mutableStateListOf<ChatMessage>() }
    var isThinking by remember { mutableStateOf(false) }
    var listening by remember { mutableStateOf(false) }
    var partialText by remember { mutableStateOf("") }
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    DisposableEffect(Unit) {
        if (messages.isEmpty()) {
            messages.addAll(history.load())
        }
        if (messages.isEmpty()) {
            messages.add(
                ChatMessage(
                    role = Role.ASSISTANT,
                    text = "😈 Ultron aktif.\n\n" +
                        "Burada sana şekerleme yapmam: fikirlerini sorgularım, zayıf noktalarını " +
                        "gösteririm. Ama bir komut verirsen paşa gibi yaparım.\n\n" +
                        "Hadi, bana en büyük fikrini anlat."
                )
            )
            history.save(messages)
        }
        onDispose { tts.shutdown() }
    }

    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, "Sesli komut için mikrofon izni gerekiyor", Toast.LENGTH_LONG).show()
        }
    }

    val voiceInput = remember(activity) { activity?.let { VoiceInput(it) } }

    DisposableEffect(Unit) {
        onDispose { voiceInput?.destroy() }
    }

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || isThinking) return

        messages.add(ChatMessage(role = Role.USER, text = trimmed))
        history.save(messages)

        val command = runCatching { engine.tryExecute(trimmed) }.getOrNull()
        if (command != null) {
            messages.add(
                ChatMessage(role = Role.ASSISTANT, text = command.speech, isCommand = true)
            )
            history.save(messages)
            if (command.ok) tts.speak(command.speech, deepVoice = true)
            return
        }

        isThinking = true
        scope.launch {
            val reply = withContext(Dispatchers.IO) {
                llm.chat(messages.toList(), forceUltron = true)
            } ?: ("Sohbet modu yanıt veremedi. Sebep: " + (llm.lastError ?: "bilinmiyor"))
            messages.add(ChatMessage(role = Role.ASSISTANT, text = reply))
            history.save(messages)
            if (settings.speakReplies) tts.speak(reply, deepVoice = true)
            isThinking = false
        }
    }

    fun toggleMic() {
        val voice = voiceInput ?: return
        if (listening) {
            voice.stop()
            listening = false
            return
        }
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        listening = true
        partialText = ""
        voice.start(
            onPartial = { partialText = it },
            onFinal = {
                listening = false
                partialText = ""
                send(it)
            },
            onError = {
                listening = false
                partialText = ""
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisDark)
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            JarvisOrb(pulsing = listening || isThinking)
            Spacer(Modifier.size(12.dp))
            Column {
                Text("ULTRON 😈", style = MaterialTheme.typography.titleMedium, color = UltronRed)
                Text(
                    text = when {
                        listening -> "Dinliyorum..."
                        isThinking -> "Fikrini sorguluyorum..."
                        else -> "Meydan okumaya hazır"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = JarvisTextDim
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message)
            }
            if (isThinking) {
                item {
                    Text(
                        "Ultron düşünüyor…",
                        style = MaterialTheme.typography.bodySmall,
                        color = JarvisTextDim,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            if (listening && partialText.isNotBlank()) {
                item {
                    Text(
                        partialText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = UltronOrange,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Fikrini anlat, sorgulayayım", color = JarvisTextDim) },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )
            Spacer(Modifier.size(4.dp))
            IconButton(
                onClick = { toggleMic() },
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        if (listening) Color(0xFFEF4444) else UltronOrange,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (listening) Icons.Filled.Stop else Icons.Filled.Mic,
                    contentDescription = if (listening) "Durdur" else "Konuş",
                    tint = Color.White
                )
            }
            Spacer(Modifier.size(4.dp))
            IconButton(
                onClick = {
                    if (input.isNotBlank()) {
                        send(input)
                        input = ""
                    }
                },
                modifier = Modifier
                    .size(52.dp)
                    .background(UltronRed, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Gönder",
                    tint = Color.White
                )
            }
        }
    }
}
