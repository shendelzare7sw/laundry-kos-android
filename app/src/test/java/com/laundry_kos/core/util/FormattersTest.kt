package com.laundry_kos.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun formatRupiah_formatsIntegersWithoutFloatRounding() {
        assertEquals("-", Formatters.formatRupiah(null))
        assertEquals("Rp0", Formatters.formatRupiah(0))
        assertEquals("Rp8.000", Formatters.formatRupiah(8000))
        assertEquals("Rp24.000", Formatters.formatRupiah(24000))
        assertEquals("Rp1.250.500", Formatters.formatRupiah(1_250_500))
        assertEquals("-Rp15.000", Formatters.formatRupiah(-15000))
    }

    @Test
    fun formatBeratKg_usesServerLabelOrIntegerCentiConversion() {
        assertEquals("23,7 kg", Formatters.formatBeratKg(beratCenti = 2370, beratLabel = "23,7"))
        assertEquals("23,7", Formatters.formatBeratKg(beratCenti = 2370, beratLabel = null, denganSatuan = false))
        assertEquals("20 kg", Formatters.formatBeratKg(beratCenti = 2000, beratLabel = null))
        assertEquals("23,75 kg", Formatters.formatBeratKg(beratCenti = 2375, beratLabel = null))
        assertEquals("2,05 kg", Formatters.formatBeratKg(beratCenti = 205, beratLabel = null))
        assertEquals("-", Formatters.formatBeratKg(beratCenti = null, beratLabel = null))
    }
}
