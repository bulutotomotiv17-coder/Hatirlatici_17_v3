package com.hatirlatici.app

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat

/**
 * Kilit her açıldığında (ACTION_USER_PRESENT) zamanı gelmiş ve henüz onaylanmamış
 * hatırlatmaları ekranda gösterir. Bu yayın manifest ile dinlenemediği için
 * küçük bir ön plan servisi gerekir.
 */
class UnlockService : Service() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            if (i.action == Intent.ACTION_USER_PRESENT) {
                NoteStore.markDue(c)
                if (NoteStore.dueNotes(c).isNotEmpty()) Alerter.launch(c)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Alerter.createChannels(this)
        val n = Notification.Builder(this, Alerter.CH_SERVICE)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Hatırlatıcı çalışıyor")
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, n)
        }
        ContextCompat.registerReceiver(
            this, receiver, IntentFilter(Intent.ACTION_USER_PRESENT),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        NoteStore.markDue(this)
        Scheduler.reschedule(this)
        return START_STICKY
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(receiver)
        } catch (_: Exception) {
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun start(ctx: Context) {
            ContextCompat.startForegroundService(ctx, Intent(ctx, UnlockService::class.java))
        }
    }
}
