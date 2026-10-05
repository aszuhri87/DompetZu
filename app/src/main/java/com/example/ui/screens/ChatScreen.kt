package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.gemini.ChatMessage
import com.example.data.gemini.GeminiChatModel
import com.example.data.gemini.MessageRole
import com.example.data.model.MonthlyReportData
import com.example.data.util.MonthlyReportCalculator
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    selectedModel: GeminiChatModel,
    inputText: String,
    monthlyReport: MonthlyReportData?,
    onInputChanged: (String) -> Unit,
    onSendMessage: (text: String, financialContext: String?) -> Unit,
    onSelectModel: (GeminiChatModel) -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new messages
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Prepare live financial context string from monthlyReport
    val financialContextSummary = monthlyReport?.let { report ->
        """
        Periode: ${report.monthTitle}
        Total Pemasukan: ${MonthlyReportCalculator.formatRupiah(report.totalIncome)}
        Total Pengeluaran: ${MonthlyReportCalculator.formatRupiah(report.totalExpense)}
        Arus Kas Bersih: ${MonthlyReportCalculator.formatRupiah(report.netCashFlow)}
        Rasio Tabungan: ${String.format(Locale.US, "%.1f", report.savingsRate)}%
        Skor Kesehatan Finansial: ${report.financialHealth.score}/100 (${report.financialHealth.level})
        Kategori Terbesar: ${report.topExpenseCategory?.categoryName ?: "Belum ada"} (${MonthlyReportCalculator.formatRupiah(report.topExpenseCategory?.totalAmount ?: 0.0)})
        """.trimIndent()
    }

    val suggestionPrompts = listOf(
        "📊 Analisis Keuangan Saya",
        "💡 Tips Hemat Pengeluaran",
        "🎯 Strategi Anggaran 50/30/20",
        "📈 Rekomendasi Dana Darurat"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .testTag("chat_screen")
    ) {
        // Scrollable Message Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageBubble(message = message)
            }

            if (isLoading) {
                item {
                    LoadingBubble()
                }
            }
        }

        // Quick Suggestion Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suggestionPrompts) { prompt ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable(enabled = !isLoading) {
                            val context = if (prompt.contains("Analisis", ignoreCase = true)) {
                                financialContextSummary
                            } else null
                            onSendMessage(prompt, context)
                        }
                        .testTag("suggestion_chip_$prompt")
                ) {
                    Text(
                        text = prompt,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Input Bar
        ChatInputBar(
            inputText = inputText,
            isLoading = isLoading,
            onInputChanged = onInputChanged,
            onSend = {
                onSendMessage(inputText, financialContextSummary)
            }
        )
    }
}



@Composable
fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == MessageRole.USER
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("message_bubble_${message.id}"),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(EmeraldDark, EmeraldLight)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "AI",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) {
                MaterialTheme.colorScheme.primary
            } else if (message.isError) {
                ExpenseRed.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surface
            },
            border = if (!isUser && !message.isError) {
                androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            } else null,
            shadowElevation = if (isUser) 2.dp else 1.dp,
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                FormattedChatMessageText(
                    text = message.text,
                    isUser = isUser,
                    isError = message.isError
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = timeFormat.format(Date(message.timestamp)),
                    fontSize = 9.sp,
                    color = if (isUser) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun FormattedChatMessageText(
    text: String,
    isUser: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val lines = remember(text) { text.split("\n") }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        lines.forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.isBlank() -> {
                    Spacer(modifier = Modifier.height(2.dp))
                }
                trimmed.startsWith("#") -> {
                    // Header title line (#, ##, ###) - render as styled highlighted header without '#'
                    val headerText = trimmed.replace(Regex("^#+\\s*"), "")
                    val annotated = buildInlineFormattedString(headerText, isUser = isUser, isHeader = true)

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isUser) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = annotated,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUser) Color.White else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            lineHeight = 18.sp
                        )
                    }
                }
                trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                    // Bullet list item - render clean bullet without '*'
                    val bulletText = trimmed.substring(2).trim()
                    val annotated = buildInlineFormattedString(bulletText, isUser = isUser, isHeader = false)
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.padding(start = 2.dp, top = 1.dp, bottom = 1.dp)
                    ) {
                        Text(
                            text = "• ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isUser) Color.White else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = annotated,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = if (isUser) Color.White else if (isError) ExpenseRed else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                else -> {
                    // Normal paragraph text
                    val annotated = buildInlineFormattedString(trimmed, isUser = isUser, isHeader = false)
                    Text(
                        text = annotated,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = if (isUser) Color.White else if (isError) ExpenseRed else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun buildInlineFormattedString(
    rawText: String,
    isUser: Boolean,
    isHeader: Boolean
): AnnotatedString {
    val headerColor = MaterialTheme.colorScheme.primary
    val bodyColor = MaterialTheme.colorScheme.onSurface

    return remember(rawText, isUser, isHeader, headerColor, bodyColor) {
        buildAnnotatedString {
            // Strip any remaining stray hash symbols
            val cleanSource = rawText.replace("#", "")
            val pattern = Regex("(\\*\\*|\\*)(.*?)\\1")
            var lastIndex = 0

            for (match in pattern.findAll(cleanSource)) {
                if (match.range.first > lastIndex) {
                    val plainChunk = cleanSource.substring(lastIndex, match.range.first).replace("*", "")
                    append(plainChunk)
                }
                val matchedContent = match.groupValues[2].replace("*", "")
                val startSpan = length
                append(matchedContent)
                val endSpan = length

                addStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isUser) Color.White else if (isHeader) headerColor else bodyColor
                    ),
                    start = startSpan,
                    end = endSpan
                )

                lastIndex = match.range.last + 1
            }

            if (lastIndex < cleanSource.length) {
                val remainingChunk = cleanSource.substring(lastIndex).replace("*", "")
                append(remainingChunk)
            }
        }
    }
}

@Composable
fun LoadingBubble(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "pulse_anim")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("loading_bubble"),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(EmeraldDark, EmeraldLight)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = "AI Berpikir",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Asisten sedang berpikir...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    inputText: String,
    isLoading: Boolean,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChanged,
                placeholder = { Text("Tanya seputar keuangan...", fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            val canSend = inputText.isNotBlank() && !isLoading

            IconButton(
                onClick = onSend,
                enabled = canSend,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("send_chat_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Kirim",
                    tint = if (canSend) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
