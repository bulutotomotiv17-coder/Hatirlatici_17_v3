package com.hatirlatici.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NoteStore.markDue(context)
        Scheduler.reschedule(context)
        try {
            UnlockService.start(context)
        } catch (_: Exception) {
        }
    }
}
