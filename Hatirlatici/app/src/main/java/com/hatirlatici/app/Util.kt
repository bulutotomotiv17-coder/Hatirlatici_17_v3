package com.hatirlatici.app

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.core.graphics.ColorUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val PALETTE = intArrayOf(
    0xFFEF5350.toInt(), // kırmızı
    0xFFFF9800.toInt(), // turuncu
    0xFFFFEB3B.toInt(), // sarı
    0xFF9CCC65.toInt(), // yeşil
    0xFF26A69A.toInt(), // turkuaz
    0xFF42A5F5.toInt(), // mavi
    0xFF7E57C2.toInt(), // mor
    0xFFEC407A.toInt(), // pembe
    0xFF8D6E63.toInt(), // kahve
    0xFF78909C.toInt()  // gri
)

fun Context.dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
fun View.dp(v: Int): Int = context.dp(v)

fun textOn(bg: Int): Int =
    if (ColorUtils.calculateLuminance(bg) > 0.5) Color.BLACK else Color.WHITE

fun rounded(color: Int, radiusPx: Int): GradientDrawable =
    GradientDrawable().apply {
        setColor(color)
        cornerRadius = radiusPx.toFloat()
    }

fun fmtTime(t: Long): String =
    SimpleDateFormat("d MMM yyyy, HH:mm", Locale("tr", "TR")).format(Date(t))
