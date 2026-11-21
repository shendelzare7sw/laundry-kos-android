package com.laundry_kos.core.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.laundry_kos.data.dto.LaporanBarisDto
import com.laundry_kos.data.dto.LaporanResponse

/**
 * Menghasilkan dokumen HTML A4 2 kolom persis desain cetak resmi web (`laporan/index.blade.php`):
 * - Header rentang + garis marun tebal
 * - 3 kotak ringkasan saja (Total Laundry, Jumlah Penghuni, Mendekati / Lewat)
 * - Grid kartu 2 kolom bergaris titik-titik + batang berwarna (Biaya utama, Tagihan lewat kuota, Bayar terpisah)
 * - Footer marun total keseluruhan
 */
object LaporanPdfPrinter {

    // Simpan referensi sementara hingga WebView selesai memuat agar tidak ter-GC.
    private var activeWebView: WebView? = null

    fun printLaporan(context: Context, laporan: LaporanResponse) {
        val html = buildPrintHtml(laporan)
        val jobName = "Laporan Laundry - ${laporan.namaProperti} (${laporan.rentang.label})"

        val webView = WebView(context).apply {
            settings.javaScriptEnabled = false
            settings.allowFileAccess = false
        }
        activeWebView = webView

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false

            override fun onPageFinished(view: WebView?, url: String?) {
                val target = view ?: return
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
                val adapter = target.createPrintDocumentAdapter(jobName)
                val attrs = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()
                printManager.print(jobName, adapter, attrs)
                activeWebView = null
            }
        }

        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    fun buildPrintHtml(laporan: LaporanResponse): String {
        val sb = StringBuilder()
        sb.append(
            """
            <!DOCTYPE html>
            <html lang="id">
            <head>
            <meta charset="utf-8" />
            <style>
              @page { size: A4 portrait; margin: 10mm; }
              * { box-sizing: border-box; }
              body {
                font-family: Roboto, Arial, sans-serif;
                font-size: 10.5px;
                color: #1C1C1C;
                background: #FFFFFF;
                margin: 0;
                padding: 0;
                -webkit-print-color-adjust: exact;
                print-color-adjust: exact;
              }
              .header {
                display: flex;
                justify-content: space-between;
                align-items: flex-start;
                gap: 12px;
                border-bottom: 3px solid #8B1A1A;
                padding-bottom: 10px;
                margin-bottom: 12px;
              }
              .prop-name { font-size: 11px; font-weight: 700; color: #1C1C1C; margin: 0; }
              .period { font-size: 18px; font-weight: 800; color: #1C1C1C; margin: 2px 0 3px 0; letter-spacing: -0.2px; }
              .sub-note { font-size: 10px; color: #6B7280; margin: 0; }
              .head-right { text-align: right; }
              .doc-tag { font-size: 10px; font-weight: 800; color: #C8920E; letter-spacing: 0.6px; text-transform: uppercase; margin: 0; }
              .head-meta { font-size: 9.5px; color: #6B7280; margin: 2px 0 0 0; }

              .stats {
                display: grid;
                grid-template-columns: repeat(3, 1fr);
                gap: 10px;
                margin-bottom: 14px;
              }
              .stat-card {
                border: 1px solid #ECE5DB;
                border-radius: 12px;
                padding: 10px 12px;
                background: #FFFFFF;
              }
              .stat-label { font-size: 9px; font-weight: 700; color: #6B7280; text-transform: uppercase; letter-spacing: 0.4px; }
              .stat-val { font-size: 16px; font-weight: 800; margin-top: 4px; color: #C8920E; }
              .stat-val.danger { color: #DC2626; }

              .section-title {
                font-size: 9.5px;
                font-weight: 800;
                letter-spacing: 0.6px;
                color: #6B7280;
                text-transform: uppercase;
                margin: 0 0 8px 0;
              }

              .grid {
                display: grid;
                grid-template-columns: repeat(2, 1fr);
                gap: 10px;
                align-items: start;
              }
              .card {
                border: 1px solid #ECE5DB;
                border-radius: 14px;
                padding: 10px;
                break-inside: avoid;
                page-break-inside: avoid;
                background: #FFFFFF;
              }
              .card-top {
                display: flex;
                justify-content: space-between;
                align-items: flex-start;
                gap: 8px;
              }
              .card-left { display: flex; align-items: flex-start; gap: 8px; min-width: 0; flex: 1; }
              .room-pill {
                background: #F6E6E6;
                color: #8B1A1A;
                font-weight: 800;
                font-size: 10.5px;
                border-radius: 999px;
                padding: 5px 10px;
                white-space: nowrap;
                flex-shrink: 0;
              }
              .resident-name { font-size: 12px; font-weight: 800; color: #1C1C1C; margin: 0; }
              .resident-meta { font-size: 9.5px; color: #6B7280; margin: 2px 0 0 0; }
              .quota-line { font-size: 9.5px; font-weight: 600; color: #374151; margin: 3px 0 0 0; line-height: 1.35; }
              .total-kg { font-size: 13.5px; font-weight: 800; color: #8B1A1A; white-space: nowrap; }

              .badge {
                display: inline-block;
                margin-top: 5px;
                border-radius: 999px;
                padding: 2px 8px;
                font-size: 9px;
                font-weight: 700;
              }
              .badge-normal { background: #DCFCE7; color: #15803D; }
              .badge-mendekati { background: #FBF1D8; color: #A16207; }
              .badge-lewat { background: #FEE2E2; color: #DC2626; }

              .entries-box {
                margin-top: 8px;
                background: #FAF7F2;
                border-radius: 10px;
                padding: 8px 10px;
              }
              .entry-row {
                display: flex;
                align-items: baseline;
                margin-bottom: 4px;
                font-size: 9.5px;
              }
              .entry-row:last-child { margin-bottom: 0; }
              .entry-left { color: #6B7280; white-space: nowrap; }
              .entry-dots {
                flex: 1;
                border-bottom: 1px dotted #D6CFC3;
                margin: 0 6px;
                min-width: 12px;
              }
              .entry-right { font-weight: 700; color: #1C1C1C; white-space: nowrap; }
              .entry-row.merah .entry-right { color: #DC2626; }
              .entry-note { font-size: 8.5px; color: #6B7280; font-style: italic; margin: -2px 0 4px 8px; }

              .subtotal-row {
                display: flex;
                align-items: baseline;
                margin-top: 6px;
                padding-top: 6px;
                border-top: 1px solid #ECE5DB;
                font-size: 10px;
                font-weight: 800;
                color: #1C1C1C;
              }

              .bars { margin-top: 7px; display: flex; flex-direction: column; gap: 5px; }
              .bar {
                display: flex;
                justify-content: space-between;
                align-items: center;
                border-radius: 9px;
                padding: 5px 10px;
                font-size: 9.5px;
                font-weight: 700;
              }
              .bar-ok { background: #E9F7EF; color: #15803D; }
              .bar-danger { background: #FDECEC; color: #DC2626; }
              .bar-gold { background: #FBF1D8; color: #6B4E08; }

              .footer {
                margin-top: 14px;
                border-radius: 12px;
                overflow: hidden;
                break-inside: avoid;
                page-break-inside: avoid;
              }
              .footer-top {
                background: #8B1A1A;
                color: #FFFFFF;
                padding: 11px 14px;
                display: flex;
                justify-content: space-between;
                align-items: center;
              }
              .footer-title { font-size: 12px; font-weight: 800; margin: 0; }
              .footer-sub { font-size: 9.5px; color: rgba(255,255,255,0.88); margin: 2px 0 0 0; }
              .footer-kg { font-size: 18px; font-weight: 800; margin: 0; }
              .footer-note {
                background: #F1EBE2;
                color: #6B7280;
                font-size: 9px;
                text-align: center;
                padding: 6px 10px;
              }
            </style>
            </head>
            <body>
            """.trimIndent()
        )

        // Header
        sb.append("<div class='header'>")
        sb.append("<div>")
        sb.append("<p class='prop-name'>").append(escape(laporan.namaProperti)).append("</p>")
        sb.append("<h1 class='period'>").append(escape(laporan.rentang.label)).append("</h1>")
        sb.append("<p class='sub-note'>Kuota gratis mengikuti periode laporan.</p>")
        sb.append("</div>")
        sb.append("<div class='head-right'>")
        sb.append("<p class='doc-tag'>LAPORAN LAUNDRY</p>")
        sb.append("<p class='head-meta'>Dicetak ").append(escape(laporan.dicetakLabel)).append("</p>")
        sb.append("<p class='head-meta'>Kuota gratis ").append(escape(laporan.kuotaGratisLabel)).append(" kg / penghuni</p>")
        sb.append("</div>")
        sb.append("</div>")

        // 3 Kotak Ringkasan (persis gambar 7)
        val r = laporan.ringkasan
        sb.append("<div class='stats'>")
        sb.append("<div class='stat-card'><div class='stat-label'>TOTAL LAUNDRY</div><div class='stat-val'>")
            .append(escape(r.totalKgLabel)).append(" kg</div></div>")
        sb.append("<div class='stat-card'><div class='stat-label'>JUMLAH PENGHUNI</div><div class='stat-val'>")
            .append(r.jumlahPenghuni).append("</div></div>")
        sb.append("<div class='stat-card'><div class='stat-label'>MENDEKATI / LEWAT</div><div class='stat-val danger'>")
            .append(r.jumlahMendekatiLewat).append("</div></div>")
        sb.append("</div>")

        sb.append("<h2 class='section-title'>RINCIAN PER PENGHUNI</h2>")
        sb.append("<div class='grid'>")

        for (kartu in laporan.kartuPenghuni) {
            val kuota = kartu.kuota
            val statusClass = when (kuota.status) {
                "lewat" -> "badge-lewat"
                "mendekati" -> "badge-mendekati"
                else -> "badge-normal"
            }
            val statusLabel = when (kuota.status) {
                "lewat" -> "Lewat kuota"
                "mendekati" -> "Mendekati kuota"
                else -> "Normal"
            }
            val terpakaiLbl = kuota.terpakaiLabel ?: kuota.terpakaiKg
            val kuotaLbl = kuota.kuotaLabel ?: kuota.kuotaKg
            val lebihLbl = kuota.kelebihanLabel ?: kuota.kelebihanKg

            sb.append("<div class='card'>")
            sb.append("<div class='card-top'>")
            sb.append("<div class='card-left'>")
            sb.append("<span class='room-pill'>").append(escape(kartu.labelKamar)).append("</span>")
            sb.append("<div>")
            sb.append("<p class='resident-name'>").append(escape(kartu.penghuni.nama)).append("</p>")
            sb.append("<p class='resident-meta'>").append(kartu.jumlahCatatan).append(" catatan - Penghuni</p>")
            sb.append("<p class='quota-line'>Kuota: ").append(escape(terpakaiLbl)).append(" / ")
                .append(escape(kuotaLbl)).append(" kg kuota bulan ini")
            if (kartu.tampilkanPeriodeIni) {
                sb.append(" - Periode ini ").append(escape(kartu.totalKgLabel)).append(" kg")
            }
            if (kuota.kelebihanCenti > 0) {
                sb.append(" - Lewat bulan ini ").append(escape(lebihLbl)).append(" kg")
            }
            sb.append("</p>")
            sb.append("<span class='badge ").append(statusClass).append("'>").append(statusLabel).append("</span>")
            sb.append("</div></div>")
            sb.append("<span class='total-kg'>").append(escape(kartu.totalKgLabel)).append(" kg</span>")
            sb.append("</div>")

            appendDottedEntries(sb, kartu.baris, kartu.totalKgLabel, laporan.filter.rinci)

            sb.append("<div class='bars'>")
            sb.append("<div class='bar bar-ok'><span>Biaya utama</span><span>")
                .append(escape(Formatters.formatRupiah(kartu.biayaUtama))).append("</span></div>")
            if (kartu.tagihanLewatKuota > 0) {
                sb.append("<div class='bar bar-danger'><span>Tagihan lewat kuota ")
                    .append(escape(lebihLbl)).append(" kg</span><span>")
                    .append(escape(Formatters.formatRupiah(kartu.tagihanLewatKuota))).append("</span></div>")
            }
            if (kartu.bayarTerpisah > 0) {
                sb.append("<div class='bar bar-gold'><span>Bayar terpisah ")
                    .append(kartu.jumlahItemTerpisah).append(" item</span><span>")
                    .append(escape(Formatters.formatRupiah(kartu.bayarTerpisah))).append("</span></div>")
            }
            sb.append("</div>")
            sb.append("</div>")
        }

        for (lain in laporan.kartuLainnya) {
            sb.append("<div class='card'>")
            sb.append("<div class='card-top'>")
            sb.append("<div><p class='resident-name'>").append(escape(lain.judul)).append("</p>")
            sb.append("<p class='resident-meta'>").append(lain.jumlahCatatan).append(" catatan</p></div>")
            sb.append("<span class='total-kg'>").append(escape(lain.totalKgLabel)).append(" kg</span>")
            sb.append("</div>")

            appendDottedEntries(sb, lain.baris, lain.totalKgLabel, laporan.filter.rinci)

            sb.append("<div class='bars'>")
            sb.append("<div class='bar bar-ok'><span>Biaya utama</span><span>")
                .append(escape(Formatters.formatRupiah(lain.biayaUtama))).append("</span></div>")
            if (lain.bayarTerpisah > 0) {
                sb.append("<div class='bar bar-gold'><span>Bayar terpisah ")
                    .append(lain.jumlahItemTerpisah).append(" item</span><span>")
                    .append(escape(Formatters.formatRupiah(lain.bayarTerpisah))).append("</span></div>")
            }
            sb.append("</div></div>")
        }

        sb.append("</div>")

        // Footer Marun Total Keseluruhan
        sb.append("<div class='footer'>")
        sb.append("<div class='footer-top'>")
        sb.append("<div><p class='footer-title'>Total keseluruhan laundry</p>")
        sb.append("<p class='footer-sub'>Tagihan lewat kuota ").append(escape(Formatters.formatRupiah(r.totalTagihanLewat)))
            .append(" &middot; Biaya utama ").append(escape(Formatters.formatRupiah(r.totalBiayaUtama)))
            .append(" &middot; Bayar terpisah ").append(escape(Formatters.formatRupiah(r.totalBayarTerpisah)))
            .append("</p></div>")
        sb.append("<p class='footer-kg'>").append(escape(r.totalKgLabel)).append(" kg</p>")
        sb.append("</div>")
        sb.append("<div class='footer-note'>Entri mengikuti filter tanggal. Kuota gratis dihitung dari total bulan terkait.</div>")
        sb.append("</div>")

        sb.append("</body></html>")
        return sb.toString()
    }

