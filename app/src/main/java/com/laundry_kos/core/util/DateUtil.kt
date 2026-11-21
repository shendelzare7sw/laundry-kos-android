package com.laundry_kos.core.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtil {
    val ZONE_JAKARTA: ZoneId = ZoneId.of("Asia/Jakarta")
    private val LOCALE_ID = Locale("id", "ID")
    private val ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE
    private val YM_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM", Locale.US)
    private val DISPLAY_DATE = DateTimeFormatter.ofPattern("d MMM yyyy", LOCALE_ID)
    private val DISPLAY_MONTH = DateTimeFormatter.ofPattern("MMMM yyyy", LOCALE_ID)

    fun todayIso(): String = LocalDate.now(ZONE_JAKARTA).format(ISO_DATE)

    fun currentYearMonth(): String = YearMonth.now(ZONE_JAKARTA).format(YM_FORMAT)

    fun shiftYearMonth(ym: String, deltaMonths: Long): String {
        val parsed = runCatching { YearMonth.parse(ym, YM_FORMAT) }
            .getOrElse { YearMonth.now(ZONE_JAKARTA) }
        return parsed.plusMonths(deltaMonths).format(YM_FORMAT)
    }

    fun formatMonthLabel(ym: String): String {
        val parsed = runCatching { YearMonth.parse(ym, YM_FORMAT) }.getOrNull() ?: return ym
        return parsed.format(DISPLAY_MONTH).replaceFirstChar { it.titlecase(LOCALE_ID) }
    }

    fun formatIsoDateLabel(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return "-"
        val parsed = runCatching { LocalDate.parse(isoDate.take(10), ISO_DATE) }.getOrNull()
            ?: return isoDate
        return parsed.format(DISPLAY_DATE)
    }
}
