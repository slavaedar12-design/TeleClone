package com.melosuy.teleclone

import androidx.compose.ui.graphics.Color

enum class ChatFolder(val title: String) {
    ALL("Все чаты"),
    PERSONAL("Личные"),
    CHANNELS("Каналы"),
    BOTS("Боты")
}

enum class ChatType {
    PRIVATE, GROUP, CHANNEL, BOT
}

enum class MessageStatus {
    SENDING, SENT, READ
}

data class Message(
    val id: Long,
    val senderName: String,
    val text: String,
    val time: String,
    val isOutgoing: Boolean,
    val status: MessageStatus = MessageStatus.READ,
    val replyToText: String? = null,
    val isVoice: Boolean = false,
    val voiceDurationSec: Int = 0,
    val waveform: List<Float> = emptyList(),
    val reaction: String? = null
)

data class ChatItem(
    val id: String,
    val title: String,
    val handle: String,
    val avatarColor: Color,
    val type: ChatType,
    val isVerified: Boolean = false,
    val isPinned: Boolean = false,
    val isOnline: Boolean = false,
    val unreadCount: Int = 0,
    val isMuted: Boolean = false,
    val lastTyping: Boolean = false,
    val messages: MutableList<Message>
)
