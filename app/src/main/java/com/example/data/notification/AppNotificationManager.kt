package com.example.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object AppNotificationManager {

    const val CHANNEL_BUDGET_ALERTS = "channel_budget_alerts"
    const val CHANNEL_BILL_REMINDERS = "channel_bill_reminders"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return

            // Channel for Budget Alerts
            val budgetChannel = NotificationChannel(
                CHANNEL_BUDGET_ALERTS,
                "Peringatan Anggaran",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi saat pengeluaran kategori mendekati atau melebihi batas anggaran bulanan"
                enableVibration(true)
            }

            // Channel for Recurring Bill Reminders
            val billChannel = NotificationChannel(
                CHANNEL_BILL_REMINDERS,
                "Pengingat Tagihan Rutin",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pengingat tanggal jatuh tempo tagihan rutin"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(budgetChannel)
            notificationManager.createNotificationChannel(billChannel)
        }
    }

    fun sendBudgetAlertNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        isExceeded: Boolean
    ) {
        createNotificationChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_BUDGET_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(if (isExceeded) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Permission revoked or not granted
        }
    }

    fun sendBillReminderNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        isOverdue: Boolean
    ) {
        createNotificationChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_BILL_REMINDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(if (isOverdue) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Permission revoked or not granted
        }
    }
}
