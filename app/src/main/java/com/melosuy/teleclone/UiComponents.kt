package com.melosuy.teleclone

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object ChatRepository {
    val chats = mutableStateListOf(
        ChatItem(
            id = "saved",
            title = "Избранное",
            handle = "@melosuy",
            avatarColor = Color(0xFF3390EC),
            type = ChatType.PRIVATE,
            isPinned = true,
            messages = mutableStateListOf(
                Message(1, "Вы", "Координаты шлюза: 55.7558° N, 37.6173° E", "10:12", true, MessageStatus.READ),
                Message(2, "Вы", "Сборка APK через GitHub Actions подключена.", "11:40", true, MessageStatus.READ)
            )
        ),
        ChatItem(
            id = "durov",
            title = "Pavel Durov",
            handle = "@durov",
            avatarColor = Color(0xFF2B82D9),
            type = ChatType.CHANNEL,
            isVerified = true,
            isPinned = true,
            unreadCount = 2,
            messages = mutableStateListOf(
                Message(10, "Pavel Durov", "Скорость, безопасность и минимализм интерфейса остаются главным приоритетом.", "14:05", false, reaction = "🔥"),
                Message(11, "Pavel Durov", "Новые анимации, папки чатов и мгновенная синхронизация уже доступны.", "14:22", false)
            )
        ),
        ChatItem(
            id = "sector_hub",
            title = "Sector Zero // Штаб",
            handle = "@sector_zero",
            avatarColor = Color(0xFFE56555),
            type = ChatType.GROUP,
            isOnline = true,
            unreadCount = 5,
            messages = mutableStateListOf(
                Message(20, "Оператор-07", "Канал связи стабилен. Задержка 4 мс.", "15:10", false),
                Message(21, "Вы", "Принято. Продолжаем мониторинг.", "15:11", true, MessageStatus.READ),
                Message(
                    22, "Оператор-07", "", "15:14", false,
                    isVoice = true, voiceDurationSec = 14,
                    waveform = List(28) { Random.nextFloat().coerceIn(0.15f, 1.0f) }
                )
            )
        ),
        ChatItem(
            id = "bot_synth",
            title = "Sintezator Bot",
            handle = "@melosuy_bot",
            avatarColor = Color(0xFF52B466),
            type = ChatType.BOT,
            isVerified = true,
            isOnline = true,
            messages = mutableStateListOf(
                Message(30, "Sintezator Bot", "Ядро готово к работе. Отправьте любую команду или текст.", "09:00", false)
            )
        ),
        ChatItem(
            id = "alina",
            title = "Алина",
            handle = "@alina_k",
            avatarColor = Color(0xFF9C6ADE),
            type = ChatType.PRIVATE,
            isOnline = true,
            unreadCount = 1,
            messages = mutableStateListOf(
                Message(40, "Алина", "Привет! Ты уже протестировал сборку на телефоне?", "16:48", false)
            )
        )
    )

    fun nowTime(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    fun sendMessage(chatId: String, text: String, replyTo: String? = null) {
        val idx = chats.indexOfFirst { it.id == chatId }
        if (idx == -1) return
        val chat = chats[idx]
        val msg = Message(
            id = System.currentTimeMillis(),
            senderName = "Вы",
            text = text,
            time = nowTime(),
            isOutgoing = true,
            status = MessageStatus.READ,
            replyToText = replyTo
        )
        chat.messages.add(msg)
        chats[idx] = chat.copy(unreadCount = 0)
    }

    fun sendVoiceMessage(chatId: String, durationSec: Int) {
        val idx = chats.indexOfFirst { it.id == chatId }
        if (idx == -1) return
        val chat = chats[idx]
        val msg = Message(
            id = System.currentTimeMillis(),
            senderName = "Вы",
            text = "",
            time = nowTime(),
            isOutgoing = true,
            status = MessageStatus.READ,
            isVoice = true,
            voiceDurationSec = durationSec.coerceAtLeast(1),
            waveform = List(28) { Random.nextFloat().coerceIn(0.18f, 1.0f) }
        )
        chat.messages.add(msg)
        chats[idx] = chat.copy(unreadCount = 0)
    }

    fun receiveAutoReply(chatId: String, userText: String) {
        val idx = chats.indexOfFirst { it.id == chatId }
        if (idx == -1) return
        val chat = chats[idx]
        if (chat.id == "saved" || chat.type == ChatType.CHANNEL) return

        val reply = when (chat.type) {
            ChatType.BOT -> "Выполнено: «$userText» обработано за 0.02 сек."
            ChatType.GROUP -> "Принято по каналу связи."
            else -> "Понял, принято: $userText"
        }
        chat.messages.add(
            Message(
                id = System.currentTimeMillis() + 1,
                senderName = chat.title,
                text = reply,
                time = nowTime(),
                isOutgoing = false
            )
        )
        chats[idx] = chat.copy()
    }

    fun toggleReaction(chatId: String, messageId: Long, emoji: String) {
        val chat = chats.find { it.id == chatId } ?: return
        val mIdx = chat.messages.indexOfFirst { it.id == messageId }
        if (mIdx != -1) {
            val m = chat.messages[mIdx]
            chat.messages[mIdx] = m.copy(reaction = if (m.reaction == emoji) null else emoji)
        }
    }

    fun markRead(chatId: String) {
        val idx = chats.indexOfFirst { it.id == chatId }
        if (idx != -1 && chats[idx].unreadCount > 0) {
            chats[idx] = chats[idx].copy(unreadCount = 0)
        }
    }
}
