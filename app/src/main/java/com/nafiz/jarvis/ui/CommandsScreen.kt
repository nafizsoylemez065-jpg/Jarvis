package com.nafiz.jarvis.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nafiz.jarvis.ui.theme.JarvisCyan
import com.nafiz.jarvis.ui.theme.JarvisSurface

private data class CommandExample(val title: String, val examples: List<String>)

private val commandGroups = listOf(
    CommandExample(
        "📱 Uygulama Açma",
        listOf(
            "\"YouTube'u aç\"",
            "\"Spotify'ı başlat\"",
            "\"WhatsApp'ı çalıştır\"",
            "\"Instagram'ı açar mısın\""
        )
    ),
    CommandExample(
        "🔦 Cihaz Kontrolleri",
        listOf(
            "\"Feneri aç\" / \"Feneri kapat\"",
            "\"Sesi yüzde 50 yap\"",
            "\"Sesi yükselt\" / \"Sesi kıs\"",
            "\"Parlaklığı 80 yap\""
        )
    ),
    CommandExample(
        "⏰ Zaman",
        listOf(
            "\"Alarm kur 07:30\"",
            "\"5 dakika zamanlayıcı kur\"",
            "\"Saat kaç?\"",
            "\"Bugün ayın kaçı?\""
        )
    ),
    CommandExample(
        "🔎 Arama & Gezinme",
        listOf(
            "\"Google'da yapay zeka ara\"",
            "\"Haritada İstanbul göster\"",
            "\"Evime yol tarifi ver\""
        )
    ),
    CommandExample(
        "🔋 Bilgi & Araçlar",
        listOf(
            "\"Batarya yüzde kaç?\"",
            "\"Wi-Fi ayarlarını aç\"",
            "\"Bluetooth ayarlarını aç\"",
            "\"Not al yarın ekmek al\" / \"Notlarımı oku\""
        )
    )
)

@Composable
fun CommandsScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = JarvisCyan,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    "Yapabildiklerim",
                    style = MaterialTheme.typography.titleLarge,
                    color = JarvisCyan
                )
            }
        }
        item {
            Text(
                "Bunları yazılı olarak yazabilir ya da mikrofona basıp sesli söyleyebilirsin.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF8A93B2)
            )
        }
        commandGroups.forEach { group ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(group.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        group.examples.forEach { example ->
                            Text(
                                example,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFB9C2DE),
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
