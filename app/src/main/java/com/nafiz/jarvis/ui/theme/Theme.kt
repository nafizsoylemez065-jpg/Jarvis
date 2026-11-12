package com.nafiz.jarvis.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val JarvisDark = Color(0xFF0B0E1A)
val JarvisSurface = Color(0xFF151A2E)
val JarvisSurfaceHigh = Color(0xFF1E2540)
val JarvisCyan = Color(0xFF22D3EE)
val JarvisBlue = Color(0xFF3B82F6)
val JarvisText = Color(0xFFE5E9F5)
val JarvisTextDim = Color(0xFF8A93B2)

private val DarkColors = darkColorScheme(
    primary = JarvisCyan,
    secondary = JarvisBlue,
    background = JarvisDark,
    surface = JarvisSurface,
    surfaceVariant = JarvisSurfaceHigh,
    onPrimary = Color.Black,
    onBackground = JarvisText,
    onSurface = JarvisText,
    onSurfaceVariant = JarvisTextDim
)

@Composable
fun JarvisTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
