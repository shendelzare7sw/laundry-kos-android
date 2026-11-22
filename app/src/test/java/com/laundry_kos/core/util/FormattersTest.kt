package com.laundry_kos.core.util

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
    fun hitungBiayaUtamaOtomatis_bulatkanKeAtasKeRibuan() {
        assertNull(Formatters.hitungBiayaUtamaOtomatis("", 8000, 200, 1000))
        assertEquals(16000, Formatters.hitungBiayaUtamaOtomatis("1,5", 8000, 200, 1000))
        assertEquals(16000, Formatters.hitungBiayaUtamaOtomatis("2", 8000, 200, 1000))
        assertEquals(16000, Formatters.hitungBiayaUtamaOtomatis("2.0", 8000, 200, 1000))
        assertEquals(17000, Formatters.hitungBiayaUtamaOtomatis("2,1", 8000, 200, 1000))
        assertEquals(18000, Formatters.hitungBiayaUtamaOtomatis("2.2", 8000, 200, 1000))
        assertEquals(19000, Formatters.hitungBiayaUtamaOtomatis("2.3", 8000, 200, 1000))
        assertEquals(20000, Formatters.hitungBiayaUtamaOtomatis("2,4", 8000, 200, 1000))
        assertEquals(20000, Formatters.hitungBiayaUtamaOtomatis("2.5", 8000, 200, 1000))
        assertEquals(24000, Formatters.hitungBiayaUtamaOtomatis("2.9", 8000, 200, 1000))
        assertEquals(24000, Formatters.hitungBiayaUtamaOtomatis("3", 8000, 200, 1000))
        assertEquals(25000, Formatters.hitungBiayaUtamaOtomatis("3.1", 8000, 200, 1000))
    }

    @Test
    fun rupiahVisualTransformation_formatsAndMapsOffsetsCleanly() {
        val transformed = RupiahVisualTransformation.filter(AnnotatedString("20000"))
        assertEquals("20.000", transformed.text.text)
        assertEquals(0, transformed.offsetMapping.originalToTransformed(0))
        assertEquals(1, transformed.offsetMapping.originalToTransformed(1))
        assertEquals(2, transformed.offsetMapping.originalToTransformed(2))
        assertEquals(4, transformed.offsetMapping.originalToTransformed(3))
        assertEquals(5, transformed.offsetMapping.originalToTransformed(4))
        assertEquals(6, transformed.offsetMapping.originalToTransformed(5))

        assertEquals(0, transformed.offsetMapping.transformedToOriginal(0))
        assertEquals(2, transformed.offsetMapping.transformedToOriginal(2))
        assertEquals(2, transformed.offsetMapping.transformedToOriginal(3))
        assertEquals(3, transformed.offsetMapping.transformedToOriginal(4))
        assertEquals(5, transformed.offsetMapping.transformedToOriginal(6))
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
