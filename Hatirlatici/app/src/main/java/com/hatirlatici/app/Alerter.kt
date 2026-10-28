package com.hatirlatici.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object Alerter {
    const val CH_DUE = "due_channel"
    const val CH_SERVICE = "service_channel"
    private const val NOTIF_DUE = 2

    fun createChannels(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        val due = NotificationChannel(CH_DUE, "Hatırlatmalar", NotificationManager.IMPORTANCE_HIGH).apply {
            setSound(null, null)       // ses: sadece iki bip (Beeper)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        val svc = NotificationChannel(CH_SERVICE, "Arka plan servisi", NotificationManager.IMPORTANCE_MIN)
        nm.createNotificationChannel(due)
        nm.createNotificationChannel(svc)
    }

    private fun reminderIntent(ctx: Context) =
        Intent(ctx, ReminderActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)

    /** Hatırlatma ekranını doğrudan açar ("başka uygulamaların üzerinde göster" izni ile arka plandan da çalışır). */
    fun launch(ctx: Context) {
        try {
            ctx.startActivity(reminderIntent(ctx))
        } catch (_: Exception) {
        }
    }

    /** Tam ekran bildirim: kilit ekranında ekranı uyandırıp hatırlatmayı gösterir (yedek yol). */
    fun notifyDue(ctx: Context) {
        val due = NoteStore.dueNotes(ctx)
        if (due.isEmpty()) return
        val pi = PendingIntent.getActivity(
            ctx, 200, reminderIntent(ctx),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = Notification.Builder(ctx, CH_DUE)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("⏰ Hatırlatma")
            .setContentText(due.joinToString(" • ") { it.text.take(40) })
            .setCategory(Notification.CATEGORY_ALARM)
            .setContentIntent(pi)
            .setFullScreenIntent(pi, true)
            .setAutoCancel(true)
            .build()
        ctx.getSystemService(NotificationManager::class.java).notify(NOTIF_DUE, n)
    }

    fun cancelNotif(ctx: Context) {
        ctx.getSystemService(NotificationManager::class.java).cancel(NOTIF_DUE)
    }
}
