package com.nafiz.jarvis.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nafiz.jarvis.ChatViewModel
import com.nafiz.jarvis.data.ChatMessage
import com.nafiz.jarvis.data.Role
import com.nafiz.jarvis.ui.theme.JarvisBlue
import com.nafiz.jarvis.ui.theme.JarvisCyan
import com.nafiz.jarvis.ui.theme.JarvisSurfaceHigh
import com.nafiz.jarvis.ui.theme.JarvisTextDim

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    listening: Boolean,
    partialText: String,
    onToggleMic: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(viewModel.messages.size, viewModel.isThinking) {
        if (viewModel.messages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            JarvisOrb(pulsing = listening || viewModel.isThinking)
            Spacer(Modifier.size(12.dp))
            Column {
                Text("JARVIS", style = MaterialTheme.typography.titleMedium, color = JarvisCyan)
                Text(
                    text = when {
                        listening -> "Dinliyorum..."
                        viewModel.isThinking -> "Düşünüyorum..."
                        else -> "Komut bekliyorum"
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
            items(viewModel.messages) { message ->
                MessageBubble(message)
            }
            if (viewModel.isThinking) {
                item {
                    Text(
                        "Jarvis yazıyor…",
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
                        color = JarvisCyan,
                        textAlign = TextAlign.End,
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
                placeholder = { Text("Jarvis'e yaz veya mikrofona bas", color = JarvisTextDim) },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )
            Spacer(Modifier.size(4.dp))
            IconButton(
                onClick = onToggleMic,
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        if (listening) Color(0xFFEF4444) else JarvisBlue,
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
                        viewModel.send(input)
                        input = ""
                    }
                },
                modifier = Modifier
                    .size(52.dp)
                    .background(JarvisCyan, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Gönder",
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
fun JarvisOrb(pulsing: Boolean) {
    val transition = rememberInfiniteTransition(label = "orb")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (pulsing) 1.25f else 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "orbScale"
    )
    val alpha by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = if (pulsing) 0.5f else 0.9f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "orbAlpha"
    )
    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
            .alpha(alpha)
            .background(
                brush = Brush.radialGradient(listOf(JarvisCyan, JarvisBlue)),
                shape = CircleShape
            )
    )
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == Role.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isUser) JarvisBlue else JarvisSurfaceHigh,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (message.isCommand) {
                    Text(
                        "⚡ KOMUT",
                        style = MaterialTheme.typography.labelSmall,
                        color = JarvisCyan
                    )
                    Spacer(Modifier.height(2.dp))
                }
                Text(
                    message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
