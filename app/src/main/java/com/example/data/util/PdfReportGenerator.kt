package com.example.data.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.MonthlyReportData
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard width in points
    private const val PAGE_HEIGHT = 842 // A4 standard height in points
    private const val MARGIN_LEFT = 36f
    private const val MARGIN_RIGHT = 559f
    private const val CONTENT_WIDTH = MARGIN_RIGHT - MARGIN_LEFT // 523f

    fun generatePdf(
        context: Context,
        report: MonthlyReportData,
        transactions: List<TransactionEntity>
    ): File {
        val pdfDocument = PdfDocument()

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
        }
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val generatedAt = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID")).format(Date())

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // --- PAGE 1: Header ---
        var currentY = 45f

        // Brand Banner Top Bar
        cardPaint.color = Color.parseColor("#065F46") // Emerald dark
        canvas.drawRoundRect(RectF(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + 68f), 8f, 8f, cardPaint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 18f
        textPaint.isFakeBoldText = true
        canvas.drawText("DOMPETZU - LAPORAN KEUANGAN", MARGIN_LEFT + 16f, currentY + 28f, textPaint)

        textPaint.textSize = 11f
        textPaint.isFakeBoldText = false
        canvas.drawText("Periode: ${report.monthTitle}  •  Dibuat pada: $generatedAt", MARGIN_LEFT + 16f, currentY + 50f, textPaint)

        currentY += 82f

        // --- KPI Summary Cards (4 Cards Grid) ---
        val cardWidth = (CONTENT_WIDTH - 18f) / 2f
        val cardHeight = 52f

        // Card 1: Pemasukan
        drawKpiCard(
            canvas = canvas,
            rect = RectF(MARGIN_LEFT, currentY, MARGIN_LEFT + cardWidth, currentY + cardHeight),
            title = "TOTAL PEMASUKAN",
            value = MonthlyReportCalculator.formatRupiah(report.totalIncome),
            bgColor = Color.parseColor("#ECFDF5"),
            borderColor = Color.parseColor("#A7F3D0"),
            textColor = Color.parseColor("#065F46")
        )

        // Card 2: Pengeluaran
        drawKpiCard(
            canvas = canvas,
            rect = RectF(MARGIN_LEFT + cardWidth + 18f, currentY, MARGIN_RIGHT, currentY + cardHeight),
            title = "TOTAL PENGELUARAN",
            value = MonthlyReportCalculator.formatRupiah(report.totalExpense),
            bgColor = Color.parseColor("#FEF2F2"),
            borderColor = Color.parseColor("#FECACA"),
            textColor = Color.parseColor("#991B1B")
        )

        currentY += cardHeight + 10f

        // Card 3: Arus Kas Bersih
        val netColor = if (report.netCashFlow >= 0) Color.parseColor("#065F46") else Color.parseColor("#991B1B")
        drawKpiCard(
            canvas = canvas,
            rect = RectF(MARGIN_LEFT, currentY, MARGIN_LEFT + cardWidth, currentY + cardHeight),
            title = "ARUS KAS BERSIH (NET)",
            value = (if (report.netCashFlow >= 0) "+ " else "") + MonthlyReportCalculator.formatRupiah(report.netCashFlow),
            bgColor = Color.parseColor("#F8FAFC"),
            borderColor = Color.parseColor("#E2E8F0"),
            textColor = netColor
        )

        // Card 4: Rasio Tabungan & Kesehatan
        drawKpiCard(
            canvas = canvas,
            rect = RectF(MARGIN_LEFT + cardWidth + 18f, currentY, MARGIN_RIGHT, currentY + cardHeight),
            title = "RASIO TABUNGAN & KESEHATAN",
            value = "${String.format(Locale.US, "%.1f", report.savingsRate)}% (${report.financialHealth.level})",
            bgColor = Color.parseColor("#F8FAFC"),
            borderColor = Color.parseColor("#E2E8F0"),
            textColor = Color.parseColor("#0F172A")
        )

        currentY += cardHeight + 18f

        // --- Category Summaries Section ---
        if (report.categorySummaries.isNotEmpty()) {
            textPaint.color = Color.parseColor("#0F172A")
            textPaint.textSize = 12f
            textPaint.isFakeBoldText = true
            canvas.drawText("Ringkasan Pengeluaran per Kategori", MARGIN_LEFT, currentY, textPaint)
            currentY += 12f

            // Table Header
            cardPaint.color = Color.parseColor("#F1F5F9")
            canvas.drawRect(RectF(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + 20f), cardPaint)

            textPaint.color = Color.parseColor("#475569")
            textPaint.textSize = 9f
            textPaint.isFakeBoldText = true
            canvas.drawText("Kategori", MARGIN_LEFT + 8f, currentY + 14f, textPaint)
            canvas.drawText("Total Pengeluaran", MARGIN_LEFT + 220f, currentY + 14f, textPaint)
            canvas.drawText("Porsi (%)", MARGIN_LEFT + 360f, currentY + 14f, textPaint)
            canvas.drawText("Anggaran", MARGIN_LEFT + 440f, currentY + 14f, textPaint)

            currentY += 20f

            // Category rows
            textPaint.isFakeBoldText = false
            report.categorySummaries.take(5).forEachIndexed { index, cat ->
                if (index % 2 == 1) {
                    cardPaint.color = Color.parseColor("#F8FAFC")
                    canvas.drawRect(RectF(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + 18f), cardPaint)
                }
                textPaint.color = Color.parseColor("#1E293B")
                canvas.drawText(cat.categoryName, MARGIN_LEFT + 8f, currentY + 13f, textPaint)
                canvas.drawText(MonthlyReportCalculator.formatRupiah(cat.totalAmount), MARGIN_LEFT + 220f, currentY + 13f, textPaint)
                canvas.drawText("${String.format(Locale.US, "%.1f", cat.percentageOfTotal)}%", MARGIN_LEFT + 360f, currentY + 13f, textPaint)

                val budgetText = if (cat.budgetAmount > 0) {
                    MonthlyReportCalculator.formatRupiah(cat.budgetAmount)
                } else {
                    "-"
                }
                canvas.drawText(budgetText, MARGIN_LEFT + 440f, currentY + 13f, textPaint)

                currentY += 18f
            }

            canvas.drawLine(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY, linePaint)
            currentY += 18f
        }

        // --- Transaction History Header ---
        textPaint.color = Color.parseColor("#0F172A")
        textPaint.textSize = 12f
        textPaint.isFakeBoldText = true
        canvas.drawText("Rincian Riwayat Transaksi (${transactions.size} transaksi)", MARGIN_LEFT, currentY, textPaint)
        currentY += 12f

        // Table Header for Transactions
        fun drawTransactionTableHeader(c: Canvas, y: Float) {
            cardPaint.color = Color.parseColor("#1E293B") // Dark slate header
            c.drawRect(RectF(MARGIN_LEFT, y, MARGIN_RIGHT, y + 20f), cardPaint)

            textPaint.color = Color.WHITE
            textPaint.textSize = 8.5f
            textPaint.isFakeBoldText = true
            c.drawText("No", MARGIN_LEFT + 6f, y + 14f, textPaint)
            c.drawText("Tanggal", MARGIN_LEFT + 26f, y + 14f, textPaint)
            c.drawText("Deskripsi", MARGIN_LEFT + 95f, y + 14f, textPaint)
            c.drawText("Kategori", MARGIN_LEFT + 250f, y + 14f, textPaint)
            c.drawText("Dompet / Akun", MARGIN_LEFT + 375f, y + 14f, textPaint)
            c.drawText("Nominal (Rp)", MARGIN_LEFT + 460f, y + 14f, textPaint)
        }

        drawTransactionTableHeader(canvas, currentY)
        currentY += 20f

        // Draw transaction rows with pagination support
        val rowHeight = 18f
        val maxY = PAGE_HEIGHT - 45f // Leave margin for footer

        transactions.forEachIndexed { index, tx ->
            // Check if page overflow
            if (currentY + rowHeight > maxY) {
                // Draw footer for current page
                drawPageFooter(canvas, pageNumber)
                pdfDocument.finishPage(page)

                // Start next page
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = 40f

                // Draw continued header
                textPaint.color = Color.parseColor("#0F172A")
                textPaint.textSize = 11f
                textPaint.isFakeBoldText = true
                canvas.drawText("Rincian Riwayat Transaksi (Lanjutan) - ${report.monthTitle}", MARGIN_LEFT, currentY, textPaint)
                currentY += 14f

                drawTransactionTableHeader(canvas, currentY)
                currentY += 20f
            }

            // Alternating row background
            if (index % 2 == 1) {
                cardPaint.color = Color.parseColor("#F8FAFC")
                canvas.drawRect(RectF(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + rowHeight), cardPaint)
            }

            textPaint.isFakeBoldText = false
            textPaint.textSize = 8f
            textPaint.color = Color.parseColor("#334155")

            // Index
            canvas.drawText("${index + 1}", MARGIN_LEFT + 6f, currentY + 12f, textPaint)

            // Date
            canvas.drawText(dateFormat.format(Date(tx.dateMillis)), MARGIN_LEFT + 26f, currentY + 12f, textPaint)

            // Description (truncated if too long)
            val desc = if (tx.title.length > 28) tx.title.take(26) + "..." else tx.title
            canvas.drawText(desc, MARGIN_LEFT + 95f, currentY + 12f, textPaint)

            // Category
            val catName = if (tx.category.length > 20) tx.category.take(18) + ".." else tx.category
            canvas.drawText(catName, MARGIN_LEFT + 250f, currentY + 12f, textPaint)

            // Wallet
            canvas.drawText(tx.walletName, MARGIN_LEFT + 375f, currentY + 12f, textPaint)

            // Amount
            val isExpense = tx.type.equals(TransactionType.EXPENSE.name, ignoreCase = true)
            textPaint.isFakeBoldText = true
            textPaint.color = if (isExpense) Color.parseColor("#DC2626") else Color.parseColor("#059669")
            val amountFormatted = (if (isExpense) "- " else "+ ") + MonthlyReportCalculator.formatRupiah(tx.amount)
            canvas.drawText(amountFormatted, MARGIN_LEFT + 460f, currentY + 12f, textPaint)

            currentY += rowHeight
        }

        // Draw footer on last page
        drawPageFooter(canvas, pageNumber)
        pdfDocument.finishPage(page)

        // Write to file
        val reportsDir = File(context.cacheDir, "reports").apply {
            if (!exists()) mkdirs()
        }
        val safeMonthTitle = report.monthTitle.replace(" ", "_")
        val file = File(reportsDir, "Laporan_Keuangan_DompetZu_${safeMonthTitle}.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return file
    }

    private fun drawKpiCard(
        canvas: Canvas,
        rect: RectF,
        title: String,
        value: String,
        bgColor: Int,
        borderColor: Int,
        textColor: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = bgColor
        canvas.drawRoundRect(rect, 6f, 6f, paint)

        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        // Title
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 7.5f
        paint.isFakeBoldText = true
        canvas.drawText(title, rect.left + 10f, rect.top + 18f, paint)

        // Value
        paint.color = textColor
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText(value, rect.left + 10f, rect.top + 38f, paint)
    }

    private fun drawPageFooter(canvas: Canvas, pageNumber: Int) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 8f
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 0.5f
        }
        canvas.drawLine(MARGIN_LEFT, PAGE_HEIGHT - 32f, MARGIN_RIGHT, PAGE_HEIGHT - 32f, linePaint)
        canvas.drawText("DompetZu — Aplikasi Pengelola Keuangan Pribadi", MARGIN_LEFT, PAGE_HEIGHT - 18f, textPaint)
        canvas.drawText("Halaman $pageNumber", MARGIN_RIGHT - 50f, PAGE_HEIGHT - 18f, textPaint)
    }

    fun sharePdfReport(context: Context, pdfFile: File, title: String = "Laporan Keuangan Bulanan") {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "Berikut adalah lampiran dokumen PDF Laporan Keuangan Bulanan dari aplikasi DompetZu.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Bagikan Laporan PDF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun openPdfReport(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(intent, "Buka Dokumen PDF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
