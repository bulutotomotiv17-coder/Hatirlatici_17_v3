package com.hatirlatici.app

import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter(private val onClick: (Note) -> Unit) : RecyclerView.Adapter<NoteAdapter.VH>() {

    private var items: List<Note> = emptyList()

    fun submit(list: List<Note>) {
        items = list
        notifyDataSetChanged()
    }

    class VH(
        val wrap: FrameLayout, val card: LinearLayout,
        val text: TextView, val info: TextView
    ) : RecyclerView.ViewHolder(wrap)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val ctx = parent.context
        val wrap = FrameLayout(ctx).apply {
            setPadding(dp(6), dp(6), dp(6), dp(6))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val card = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            elevation = dp(2).toFloat()
        }
        val text = TextView(ctx).apply { textSize = 17f }
        val info = TextView(ctx).apply {
            textSize = 12f
            setPadding(0, dp(10), 0, 0)
        }
        card.addView(text)
        card.addView(info)
        wrap.addView(
            card,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )
        return VH(wrap, card, text, info)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val n = items[position]
        val fg = textOn(n.color)
        h.card.background = rounded(n.color, h.wrap.dp(16))
        h.text.text = n.text
        h.text.setTextColor(fg)
        h.info.setTextColor(fg)
        val info = when {
            n.status == 1 -> "🔔 Zamanı geldi · " + fmtTime(n.time)
            n.status == 2 -> "✓ Tamamlandı · " + fmtTime(n.time)
            n.time > 0 -> "⏰ " + fmtTime(n.time)
            else -> ""
        }
        h.info.text = info
        h.info.visibility = if (info.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
        h.card.alpha = if (n.status == 2) 0.55f else 1f
        h.wrap.setOnClickListener { onClick(n) }
    }

    override fun getItemCount() = items.size
}
