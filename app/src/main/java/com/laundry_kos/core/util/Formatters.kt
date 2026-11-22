package com.laundry_kos.core.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import kotlin.math.abs

/**
 * Formatter domain Laundry-Kos berbasis bilangan bulat (Int) tanpa aritmetika float/double
 * agar selalu 100% sinkron dengan kontrak REST API v1.
 */
object Formatters {

    fun formatRupiah(nominal: Int?, nullPlaceholder: String = "-"): String {
        if (nominal == null) return nullPlaceholder
        val negatif = nominal < 0
        val formatted = formatRibuan(abs(nominal.toLong()).toString())
        return if (negatif) "-Rp$formatted" else "Rp$formatted"
    }

    /**
     * Memformat string digit murni menjadi pemisah ribuan titik Indonesia
     * (contoh: "20000" -> "20.000", "1250000" -> "1.250.000", "" -> "").
     */
    fun formatRibuan(rawDigits: String): String {
        val digits = rawDigits.filter(Char::isDigit)
        if (digits.isEmpty()) return ""
        val len = digits.length
        return buildString(len + len / 3) {
            for (i in digits.indices) {
                append(digits[i])
                val sisa = len - 1 - i
                if (sisa > 0 && sisa % 3 == 0) {
                    append('.')
                }
            }
        }
    }

    /**
     * Pembulatan nominal rupiah KE ATAS ke kelipatan terdekat (default Rp1.000).
     * Contoh: 16.000 -> 16.000; 16.800 (2,1 kg) -> 17.000; 23.200 (2,9 kg) -> 24.000.
     */
    fun bulatkanRupiahKeAtas(nominal: Int, kelipatan: Int = 1000): Int {
        if (nominal <= 0) return 0
        if (kelipatan <= 1) return nominal
        return ((nominal.toLong() + kelipatan.toLong() - 1L) / kelipatan.toLong() * kelipatan.toLong()).toInt()
    }

    fun parseBeratKeCenti(beratText: String): Int? {
        val cleaned = beratText.replace(',', '.').trim()
        if (cleaned.isEmpty()) return null
        val match = BERAT_REGEX.matchEntire(cleaned) ?: return null
        val bulat = match.groupValues[1].toIntOrNull() ?: return null
        val fracPadded = match.groupValues[2].padEnd(3, '0')
        val duaDigit = fracPadded.substring(0, 2).toInt()
        val digitKetiga = fracPadded[2].digitToInt()
        var total = bulat * 100 + duaDigit
        if (digitKetiga >= 5) total += 1
        return total
    }

    fun hitungBiayaUtamaOtomatis(
        beratText: String,
        tarifPerKg: Int,
        minKgCenti: Int = 200,
        pembulatan: Int = 1000,
    ): Int? {
        val beratCenti = parseBeratKeCenti(beratText) ?: return null
        if (beratCenti <= 0) return null
        val beratDihitung = maxOf(beratCenti, minKgCenti)
        val nominalMentah = ((beratDihitung.toLong() * tarifPerKg.toLong()) + 50L) / 100L
        return bulatkanRupiahKeAtas(nominalMentah.toInt(), pembulatan)
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

    private val BERAT_REGEX = Regex("^(\\d+)(?:\\.(\\d+))?$")
}

/**
 * VisualTransformation untuk field nominal Rupiah pada Jetpack Compose:
 * State menyimpan digit murni ("20000"), sedangkan UI menampilkan pemisah ribuan ("20.000")
 * dengan pemetaan kursor yang presisi saat mengetik, menghapus, atau menempel teks.
 */
object RupiahVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        if (original.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }
        val formatted = Formatters.formatRibuan(original)
        val origLen = original.length
        val transLen = formatted.length

        val origToTrans = IntArray(origLen + 1)
        var seenDigits = 0
        origToTrans[0] = 0
        for (i in formatted.indices) {
            if (formatted[i] != '.') {
                seenDigits++
                if (seenDigits <= origLen) {
                    origToTrans[seenDigits] = i + 1
                }
            }
        }
        while (seenDigits < origLen) {
            seenDigits++
            origToTrans[seenDigits] = transLen
        }

        val transToOrig = IntArray(transLen + 1)
        var digitsBefore = 0
        transToOrig[0] = 0
        for (i in formatted.indices) {
            if (formatted[i] != '.') {
                digitsBefore++
            }
            transToOrig[i + 1] = digitsBefore.coerceAtMost(origLen)
        }

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                origToTrans[offset.coerceIn(0, origLen)]

            override fun transformedToOriginal(offset: Int): Int =
                transToOrig[offset.coerceIn(0, transLen)]
        }

        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
