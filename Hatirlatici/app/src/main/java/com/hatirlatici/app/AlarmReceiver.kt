package com.hatirlatici.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val app = context.applicationContext
        Thread {
            try {
                val newly = NoteStore.markDue(app)
                if (newly > 0) {
                    Alerter.createChannels(app)
                    Alerter.notifyDue(app)
                    Alerter.launch(app)
                    Beeper.twice()          // yalnızca iki bip
                }
                Scheduler.reschedule(app)
            } finally {
                pending.finish()
            }
        }.start()
    }
}
