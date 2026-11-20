package ir.khodhesab.sms_engine.parser

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import ir.khodhesab.domain.model.Bank
import ir.khodhesab.domain.repository.BankRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [BankDetector].
 */
class BankDetectorTest {

    private val bankRepository: BankRepository = mockk()
    private lateinit var detector: BankDetector

    private val otherBank = Bank(id = 99, code = "OTHER", persianName = "سایر بانک‌ها", colorHex = "#5", iconKey = "i")

    @Before
    fun setup() {
        coEvery { bankRepository.getBankByCode("OTHER") } returns otherBank
        detector = BankDetector(bankRepository)
    }

    @Test
    fun detectBank_findsBankDirectlyBySender() = runTest {
        val melli = Bank(id = 1, code = "MELLI", persianName = "بانک ملی", colorHex = "#B", iconKey = "i")
        coEvery { bankRepository.findBankBySender("9611") } returns melli

        val detected = detector.detectBank("9611", "برداشت 1000")
        assertThat(detected?.code).isEqualTo("MELLI")
    }

    @Test
    fun detectBank_fallsBackToBodyKeywordSearchWhenSenderIsGeneric() = runTest {
        val mellat = Bank(id = 2, code = "MELLAT", persianName = "بانک ملت", colorHex = "#R", iconKey = "i")
        coEvery { bankRepository.findBankBySender("2000000") } returns null
        coEvery { bankRepository.getBankByCode("MELLAT") } returns mellat

        val detected = detector.detectBank("2000000", "بانک ملت\nبرداشت 25000")
        assertThat(detected?.code).isEqualTo("MELLAT")
    }

    @Test
    fun detectBank_fallsBackToOtherBankWhenNoMatch() = runTest {
        coEvery { bankRepository.findBankBySender("99999") } returns null

        val detected = detector.detectBank("99999", "برداشت 25000 ریال")
        assertThat(detected?.code).isEqualTo("OTHER")
    }
}
