package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppNotificationEntity
import com.example.data.model.RecurringBillEntity
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationCenterDialog(
    notifications: List<AppNotificationEntity>,
    bills: List<RecurringBillEntity>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
    onMarkRead: (Long) -> Unit,
    onDeleteNotification: (Long) -> Unit,
    onClearAll: () -> Unit,
    onPayBill: (RecurringBillEntity) -> Unit
) {
    val unreadCount = notifications.count { !it.isRead }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("notification_center_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxHeight()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Pusat Notifikasi",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ExpenseRed.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$unreadCount baru",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_notification_center_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Subheader Actions
                if (notifications.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (unreadCount > 0) {
                            TextButton(
                                onClick = onMarkAllRead,
                                modifier = Modifier.testTag("mark_all_read_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tandai Semua Dibaca",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        TextButton(
                            onClick = onClearAll,
                            modifier = Modifier.testTag("clear_all_notifications_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ClearAll,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Hapus Semua",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tidak Ada Pemberitahuan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Peringatan anggaran & pengingat tagihan akan muncul di sini.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notifications, key = { it.id }) { notif ->
                            NotificationItemCard(
                                notification = notif,
                                bills = bills,
                                onMarkRead = { onMarkRead(notif.id) },
                                onDelete = { onDeleteNotification(notif.id) },
                                onPayBill = onPayBill
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItemCard(
    notification: AppNotificationEntity,
    bills: List<RecurringBillEntity>,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit,
    onPayBill: (RecurringBillEntity) -> Unit
) {
    val (icon, tintColor, bgColor) = when (notification.type) {
        "BUDGET_EXCEEDED" -> Triple(
            Icons.Default.Error,
            ExpenseRed,
            ExpenseRed.copy(alpha = 0.10f)
        )
        "BUDGET_WARNING" -> Triple(
            Icons.Default.Warning,
            AmberWarning,
            AmberWarning.copy(alpha = 0.12f)
        )
        "BILL_OVERDUE" -> Triple(
            Icons.Default.AccessTime,
            ExpenseRed,
            ExpenseRed.copy(alpha = 0.12f)
        )
        "BILL_DUE" -> Triple(
            Icons.Default.Payment,
            Color(0xFF1976D2),
            Color(0xFF1976D2).copy(alpha = 0.10f)
        )
        else -> Triple(
            Icons.Default.CheckCircle,
            IncomeGreen,
            IncomeGreen.copy(alpha = 0.10f)
        )
    }

    val matchedBill = if (notification.type.startsWith("BILL_") && notification.relatedId != null) {
        bills.find { it.id == notification.relatedId }
    } else null

    val timeFormatted = remember(notification.timestampMillis) {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.forLanguageTag("id-ID"))
        sdf.format(Date(notification.timestampMillis))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.isRead) 0.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!notification.isRead) onMarkRead() }
            .testTag("notification_item_${notification.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Icon avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title and Message
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notification.title,
                            fontSize = 14.sp,
                            fontWeight = if (notification.isRead) FontWeight.SemiBold else FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!notification.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = notification.message,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timeFormatted,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (matchedBill != null) {
                                Button(
                                    onClick = { onPayBill(matchedBill) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                        horizontal = 10.dp,
                                        vertical = 4.dp
                                    ),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("pay_bill_btn_${matchedBill.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Payment,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Bayar Sekarang", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Hapus Notifikasi",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
