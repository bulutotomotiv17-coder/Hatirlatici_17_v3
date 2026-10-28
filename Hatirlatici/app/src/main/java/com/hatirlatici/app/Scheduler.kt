package com.hatirlatici.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object Scheduler {
    /** En yakın hatırlatma için tek bir alarm kurar. */
    fun reschedule(ctx: Context) {
        val app = ctx.applicationContext
        val am = app.getSystemService(AlarmManager::class.java)
        val pi = PendingIntent.getBroadcast(
            app, 100, Intent(app, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val now = System.currentTimeMillis()
        val next = NoteStore.all(app)
            .filter { it.status == 0 && it.time > now }
            .minOfOrNull { it.time }
        if (next == null) {
            am.cancel(pi)
        } else {
            val show = PendingIntent.getActivity(
                app, 101, Intent(app, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.setAlarmClock(AlarmManager.AlarmClockInfo(next, show), pi)
        }
    }
}
