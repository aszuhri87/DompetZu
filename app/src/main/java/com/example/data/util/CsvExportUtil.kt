package com.example.data.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExportUtil {

    fun exportAndShareCsv(
        context: Context,
        transactions: List<TransactionEntity>,
        titlePrefix: String = "transaksi"
    ) {
        if (transactions.isEmpty()) {
            Toast.makeText(context, "Tidak ada data transaksi untuk diekspor", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(exportDir, "${titlePrefix}_${timeStamp}.csv")

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

            FileOutputStream(file).use { fos ->
                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                    // Write UTF-8 BOM so Excel opens it with correct UTF-8 encoding
                    writer.write("\uFEFF")

                    // Header Row
                    writer.write("No,Tanggal,Jenis,Judul,Kategori,Dompet,Nominal,Catatan\n")

                    transactions.forEachIndexed { index, tx ->
                        val dateStr = dateFormat.format(Date(tx.dateMillis))
                        val typeStr = if (tx.type == TransactionType.INCOME.name) "Pemasukan" else "Pengeluaran"
                        val titleEscaped = escapeCsvField(tx.title)
                        val categoryEscaped = escapeCsvField(tx.category)
                        val walletEscaped = escapeCsvField(tx.walletName)
                        val noteEscaped = escapeCsvField(tx.note)
                        val amountFormatted = String.format(Locale.US, "%.2f", tx.amount)

                        writer.write("${index + 1},\"$dateStr\",\"$typeStr\",\"$titleEscaped\",\"$categoryEscaped\",\"$walletEscaped\",$amountFormatted,\"$noteEscaped\"\n")
                    }
                    writer.flush()
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Ekspor Data Transaksi DompetZu")
                putExtra(Intent.EXTRA_TEXT, "Data ekspor transaksi keuangan (${transactions.size} transaksi).")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Bagikan / Simpan File CSV")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            Toast.makeText(context, "Gagal mengekspor CSV: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun escapeCsvField(value: String): String {
        return value.replace("\"", "\"\"").replace("\n", " ").replace("\r", "")
    }
}
