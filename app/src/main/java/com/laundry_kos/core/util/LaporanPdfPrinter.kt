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
 * Menghasilkan dokumen HTML A4 2 kolom sesuai gaya cetak `app.css` lalu mencetak / menyimpan PDF
 * memakai Android PrintManager bawaan.
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
              }
              .header {
                border-bottom: 2px solid #8B1A1A;
                padding-bottom: 8px;
                margin-bottom: 10px;
                display: flex;
                justify-content: space-between;
                align-items: flex-end;
              }
              .title { font-size: 16px; font-weight: 700; color: #8B1A1A; margin: 0; }
              .subtitle { font-size: 11px; color: #6B7280; margin-top: 2px; }
              .meta { text-align: right; font-size: 10px; color: #6B7280; }
              .summary {
                display: grid;
                grid-template-columns: repeat(4, 1fr);
                gap: 6px;
                margin-bottom: 12px;
              }
              .sum-box {
                border: 1px solid #ECE5DB;
                background: #FCFAF6;
                border-radius: 6px;
                padding: 6px 8px;
              }
              .sum-label { font-size: 9px; color: #6B7280; }
              .sum-val { font-size: 12px; font-weight: 700; color: #1C1C1C; margin-top: 2px; }
              .sum-val.danger { color: #DC2626; }
              .grid {
                display: grid;
                grid-template-columns: repeat(2, 1fr);
                gap: 8px;
                align-items: start;
              }
              .card {
                border: 1px solid #E0D7C9;
                border-radius: 8px;
                padding: 8px;
                break-inside: avoid;
                page-break-inside: avoid;
                background: #FFFFFF;
              }
              .card-head {
                display: flex;
                justify-content: space-between;
                align-items: baseline;
                border-bottom: 1px solid #ECE5DB;
                padding-bottom: 4px;
                margin-bottom: 6px;
              }
              .card-name { font-weight: 700; font-size: 11.5px; color: #8B1A1A; }
              .card-kamar {
                background: #FBF1D8;
                color: #6B4E08;
                border-radius: 999px;
                padding: 1px 6px;
                font-size: 9.5px;
                font-weight: 700;
              }
              table { width: 100%; border-collapse: collapse; margin-bottom: 6px; }
              th, td { padding: 2px 3px; text-align: left; border-bottom: 1px solid #F1EBE2; font-size: 9.5px; }
              th { color: #6B7280; font-weight: 600; }
              td.num, th.num { text-align: right; }
              tr.merah td { color: #DC2626; font-weight: 700; background: #FEF2F2; }
              .card-foot {
                background: #FCFAF6;
                border-radius: 5px;
                padding: 5px 6px;
                font-size: 9.5px;
              }
              .foot-row { display: flex; justify-content: space-between; margin-top: 2px; }
              .foot-row.bold { font-weight: 700; color: #1C1C1C; }
              .foot-row.danger { font-weight: 700; color: #DC2626; }
            </style>
            </head>
            <body>
            """.trimIndent()
        )

        sb.append("<div class='header'>")
        sb.append("<div><div class='title'>").append(escape(laporan.namaProperti)).append("</div>")
        sb.append("<div class='subtitle'>Laporan Laundry &bull; ").append(escape(laporan.rentang.label)).append("</div></div>")
        sb.append("<div class='meta'>Kuota gratis: ").append(escape(laporan.kuotaGratisLabel))
            .append(" kg<br/>Dicetak: ").append(escape(laporan.dicetakLabel)).append("</div>")
        sb.append("</div>")

        val r = laporan.ringkasan
        sb.append("<div class='summary'>")
        sb.append("<div class='sum-box'><div class='sum-label'>Total Berat</div><div class='sum-val'>")
            .append(escape(r.totalKgLabel)).append(" kg (").append(r.jumlahPenghuni).append(" penghuni)</div></div>")
        sb.append("<div class='sum-box'><div class='sum-label'>Biaya Utama</div><div class='sum-val'>")
            .append(escape(Formatters.formatRupiah(r.totalBiayaUtama))).append("</div></div>")
        sb.append("<div class='sum-box'><div class='sum-label'>Tagihan Lewat Kuota</div><div class='sum-val danger'>")
            .append(escape(Formatters.formatRupiah(r.totalTagihanLewat))).append("</div></div>")
        sb.append("<div class='sum-box'><div class='sum-label'>Bayar Terpisah</div><div class='sum-val'>")
            .append(escape(Formatters.formatRupiah(r.totalBayarTerpisah))).append("</div></div>")
        sb.append("</div>")

        sb.append("<div class='grid'>")
        for (kartu in laporan.kartuPenghuni) {
            sb.append("<div class='card'>")
            sb.append("<div class='card-head'>")
            sb.append("<span class='card-name'>").append(escape(kartu.penghuni.nama)).append("</span>")
            sb.append("<span class='card-kamar'>Kamar ").append(escape(kartu.labelKamar)).append("</span>")
            sb.append("</div>")

            appendBarisTable(sb, kartu.baris)

            sb.append("<div class='card-foot'>")
            sb.append("<div class='foot-row'><span>Total berat / Kuota</span><span>")
                .append(escape(kartu.kuota.terpakaiLabel ?: kartu.kuota.terpakaiKg)).append(" / ")
                .append(escape(kartu.kuota.kuotaLabel ?: kartu.kuota.kuotaKg)).append(" kg</span></div>")
            sb.append("<div class='foot-row'><span>Biaya utama</span><span>")
                .append(escape(Formatters.formatRupiah(kartu.biayaUtama))).append("</span></div>")
            if (kartu.tagihanLewatKuota > 0) {
                sb.append("<div class='foot-row danger'><span>Tagihan lewat kuota</span><span>")
                    .append(escape(Formatters.formatRupiah(kartu.tagihanLewatKuota))).append("</span></div>")
            }
            if (kartu.bayarTerpisah > 0) {
                sb.append("<div class='foot-row bold'><span>Bayar terpisah (")
                    .append(kartu.jumlahItemTerpisah).append(" item)</span><span>")
                    .append(escape(Formatters.formatRupiah(kartu.bayarTerpisah))).append("</span></div>")
            }
            sb.append("</div></div>")
        }

        for (lain in laporan.kartuLainnya) {
            sb.append("<div class='card'>")
            sb.append("<div class='card-head'>")
            sb.append("<span class='card-name'>").append(escape(lain.judul)).append("</span>")
            sb.append("<span class='card-kamar'>").append(escape(lain.totalKgLabel)).append(" kg</span>")
            sb.append("</div>")

            appendBarisTable(sb, lain.baris)

            sb.append("<div class='card-foot'>")
            sb.append("<div class='foot-row bold'><span>Biaya utama</span><span>")
                .append(escape(Formatters.formatRupiah(lain.biayaUtama))).append("</span></div>")
            if (lain.bayarTerpisah > 0) {
                sb.append("<div class='foot-row'><span>Bayar terpisah</span><span>")
                    .append(escape(Formatters.formatRupiah(lain.bayarTerpisah))).append("</span></div>")
            }
            sb.append("</div></div>")
        }

        sb.append("</div></body></html>")
        return sb.toString()
    }

    private fun appendBarisTable(sb: StringBuilder, baris: List<LaporanBarisDto>) {
        if (baris.isEmpty()) return
        sb.append("<table><thead><tr><th>Tanggal</th><th>Uraian</th><th class='num'>Biaya</th></tr></thead><tbody>")
        for (row in baris) {
            val trClass = if (row.merah) " class='merah'" else ""
            sb.append("<tr").append(trClass).append(">")
            sb.append("<td>").append(escape(row.tanggalLabel)).append("</td>")
            if (row.tipe == "item") {
                sb.append("<td>Item: ").append(escape(row.namaItem ?: "-")).append("</td>")
            } else {
                val label = (row.beratLabel ?: "-") + " kg" + if (row.merah) " *" else ""
                sb.append("<td>").append(escape(label)).append("</td>")
            }
            sb.append("<td class='num'>").append(escape(Formatters.formatRupiah(row.biaya, "Belum diisi"))).append("</td>")
            sb.append("</tr>")
        }
        sb.append("</tbody></table>")
    }

    private fun escape(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")
}
