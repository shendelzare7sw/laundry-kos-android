package com.laundry_kos.core.util

import kotlin.math.abs

/**
 * Formatter domain Laundry-Kos berbasis bilangan bulat (Int) tanpa aritmetika float/double
 * agar selalu 100% sinkron dengan kontrak REST API v1.
 */
object Formatters {

    fun formatRupiah(nominal: Int?, nullPlaceholder: String = "-"): String {
        if (nominal == null) return nullPlaceholder
        val negatif = nominal < 0
        val digits = abs(nominal.toLong()).toString()
        val formatted = buildString {
            val len = digits.length
            for (i in digits.indices) {
                append(digits[i])
                val sisa = len - 1 - i
                if (sisa > 0 && sisa % 3 == 0) {
                    append('.')
                }
            }
        }
        return if (negatif) "-Rp$formatted" else "Rp$formatted"
    }

    /**
     * Prioritaskan `beratLabel` dari server jika tersedia; fallback konversi integer dari `beratCenti`
     * (misal 2370 -> "23,7", 2000 -> "20", 2375 -> "23,75").
     */
    fun formatBeratKg(
        beratCenti: Int?,
        beratLabel: String? = null,
        denganSatuan: Boolean = true,
        nullPlaceholder: String = "-",
    ): String {
        val label = beratLabel?.trim()?.takeIf { it.isNotEmpty() }
            ?: beratCenti?.let { centiKeLabel(it) }
            ?: return nullPlaceholder
        return if (denganSatuan) "$label kg" else label
    }

    fun centiKeLabel(centi: Int): String {
        val negatif = centi < 0
        val nilaiAbs = abs(centi)
        val utuh = nilaiAbs / 100
        val pecahan = nilaiAbs % 100
        val teks = when {
            pecahan == 0 -> utuh.toString()
            pecahan % 10 == 0 -> "$utuh,${pecahan / 10}"
            else -> "$utuh,${pecahan.toString().padStart(2, '0')}"
        }
        return if (negatif) "-$teks" else teks
    }
}
