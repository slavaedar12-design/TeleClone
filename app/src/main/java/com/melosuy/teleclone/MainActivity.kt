package com.melosuy.teleclone

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class TgPalette(
    val bgPrimary: Color,
    val bgSecondary: Color,
    val headerBg: Color,
    val outgoingBubble: Color,
    val incomingBubble: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val unreadBadge: Color
)

fun telegramPalette(dark: Boolean): TgPalette = if (dark) {
    TgPalette(
        bgPrimary = Color(0xFF17212B),
        bgSecondary = Color(0xFF0E1621),
        headerBg = Color(0xFF17212B),
        outgoingBubble = Color(0xFF2B5278),
        incomingBubble = Color(0xFF182533),
        accent = Color(0xFF5288C1),
        textPrimary = Color(0xFFF5F5F5),
        textSecondary = Color(0xFF7F91A4),
        divider = Color(0xFF101921),
        unreadBadge = Color(0xFF3E78B2)
    )
} else {
    TgPalette(
        bgPrimary = Color(0xFFFFFFFF),
        bgSecondary = Color(0xFFD5E3EE),
        headerBg = Color(0xFF517DA2),
        outgoingBubble = Color(0xFFEEFFDE),
        incomingBubble = Color(0xFFFFFFFF),
        accent = Color(0xFF3390EC),
        textPrimary = Color(0xFF000000),
        textSecondary = Color(0xFF707579),
        divider = Color(0xFFE6E6E6),
        unreadBadge = Color(0xFF4ECC5E)
    )
}

