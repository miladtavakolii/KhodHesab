package ir.khodhesab.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import ir.khodhesab.domain.model.Account
import ir.khodhesab.domain.model.Bank
import ir.khodhesab.domain.model.Category
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.AccountRepository
import ir.khodhesab.domain.repository.BankRepository
import ir.khodhesab.domain.repository.CategoryRepository
import ir.khodhesab.domain.repository.TransactionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [RecordSmsTransactionUseCase].
 */
class RecordSmsTransactionUseCaseTest {

    private val bankRepository: BankRepository = mockk()
    private val accountRepository: AccountRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()

    private lateinit var useCase: RecordSmsTransactionUseCase

    @Before
    fun setup() {
        useCase = RecordSmsTransactionUseCase(
            bankRepository,
            accountRepository,
            categoryRepository,
            transactionRepository
        )
    }

    @Test
    fun invoke_skipsProcessingWhenSmsHashIsDuplicate() = runTest {
        coEvery { transactionRepository.existsBySmsHash("HASH_123") } returns true

        val result = useCase(
            senderNumber = "9611",
            accountIdentifier = "1234",
            kind = TransactionKind.WITHDRAWAL,
            amount = 100_000L,
            balanceAfter = 500_000L,
            timestamp = 1000L,
            rawSmsBody = "برداشت 100000",
            smsHash = "HASH_123"
        )

        assertThat(result).isEqualTo(RecordSmsResult.DuplicateSkipped)
        coVerify(exactly = 0) { bankRepository.findBankBySender(any()) }
    }

    @Test
    fun invoke_returnsBankNotRecognizedWhenSenderNotFound() = runTest {
        coEvery { transactionRepository.existsBySmsHash("HASH_123") } returns false
        coEvery { bankRepository.findBankBySender("99999") } returns null

        val result = useCase(
            senderNumber = "99999",
            accountIdentifier = "1234",
            kind = TransactionKind.WITHDRAWAL,
            amount = 100_000L,
            balanceAfter = 500_000L,
            timestamp = 1000L,
            rawSmsBody = "برداشت 100000",
            smsHash = "HASH_123"
        )

        assertThat(result).isEqualTo(RecordSmsResult.BankNotRecognized)
    }

    @Test
    fun invoke_insertsTransactionAndUpdatesBalanceWhenValid() = runTest {
        val bank = Bank(id = 1L, code = "MELLI", persianName = "بانک ملی", senderNumbers = listOf("9611"), colorHex = "#BLUE", iconKey = "i")
        val account = Account(id = 10L, bank = bank, accountIdentifier = "1234", title = "بانک ملی (1234)")
        val category = Category(id = 5L, name = "رستوران", kind = TransactionKind.WITHDRAWAL, iconKey = "f", colorHex = "#R")

        coEvery { transactionRepository.existsBySmsHash("HASH_123") } returns false
        coEvery { bankRepository.findBankBySender("9611") } returns bank
        coEvery { accountRepository.getOrCreateAccount(1L, "1234", any()) } returns account
        coEvery { categoryRepository.matchCategoryByKeywords(any(), TransactionKind.WITHDRAWAL) } returns category
        coEvery {
            transactionRepository.insertTransaction(
                accountId = 10L,
                categoryId = 5L,
                kind = TransactionKind.WITHDRAWAL,
                amount = 100_000L,
                balanceAfter = 500_000L,
                timestamp = 1000L,
                description = null,
                rawSmsBody = "برداشت 100000",
                smsHash = "HASH_123"
            )
        } returns 100L
        coEvery { accountRepository.updateBalanceIfNewer(10L, 500_000L, 1000L) } returns Unit

        val result = useCase(
            senderNumber = "9611",
            accountIdentifier = "1234",
            kind = TransactionKind.WITHDRAWAL,
            amount = 100_000L,
            balanceAfter = 500_000L,
            timestamp = 1000L,
            rawSmsBody = "برداشت 100000",
            smsHash = "HASH_123"
        )

        assertThat(result).isEqualTo(RecordSmsResult.Inserted(100L))
        coVerify(exactly = 1) { accountRepository.updateBalanceIfNewer(10L, 500_000L, 1000L) }
    }
}
