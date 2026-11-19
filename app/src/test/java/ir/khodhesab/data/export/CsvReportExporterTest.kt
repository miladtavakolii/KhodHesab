package ir.khodhesab.data.export

import com.google.common.truth.Truth.assertThat
import ir.khodhesab.domain.model.Account
import ir.khodhesab.domain.model.Bank
import ir.khodhesab.domain.model.Category
import ir.khodhesab.domain.model.CurrencyUnit
import ir.khodhesab.domain.model.Transaction
import ir.khodhesab.domain.model.TransactionKind
import org.junit.Test

/**
 * Unit tests for [CsvReportExporter].
 */
class CsvReportExporterTest {

    private val exporter = CsvReportExporter()

    private val bank = Bank(id = 1, code = "MELLAT", persianName = "بانک ملت", colorHex = "#R", iconKey = "i")
    private val account = Account(id = 10, bank = bank, accountIdentifier = "1234", title = "کارت روزمره")
    private val category = Category(id = 5, name = "غذا و رستوران", kind = TransactionKind.WITHDRAWAL, iconKey = "food", colorHex = "#R")

    private val tx = Transaction(
        id = 100,
        account = account,
        category = category,
        kind = TransactionKind.WITHDRAWAL,
        amount = 150_000L,
        balanceAfter = 5_000_000L,
        timestamp = 1700000000000L,
        description = "خرید پیتزا, پپسی"
    )

    @Test
    fun generateCsvContent_startsContentWithUtf8BomAndPersianHeaders() {
        val csv = exporter.generateCsvContent(listOf(tx), CurrencyUnit.TOMAN)

        assertThat(csv).startsWith(CsvReportExporter.UTF8_BOM)
        assertThat(csv).contains("تاریخ شمسی,زمان,بانک,حساب,نوع تراکنش,دسته‌بندی,مبلغ (تومان),مانده حساب,توضیحات")
        assertThat(csv).contains("بانک ملت")
        assertThat(csv).contains("برداشت")
        assertThat(csv).contains("غذا و رستوران")
        assertThat(csv).contains("\"خرید پیتزا, پپسی\"")
    }
}
