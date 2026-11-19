package ir.khodhesab.data.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import ir.khodhesab.core.date.JalaliCalendarHelper
import ir.khodhesab.domain.model.CurrencyUnit
import ir.khodhesab.domain.model.Transaction
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.ui.util.PersianCurrencyFormatter
import java.io.File
import javax.inject.Inject

/**
 * Utility for exporting transactions to Excel-compatible CSV format with UTF-8 BOM encoding.
 */
class CsvReportExporter @Inject constructor() {

    companion object {
        const val UTF8_BOM = "\uFEFF"
    }

    /**
     * Generates a UTF-8 BOM encoded CSV string for a list of transactions.
     *
     * @param transactions List of transactions to export.
     * @param currencyUnit Preferred [CurrencyUnit] for amount formatting.
     * @return Formatted CSV string.
     */
    fun generateCsvContent(
        transactions: List<Transaction>,
        currencyUnit: CurrencyUnit = CurrencyUnit.TOMAN
    ): String {
        val sb = StringBuilder()
        sb.append(UTF8_BOM)

        val unitLabel = if (currencyUnit == CurrencyUnit.TOMAN) "تومان" else "ریال"
        sb.append("تاریخ شمسی,زمان,بانک,حساب,نوع تراکنش,دسته‌بندی,مبلغ ($unitLabel),مانده حساب,توضیحات\n")

        for (tx in transactions) {
            val jDate = JalaliCalendarHelper.fromEpochMillis(tx.timestamp)
            val dateStr = JalaliCalendarHelper.formatNumeric(jDate, usePersianDigits = true)
            val timeStr = "%02d:%02d".format(jDate.hour, jDate.minute)
            val bankName = escapeCsv(tx.account.bank.persianName)
            val accountTitle = escapeCsv(tx.account.title)
            val kindStr = when (tx.kind) {
                TransactionKind.WITHDRAWAL -> "برداشت"
                TransactionKind.DEPOSIT -> "واریز"
                TransactionKind.TRANSFER -> "انتقال"
            }
            val categoryName = escapeCsv(tx.category?.name ?: "دسته‌بندی نشده")

            val amountVal = if (currencyUnit == CurrencyUnit.TOMAN) tx.amount / 10L else tx.amount
            val amountStr = PersianCurrencyFormatter.toPersianDigits(amountVal.toString())

            val balanceVal = tx.balanceAfter?.let { if (currencyUnit == CurrencyUnit.TOMAN) it / 10L else it }
            val balanceStr = balanceVal?.let { PersianCurrencyFormatter.toPersianDigits(it.toString()) } ?: "-"

            val desc = escapeCsv(tx.description ?: "")

            sb.append("$dateStr,$timeStr,$bankName,$accountTitle,$kindStr,$categoryName,$amountStr,$balanceStr,$desc\n")
        }

        return sb.toString()
    }

    /**
     * Exports transactions to a cache file and returns a shareable [FileProvider] Uri.
     */
    fun exportToCacheFile(
        context: Context,
        transactions: List<Transaction>,
        currencyUnit: CurrencyUnit = CurrencyUnit.TOMAN,
        fileName: String = "khodhesab_export.csv"
    ): Uri {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val file = File(exportDir, fileName)
        file.writeText(generateCsvContent(transactions, currencyUnit), Charsets.UTF_8)

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun escapeCsv(text: String): String {
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            val escaped = text.replace("\"", "\"\"")
            return "\"$escaped\""
        }
        return text
    }
}
