package com.bharataiscanner.app

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

data class DocumentRecord(
    val uri: String,
    val name: String,
    val folder: String = "General",
    val createdAt: Long = System.currentTimeMillis()
)

object DocumentManager {
    private const val PREF = "bharat_documents"
    private const val KEY = "records"

    fun load(context: Context): List<DocumentRecord> = runCatching {
        val raw = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY, "[]") ?: "[]"
        val a = JSONArray(raw)
        List(a.length()) { i ->
            val o = a.getJSONObject(i)
            DocumentRecord(o.getString("uri"), o.getString("name"), o.optString("folder", "General"), o.optLong("createdAt"))
        }
    }.getOrDefault(emptyList())

    fun save(context: Context, records: List<DocumentRecord>) {
        val a = JSONArray()
        records.forEach { d ->
            a.put(JSONObject().apply {
                put("uri", d.uri); put("name", d.name); put("folder", d.folder); put("createdAt", d.createdAt)
            })
        }
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(KEY, a.toString()).apply()
    }

    fun add(context: Context, uri: Uri, name: String = "दस्तावेज़ ${System.currentTimeMillis()}", folder: String = "General") {
        val old = load(context).filterNot { it.uri == uri.toString() }
        save(context, listOf(DocumentRecord(uri.toString(), name, folder)) + old.take(99))
    }
}
