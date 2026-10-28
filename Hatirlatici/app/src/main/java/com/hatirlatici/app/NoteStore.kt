package com.hatirlatici.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** status: 0 = bekliyor (veya hatırlatmasız not), 1 = zamanı geldi, 2 = tamamlandı */
data class Note(
    val id: Long,
    var text: String,
    var color: Int,
    var time: Long,
    var status: Int
)

object NoteStore {
    private const val PREF = "notes"
    private const val KEY = "list"

    @Synchronized
    fun all(ctx: Context): MutableList<Note> {
        val raw = ctx.applicationContext.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY, "[]") ?: "[]"
        val arr = JSONArray(raw)
        val out = mutableListOf<Note>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                Note(
                    o.getLong("id"), o.getString("text"), o.getInt("color"),
                    o.getLong("time"), o.getInt("status")
                )
            )
        }
        return out
    }

    @Synchronized
    private fun write(ctx: Context, list: List<Note>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id).put("text", it.text).put("color", it.color)
                    .put("time", it.time).put("status", it.status)
            )
        }
        ctx.applicationContext.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY, arr.toString()).apply()
    }

    @Synchronized
    fun upsert(ctx: Context, n: Note) {
        val l = all(ctx)
        val i = l.indexOfFirst { it.id == n.id }
        if (i >= 0) l[i] = n else l.add(n)
        write(ctx, l)
    }

    @Synchronized
    fun delete(ctx: Context, id: Long) {
        write(ctx, all(ctx).filter { it.id != id })
    }

    @Synchronized
    fun update(ctx: Context, id: Long, block: (Note) -> Unit) {
        val l = all(ctx)
        l.firstOrNull { it.id == id }?.let(block)
        write(ctx, l)
    }

    /** Zamanı gelmiş bekleyen notları "zamanı geldi" yapar. Yeni gelen sayısını döndürür. */
    @Synchronized
    fun markDue(ctx: Context): Int {
        val now = System.currentTimeMillis()
        val l = all(ctx)
        var c = 0
        l.forEach {
            if (it.status == 0 && it.time in 1..now) {
                it.status = 1
                c++
            }
        }
        if (c > 0) write(ctx, l)
        return c
    }

    fun dueNotes(ctx: Context): List<Note> = all(ctx).filter { it.status == 1 }
}
