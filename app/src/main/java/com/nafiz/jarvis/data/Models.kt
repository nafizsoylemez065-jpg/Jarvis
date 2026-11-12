package com.nafiz.jarvis.data

enum class Role { USER, ASSISTANT }

data class ChatMessage(
    val role: Role,
    val text: String,
    val isCommand: Boolean = false,
    val time: Long = System.currentTimeMillis()
)