    private fun appendDottedEntries(
        sb: StringBuilder,
        baris: List<LaporanBarisDto>,
        subtotalKgLabel: String,
        rinci: Boolean,
    ) {
        sb.append("<div class='entries-box'>")
        for (row in baris) {
            val rowClass = if (row.merah) "entry-row merah" else "entry-row"
            val left = if (row.tipe == "item") {
                "${row.tanggalLabel} - ${row.namaItem ?: "Item"}"
            } else {
                row.tanggalLabel
            }
            val right = if (row.tipe == "item") {
                "1 item - ${Formatters.formatRupiah(row.biaya, "Rp0")}"
            } else {
                val beratPart = "${row.beratLabel ?: "-"} kg"
                if (row.biaya != null) "$beratPart - ${Formatters.formatRupiah(row.biaya)}" else beratPart
            }
            sb.append("<div class='").append(rowClass).append("'>")
            sb.append("<span class='entry-left'>").append(escape(left)).append("</span>")
            sb.append("<span class='entry-dots'></span>")
            sb.append("<span class='entry-right'>").append(escape(right)).append("</span>")
            sb.append("</div>")
            if (rinci && !row.catatan.isNullOrBlank()) {
                sb.append("<div class='entry-note'>Catatan: ").append(escape(row.catatan)).append("</div>")
            }
        }
        sb.append("<div class='subtotal-row'>")
        sb.append("<span>Subtotal semua</span>")
        sb.append("<span class='entry-dots'></span>")
        sb.append("<span>").append(escape(subtotalKgLabel)).append(" kg</span>")
        sb.append("</div></div>")
    }

    private fun escape(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")
}