@Composable
fun AvatarCircle(title: String, color: Color, isSaved: Boolean = false, sizeDp: Int = 52) {
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        if (isSaved) {
            Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color.White, modifier = Modifier.size((sizeDp * 0.5f).dp))
        } else {
            val initials = title.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
            Text(
                text = initials.ifEmpty { "T" },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (sizeDp * 0.36f).sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    onOpenChat: (String) -> Unit
) {
    val palette = telegramPalette(isDark)
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedFolder by remember { mutableStateOf(ChatFolder.ALL) }
    var searchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredChats = ChatRepository.chats
        .filter { chat ->
            val folderOk = when (selectedFolder) {
                ChatFolder.ALL -> true
                ChatFolder.PERSONAL -> chat.type == ChatType.PRIVATE
                ChatFolder.CHANNELS -> chat.type == ChatType.CHANNEL || chat.type == ChatType.GROUP
                ChatFolder.BOTS -> chat.type == ChatType.BOT
            }
            val searchOk = searchQuery.isBlank() ||
                chat.title.contains(searchQuery, ignoreCase = true) ||
                chat.messages.any { it.text.contains(searchQuery, ignoreCase = true) }
            folderOk && searchOk
        }
        .sortedByDescending { it.isPinned }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = palette.bgPrimary,
                modifier = Modifier.width(300.dp)
            ) {
                // Шапка профиля в стиле Telegram
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDark) Color(0xFF1F2C3A) else Color(0xFF517DA2))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarCircle("Мисуй Мисуйич", Color(0xFF3390EC), sizeDp = 60)
                            IconButton(onClick = onToggleTheme) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Тема",
                                    tint = Color.White
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Мисуй Мисуйич", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("+7 (999) 000-00-01 • @melosuy", color = Color.White.copy(alpha = 0.78f), fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))
                DrawerMenuItem(Icons.Default.Group, "Создать группу", palette) { scope.launch { drawerState.close() } }
                DrawerMenuItem(Icons.Default.Person, "Контакты", palette) { scope.launch { drawerState.close() } }
                DrawerMenuItem(Icons.Default.Call, "Звонки", palette) { scope.launch { drawerState.close() } }
                DrawerMenuItem(Icons.Default.BookmarkBorder, "Избранное", palette) {
                    scope.launch { drawerState.close() }
                    onOpenChat("saved")
                }
                DrawerMenuItem(Icons.Default.Settings, "Настройки", palette) { scope.launch { drawerState.close() } }
            }
        }
    ) {
        Scaffold(
            containerColor = palette.bgPrimary,
            topBar = {
                Column(modifier = Modifier.background(palette.headerBg)) {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = palette.headerBg,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        ),
                        navigationIcon = {
                            if (searchActive) {
                                IconButton(onClick = { searchActive = false; searchQuery = "" }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                                }
                            } else {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Меню")
                                }
                            }
                        },
                        title = {
                            if (searchActive) {
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Поиск", color = Color.White.copy(0.65f)) },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = Color.White,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    )
                                )
                            } else {
                                Text("Telegram", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        },
                        actions = {
                            if (!searchActive) {
                                IconButton(onClick = { searchActive = true }) {
                                    Icon(Icons.Default.Search, contentDescription = "Поиск")
                                }
                            }
                        }
                    )

                    // Папки чатов (вкладки как в Telegram)
                    ScrollableTabRow(
                        selectedTabIndex = ChatFolder.entries.indexOf(selectedFolder),
                        containerColor = palette.headerBg,
                        contentColor = Color.White,
                        edgePadding = 12.dp,
                        divider = {}
                    ) {
                        ChatFolder.entries.forEach { folder ->
                            val selected = selectedFolder == folder
                            Tab(
                                selected = selected,
                                onClick = { selectedFolder = folder },
                                text = {
                                    Text(
                                        text = folder.title,
                                        color = if (selected) Color.White else Color.White.copy(alpha = 0.65f),
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
                                }
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onOpenChat("bot_synth") },
                    containerColor = palette.accent,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Новое сообщение")
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(filteredChats, key = { it.id }) { chat ->
                    ChatRowItem(chat = chat, palette = palette, onClick = {
                        ChatRepository.markRead(chat.id)
                        onOpenChat(chat.id)
                    })
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 78.dp),
                        thickness = 0.6.dp,
                        color = palette.divider
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(icon: ImageVector, label: String, palette: TgPalette, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = palette.textSecondary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(24.dp))
        Text(label, color = palette.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ChatRowItem(chat: ChatItem, palette: TgPalette, onClick: () -> Unit) {
    val lastMsg = chat.messages.lastOrNull()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            AvatarCircle(chat.title, chat.avatarColor, isSaved = chat.id == "saved", sizeDp = 54)
            if (chat.isOnline) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(palette.bgPrimary)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4ECC5E))
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.title,
                    color = palette.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (chat.isVerified) {
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.Verified, contentDescription = null, tint = palette.accent, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.weight(1f))
                if (lastMsg?.isOutgoing == true) {
                    Icon(
                        Icons.Default.DoneAll,
                        contentDescription = null,
                        tint = palette.accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text = lastMsg?.time ?: "",
                    color = palette.textSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val preview = when {
                    lastMsg == null -> "Нет сообщений"
                    lastMsg.isVoice -> "🎤 Голосовое сообщение (${lastMsg.voiceDurationSec} сек)"
                    chat.type == ChatType.GROUP && !lastMsg.isOutgoing -> "${lastMsg.senderName}: ${lastMsg.text}"
                    else -> lastMsg.text
                }
                Text(
                    text = preview,
                    color = if (lastMsg?.isVoice == true) palette.accent else palette.textSecondary,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (chat.unreadCount > 0) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .height(22.dp)
                            .defaultMinSize(minWidth = 22.dp)
                            .clip(CircleShape)
                            .background(palette.unreadBadge)
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (chat.isPinned) {
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.PushPin, contentDescription = null, tint = palette.textSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    isDark: Boolean,
    onBack: () -> Unit
) {
    val palette = telegramPalette(isDark)
    val chat = ChatRepository.chats.find { it.id == chatId } ?: return
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var inputText by remember { mutableStateOf("") }
    var replyTarget by remember { mutableStateOf<Message?>(null) }
    var isTyping by remember { mutableStateOf(false) }

    LaunchedEffect(chat.messages.size) {
        if (chat.messages.isNotEmpty()) {
            listState.animateScrollToItem(chat.messages.size - 1)
        }
    }

    Scaffold(
        containerColor = palette.bgSecondary,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = palette.headerBg,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarCircle(chat.title, chat.avatarColor, isSaved = chat.id == "saved", sizeDp = 40)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(chat.title, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1)
                                if (chat.isVerified) {
                                    Spacer(Modifier.width(4.dp))
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF64B5F6), modifier = Modifier.size(16.dp))
                                }
                            }
                            val statusText = when {
                                isTyping -> "печатает..."
                                chat.id == "saved" -> "облачное хранилище"
                                chat.type == ChatType.CHANNEL -> "142 800 подписчиков"
                                chat.type == ChatType.BOT -> "бот"
                                chat.isOnline -> "в сети"
                                else -> "был(а) недавно"
                            }
                            Text(
                                text = statusText,
                                fontSize = 12.sp,
                                color = if (isTyping || chat.isOnline) Color(0xFF64B5F6) else Color.White.copy(alpha = 0.72f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.Call, contentDescription = "Звонок") }
                    IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, contentDescription = "Ещё") }
                }
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(palette.bgPrimary)) {
                // Плашка ответа (Reply)
                replyTarget?.let { target ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(34.dp)
                                .background(palette.accent)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("В ответ ${target.senderName}", color = palette.accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = if (target.isVoice) "🎤 Голосовое сообщение" else target.text,
                                color = palette.textSecondary,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { replyTarget = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Отмена", tint = palette.textSecondary)
                        }
                    }
                }

                // Нижняя панель ввода Telegram
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        inputText += "🔥"
                    }) {
                        Icon(Icons.Default.SentimentSatisfiedAlt, contentDescription = "Стикеры", tint = palette.textSecondary)
                    }

                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Сообщение", color = palette.textSecondary) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = palette.textPrimary,
                            unfocusedTextColor = palette.textPrimary,
                            cursorColor = palette.accent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    if (inputText.isBlank()) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Файл", tint = palette.textSecondary)
                        }
                        IconButton(onClick = {
                            ChatRepository.sendVoiceMessage(chat.id, (4..18).random())
                        }) {
                            Icon(Icons.Default.Mic, contentDescription = "Голосовое", tint = palette.accent)
                        }
                    } else {
                        IconButton(onClick = {
                            val textToSend = inputText.trim()
                            val rep = replyTarget?.text
                            inputText = ""
                            replyTarget = null
                            ChatRepository.sendMessage(chat.id, textToSend, rep)

                            if (chat.id != "saved" && chat.type != ChatType.CHANNEL) {
                                scope.launch {
                                    isTyping = true
                                    delay(900)
                                    isTyping = false
                                    ChatRepository.receiveAutoReply(chat.id, textToSend)
                                }
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Отправить", tint = palette.accent)
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(chat.messages, key = { it.id }) { msg ->
                MessageBubble(
                    msg = msg,
                    showSender = chat.type == ChatType.GROUP && !msg.isOutgoing,
                    palette = palette,
                    isDark = isDark,
                    onClick = { replyTarget = msg },
                    onDoubleClick = { ChatRepository.toggleReaction(chat.id, msg.id, "❤️") }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    msg: Message,
    showSender: Boolean,
    palette: TgPalette,
    isDark: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit
) {
    val bubbleColor = if (msg.isOutgoing) palette.outgoingBubble else palette.incomingBubble
    val alignment = if (msg.isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (msg.isOutgoing) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    var playingVoice by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Column(
            modifier = Modifier
                .widthIn(max = 310.dp)
                .clip(shape)
                .background(bubbleColor)
                .combinedClickable(onClick = onClick, onDoubleClick = onDoubleClick)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (showSender) {
                Text(msg.senderName, color = palette.accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(2.dp))
            }

            msg.replyToText?.let { rep ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.width(2.5.dp).height(24.dp).background(palette.accent))
                    Spacer(Modifier.width(6.dp))
                    Text(rep, color = palette.textPrimary.copy(alpha = 0.85f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(4.dp))
            }

            if (msg.isVoice) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(palette.accent)
                            .clickable { playingVoice = !playingVoice },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (playingVoice) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Canvas(modifier = Modifier.width(150.dp).height(22.dp)) {
                            val bars = msg.waveform.ifEmpty { List(24) { 0.5f } }
                            val barW = size.width / (bars.size * 1.5f)
                            bars.forEachIndexed { idx, amp ->
                                val h = size.height * amp
                                drawRoundRect(
                                    color = if (playingVoice && idx < 12) Color.White else palette.accent,
                                    topLeft = Offset(idx * barW * 1.5f, (size.height - h) / 2f),
                                    size = Size(barW, h),
                                    cornerRadius = CornerRadius(2f, 2f)
                                )
                            }
                        }
                        Text(
                            text = String.format("0:%02d", msg.voiceDurationSec),
                            color = palette.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                Text(
                    text = msg.text,
                    color = palette.textPrimary,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
            }

            Spacer(Modifier.height(3.dp))

            Row(
                modifier = Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                msg.reaction?.let { emoji ->
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.18f), CircleShape)
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(emoji, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = msg.time,
                    color = if (msg.isOutgoing && !isDark) Color(0xFF4FAE4E) else palette.textSecondary,
                    fontSize = 11.sp
                )
                if (msg.isOutgoing) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Default.DoneAll,
                        contentDescription = null,
                        tint = if (!isDark) Color(0xFF4FAE4E) else Color(0xFF64B5F6),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
