package com.hatirlatici.app

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.content.Intent
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/** Zamanı gelmiş hatırlatmaları dikkat çekici (yanıp sönen sarı/turuncu) ekranda gösterir. */
class ReminderActivity : AppCompatActivity() {

    private lateinit var root: LinearLayout
    private lateinit var list: LinearLayout
    private var anim: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(40), dp(16), dp(16))
        }
        val title = TextView(this).apply {
            text = "⏰ HATIRLATMA"
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(12))
        }
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(list) }
        val close = Button(this).apply {
            text = "ŞİMDİLİK KAPAT"
            setOnClickListener { finish() }
        }
        root.addView(title)
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(close)
        setContentView(root)

        anim = ValueAnimator.ofObject(ArgbEvaluator(), 0xFFFFD600.toInt(), 0xFFFF6D00.toInt()).apply {
            duration = 600
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { root.setBackgroundColor(it.animatedValue as Int) }
            start()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onDestroy() {
        anim?.cancel()
        super.onDestroy()
    }

    private fun refresh() {
        list.removeAllViews()
        val due = NoteStore.dueNotes(this)
        if (due.isEmpty()) {
            Alerter.cancelNotif(this)
            finish()
            return
        }
        due.forEach { n -> list.addView(card(n)) }
    }

    private fun card(n: Note): LinearLayout {
        val fg = textOn(n.color)
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(12))
            background = rounded(n.color, dp(18))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, dp(12)) }
        }
        card.addView(TextView(this).apply {
            text = n.text
            textSize = 24f
            setTextColor(fg)
        })
        card.addView(TextView(this).apply {
            text = fmtTime(n.time)
            textSize = 13f
            setTextColor(fg)
            setPadding(0, dp(6), 0, dp(8))
        })
        val buttons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val ok = Button(this).apply {
            text = "TAMAM"
            setOnClickListener {
                NoteStore.update(this@ReminderActivity, n.id) { it.status = 2 }
                refresh()
            }
        }
        val snooze = Button(this).apply {
            text = "10 DK ERTELE"
            setOnClickListener {
                NoteStore.update(this@ReminderActivity, n.id) {
                    it.time = System.currentTimeMillis() + 10 * 60 * 1000L
                    it.status = 0
                }
                Scheduler.reschedule(this@ReminderActivity)
                refresh()
            }
        }
        val lp = { LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) }
        buttons.addView(ok, lp())
        buttons.addView(snooze, lp())
        card.addView(buttons)
        return card
    }
}
