package com.nafiz.jarvis

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nafiz.jarvis.ui.ChatScreen
import com.nafiz.jarvis.ui.CommandsScreen
import com.nafiz.jarvis.ui.SettingsScreen
import com.nafiz.jarvis.ui.UltronScreen
import com.nafiz.jarvis.ui.theme.JarvisDark
import com.nafiz.jarvis.ui.theme.JarvisSurface
import com.nafiz.jarvis.ui.theme.JarvisTheme
import com.nafiz.jarvis.voice.VoiceInput

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val crashPrefs = getSharedPreferences("jarvis_crash", MODE_PRIVATE)
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching {
                crashPrefs.edit()
                    .putString("last_crash", Log.getStackTraceString(error).take(1200))
                    .putLong("crash_time", System.currentTimeMillis())
                    .apply()
            }
            defaultHandler?.uncaughtException(thread, error)
        }

        val lastCrash = crashPrefs.getString("last_crash", null)
        if (lastCrash != null) {
            setContent {
                JarvisTheme {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .background(JarvisDark)
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp)
                    ) {
                        Text(
                            "🚨 Uygulama çöktü — rapor:",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFFF87171)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            lastCrash,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = {
                            crashPrefs.edit().remove("last_crash").apply()
                            recreate()
                        }) {
                            Text("Raporu temizle ve uygulamayı aç")
                        }
                    }
                }
            }
            return
        }

        setContent {
            JarvisTheme {
                JarvisApp()
            }
        }
    }
}

@Composable
fun JarvisApp(viewModel: ChatViewModel = viewModel()) {
    val context = LocalContext.current
    val activity = context as? Activity
    var selectedTab by remember { mutableIntStateOf(0) }
    var listening by remember { mutableStateOf(false) }
    var partialText by remember { mutableStateOf("") }

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
                viewModel.send(it)
            },
            onError = {
                listening = false
                partialText = ""
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        )
    }

    Scaffold(
        containerColor = JarvisDark,
        bottomBar = {
            NavigationBar(containerColor = JarvisSurface) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Chat, contentDescription = "Sohbet") },
                    label = { Text("Sohbet") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.Bolt, contentDescription = "Komutlar") },
                    label = { Text("Komutlar") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Ayarlar") },
                    label = { Text("Ayarlar") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Filled.Whatshot, contentDescription = "Ultron") },
                    label = { Text("Ultron") }
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> ChatScreen(
                    viewModel = viewModel,
                    listening = listening,
                    partialText = partialText,
                    onToggleMic = { toggleMic() }
                )
                1 -> CommandsScreen()
                2 -> SettingsScreen()
                3 -> UltronScreen()
            }
        }
    }
}
