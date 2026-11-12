package com.nafiz.jarvis.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.nafiz.jarvis.data.SettingsStore
import com.nafiz.jarvis.ui.theme.JarvisCyan
import com.nafiz.jarvis.ui.theme.JarvisSurface

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val settings = remember { SettingsStore(context) }

    var baseUrl by remember { mutableStateOf(settings.baseUrl) }
    var apiKey by remember { mutableStateOf(settings.apiKey) }
    var model by remember { mutableStateOf(settings.model) }
    var speakReplies by remember { mutableStateOf(settings.speakReplies) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("⚙️ Ayarlar", style = MaterialTheme.typography.titleLarge, color = JarvisCyan)

        Text(
            "Sohbet modu için herhangi bir OpenAI uyumlu yapay zeka servisini kullanabilirsin " +
                "(Mistral, OpenAI, OpenRouter, Groq...). API anahtarı sadece telefonunda saklanır.",
            style = MaterialTheme.typography.bodySmall
        )

        OutlinedTextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            label = { Text("API Adresi") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("API Anahtarı") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = model,
            onValueChange = { model = it },
            label = { Text("Model") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Yanıtları sesli oku", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = speakReplies, onCheckedChange = { speakReplies = it })
        }

        Button(
            onClick = {
                settings.baseUrl = baseUrl.trim()
                settings.apiKey = apiKey.trim()
                settings.model = model.trim()
                settings.speakReplies = speakReplies
                Toast.makeText(context, "Ayarlar kaydedildi", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kaydet")
        }

        Spacer(Modifier.height(8.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .background(JarvisSurface, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text("💡 İpucu", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                "API anahtarı olmadan da komutlar çalışır: uygulama açma, fener, ses, " +
                    "alarm, zamanlayıcı, batarya, not alma ve arama. Anahtar girersen Jarvis " +
                    "her konuda sohbet eder.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
