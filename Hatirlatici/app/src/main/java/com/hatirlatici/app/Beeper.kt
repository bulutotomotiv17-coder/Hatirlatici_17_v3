package com.hatirlatici.app

import android.media.AudioManager
import android.media.ToneGenerator

object Beeper {
    /** Tam olarak iki kez bip sesi çalar (alarm ses seviyesinde). Bloklar, arka plan thread'inde çağırın. */
    fun twice() {
        try {
            val tg = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            tg.startTone(ToneGenerator.TONE_PROP_BEEP, 400)
            Thread.sleep(800)
            tg.startTone(ToneGenerator.TONE_PROP_BEEP, 400)
            Thread.sleep(600)
            tg.release()
        } catch (_: Exception) {
        }
    }
}
