package com.hatirlatici.app

import android.Manifest
import android.app.DatePickerDialog
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var emptyView: TextView
    private val noteAdapter = NoteAdapter { showEditor(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Hatırlatıcı"

        val root = FrameLayout(this)
        root.setBackgroundColor(0xFFF3F0F7.toInt())

        val recycler = RecyclerView(this).apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = noteAdapter
            setPadding(dp(6), dp(6), dp(6), dp(96))
            clipToPadding = false
        }
        root.addView(
            recycler,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        )

        emptyView = TextView(this).apply {
            text = "Henüz not yok.\nSağ alttaki + ile ekleyin."
            gravity = Gravity.CENTER
            textSize = 16f
            setTextColor(0xFF666666.toInt())
        }
        root.addView(
            emptyView,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        )

        val fab = FloatingActionButton(this).apply {
            setImageResource(android.R.drawable.ic_input_add)
            setOnClickListener { showEditor(null) }
        }
        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.END
        ).apply { setMargins(0, 0, dp(20), dp(24)) }
        root.addView(fab, lp)

        setContentView(root)

        Alerter.createChannels(this)
        UnlockService.start(this)
    }

    override fun onResume() {
        super.onResume()
        NoteStore.markDue(this)
        Scheduler.reschedule(this)
        refresh()
        checkSetup()
    }

    private fun refresh() {
        fun order(n: Note) = when {
            n.status == 1 -> 0
            n.status == 2 -> 3
            n.time > 0 -> 1
            else -> 2
        }
        val list = NoteStore.all(this).sortedWith(
            compareBy<Note>({ order(it) }, { if (it.time == 0L) Long.MAX_VALUE else it.time })
        )
        noteAdapter.submit(list)
        emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    // ---------- İzinler ----------

    private fun checkSetup() {
        val prefs = getSharedPreferences("setup", MODE_PRIVATE)
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
            !prefs.getBoolean("asked_notif", false)
        ) {
            prefs.edit().putBoolean("asked_notif", true).apply()
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            return
        }
        if (askOnce(
                "overlay", !Settings.canDrawOverlays(this),
                "Diğer uygulamaların üzerinde göster",
                "Hatırlatma ekranının telefon kilidini her açtığınızda ve zamanı geldiğinde otomatik belirmesi için bu izni açın."
            ) {
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            }
        ) return

        val fsiMissing = Build.VERSION.SDK_INT >= 34 &&
            !getSystemService(NotificationManager::class.java).canUseFullScreenIntent()
        if (askOnce(
                "fsi", fsiMissing,
                "Tam ekran bildirim",
                "Kilitli ekranda hatırlatmanın ekranı uyandırıp açılabilmesi için tam ekran bildirim iznini açın."
            ) {
                Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:$packageName"))
            }
        ) return

        val battery = !getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(packageName)
        askOnce(
            "battery", battery,
            "Pil optimizasyonu",
            "Uygulamanın arka planda kapatılmaması için pil optimizasyonunu devre dışı bırakın."
        ) {
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName"))
        }
    }

    private fun askOnce(key: String, needed: Boolean, title: String, msg: String, intent: () -> Intent): Boolean {
        if (!needed) return false
        val p = getSharedPreferences("setup", MODE_PRIVATE)
        if (p.getBoolean(key, false)) return false
        p.edit().putBoolean(key, true).apply()
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton("Ayarları Aç") { _, _ ->
                try {
                    startActivity(intent())
                } catch (_: Exception) {
                }
            }
            .setNegativeButton("Sonra", null)
            .show()
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        checkSetup()
    }

    // ---------- Not düzenleyici ----------

    private fun circle(color: Int, selected: Boolean) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
        setStroke(if (selected) dp(4) else dp(1), if (selected) Color.BLACK else 0xFF999999.toInt())
    }

    private fun showEditor(existing: Note?) {
        var color = existing?.color ?: PALETTE[0]
        var time = existing?.time ?: 0L

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
        }
        val input = EditText(this).apply {
            hint = "Not / hatırlatma metni"
            setText(existing?.text ?: "")
            minLines = 3
            gravity = Gravity.TOP or Gravity.START
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }
        box.addView(input)

        val swatches = mutableListOf<View>()
        val row = LinearLayout(this)
        fun paint() {
            swatches.forEachIndexed { i, v -> v.background = circle(PALETTE[i], PALETTE[i] == color) }
        }
        PALETTE.forEach { c ->
            val v = View(this)
            val p = LinearLayout.LayoutParams(dp(40), dp(40)).apply { setMargins(dp(4), dp(10), dp(4), dp(10)) }
            v.setOnClickListener {
                color = c
                paint()
            }
            swatches.add(v)
            row.addView(v, p)
        }
        paint()
        box.addView(HorizontalScrollView(this).apply { addView(row) })

        val timeText = TextView(this).apply {
            textSize = 16f
            setPadding(0, dp(8), 0, dp(4))
        }
        fun updateTime() {
            timeText.text = if (time > 0) "⏰ " + fmtTime(time) else "Hatırlatma yok"
        }
        updateTime()
        box.addView(timeText)

        val pick = Button(this).apply {
            text = "Tarih ve saat seç"
            setOnClickListener {
                val cal = Calendar.getInstance().apply { if (time > 0) timeInMillis = time }
                DatePickerDialog(
                    this@MainActivity,
                    { _, y, m, d ->
                        TimePickerDialog(
                            this@MainActivity,
                            { _, h, mi ->
                                val c = Calendar.getInstance()
                                c.set(y, m, d, h, mi, 0)
                                c.set(Calendar.MILLISECOND, 0)
                                time = c.timeInMillis
                                updateTime()
                            },
                            cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true
                        ).show()
                    },
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
        val clear = Button(this).apply {
            text = "Hatırlatmayı kaldır"
            setOnClickListener {
                time = 0L
                updateTime()
            }
        }
        box.addView(pick)
        box.addView(clear)

        val builder = AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Yeni not" else "Notu düzenle")
            .setView(ScrollView(this).apply { addView(box) })
            .setPositiveButton("Kaydet", null)
            .setNegativeButton("İptal", null)
        if (existing != null) {
            builder.setNeutralButton("Sil") { _, _ ->
                NoteStore.delete(this, existing.id)
                Scheduler.reschedule(this)
                refresh()
            }
        }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val text = input.text.toString().trim()
            val unchanged = existing != null && time == existing.time
            when {
                text.isEmpty() -> Toast.makeText(this, "Lütfen bir metin yazın", Toast.LENGTH_SHORT).show()
                !unchanged && time > 0 && time <= System.currentTimeMillis() ->
                    Toast.makeText(this, "Geçmiş bir zaman seçilemez", Toast.LENGTH_SHORT).show()
                else -> {
                    val status = if (unchanged) existing!!.status else 0
                    NoteStore.upsert(
                        this, Note(existing?.id ?: System.currentTimeMillis(), text, color, time, status)
                    )
                    Scheduler.reschedule(this)
                    dialog.dismiss()
                    refresh()
                }
            }
        }
    }
}
