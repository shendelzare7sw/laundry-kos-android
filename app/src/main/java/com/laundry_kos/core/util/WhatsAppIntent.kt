package com.laundry_kos.core.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object WhatsAppIntent {

    fun openUrlOrCopy(
        context: Context,
        url: String?,
        fallbackText: String? = null,
        label: String = "Pesan WhatsApp",
    ) {
        if (!url.isNullOrBlank()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val opened = runCatching {
                context.startActivity(intent)
                true
            }.getOrDefault(false)

            if (opened) return
        }

        if (!fallbackText.isNullOrBlank()) {
            copyToClipboard(context, fallbackText, label)
            Toast.makeText(
                context,
                "Teks $label disalin ke clipboard.",
                Toast.LENGTH_SHORT,
            ).show()
        } else {
            Toast.makeText(
                context,
                "Nomor WhatsApp belum tersedia atau aplikasi tidak ditemukan.",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    fun sharePlainText(context: Context, text: String, subject: String = "Rekap Laundry-Kos") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(Intent.createChooser(intent, subject))
        }.onFailure {
            copyToClipboard(context, text, subject)
            Toast.makeText(context, "Teks disalin ke clipboard.", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Laundry-Kos") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    }
}
