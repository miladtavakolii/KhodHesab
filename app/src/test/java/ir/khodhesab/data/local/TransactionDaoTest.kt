package ir.khodhesab.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import ir.khodhesab.data.local.dao.AccountDao
import ir.khodhesab.data.local.dao.BankDao
import ir.khodhesab.data.local.dao.CategoryDao
import ir.khodhesab.data.local.dao.TransactionDao
import ir.khodhesab.data.local.entity.AccountEntity
import ir.khodhesab.data.local.entity.BankEntity
import ir.khodhesab.data.local.entity.CategoryEntity
import ir.khodhesab.data.local.entity.TransactionEntity
import ir.khodhesab.data.local.model.TransactionType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * In-Memory Room database unit tests for [TransactionDao].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class TransactionDaoTest {

    private lateinit var database: KhodHesabDatabase
    private lateinit var bankDao: BankDao
    private lateinit var accountDao: AccountDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var transactionDao: TransactionDao

    private var bankId: Long = 0
    private var accountId: Long = 0
    private var categoryId: Long = 0

    @Before
    fun setup() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, KhodHesabDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        bankDao = database.bankDao()
        accountDao = database.accountDao()
        categoryDao = database.categoryDao()
        transactionDao = database.transactionDao()

        bankId = bankDao.insertBank(BankEntity(code = "MELLI", persianName = "بانک ملی", senderNumbers = listOf("9611"), colorHex = "#BLUE", iconKey = "icon"))
        accountId = accountDao.insertAccount(AccountEntity(bankId = bankId, accountIdentifier = "1234", title = "حساب ۱"))
        categoryId = categoryDao.insertCategory(CategoryEntity(name = "رستوران", type = TransactionType.WITHDRAWAL, iconKey = "food", colorHex = "#RED"))
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertTransaction_enforcesUniqueSmsHashDeduplication() = runTest {
        val tx1 = TransactionEntity(
            accountId = accountId,
            categoryId = categoryId,
            type = TransactionType.WITHDRAWAL,
            amount = 100_000L,
            timestamp = 1000L,
            smsHash = "HASH_ABC_123"
        )
        val tx2 = TransactionEntity(
            accountId = accountId,
            categoryId = categoryId,
            type = TransactionType.WITHDRAWAL,
            amount = 100_000L,
            timestamp = 1000L,
            smsHash = "HASH_ABC_123"
        )

        val id1 = transactionDao.insertTransaction(tx1)
        val id2 = transactionDao.insertTransaction(tx2)

        assertThat(id1).isGreaterThan(0L)
        assertThat(id2).isEqualTo(-1L)

        val exists = transactionDao.existsBySmsHash("HASH_ABC_123")
        assertThat(exists).isTrue()
    }

    @Test
    fun getTransactionsFiltered_retrievesRelationalProjectionWithDetails() = runTest {
        val txId = transactionDao.insertTransaction(
            TransactionEntity(
                accountId = accountId,
                categoryId = categoryId,
                type = TransactionType.WITHDRAWAL,
                amount = 250_000L,
                timestamp = 5000L,
                description = "خرید شام"
            )
        )

        transactionDao.getTransactionsFiltered(
            accountId = accountId,
            type = TransactionType.WITHDRAWAL,
            startTime = 1000L,
            endTime = 10000L,
            searchQuery = "شام"
        ).test {
            val list = awaitItem()
            assertThat(list).hasSize(1)
            val item = list.first()

            assertThat(item.transaction.id).isEqualTo(txId)
            assertThat(item.transaction.amount).isEqualTo(250_000L)
            assertThat(item.accountWithBank.account.title).isEqualTo("حساب ۱")
            assertThat(item.accountWithBank.bank.code).isEqualTo("MELLI")
            assertThat(item.category?.name).isEqualTo("رستوران")
        }
    }

    @Test
    fun getCategoryBreakdown_aggregatesTotalsAndCountsPerCategory() = runTest {
        val catFood = categoryDao.insertCategory(CategoryEntity(name = "غذا", type = TransactionType.WITHDRAWAL, iconKey = "food", colorHex = "#RED"))
        val catTransport = categoryDao.insertCategory(CategoryEntity(name = "سفر", type = TransactionType.WITHDRAWAL, iconKey = "bus", colorHex = "#YELLOW"))

        transactionDao.insertTransaction(TransactionEntity(accountId = accountId, categoryId = catFood, type = TransactionType.WITHDRAWAL, amount = 100_000L, timestamp = 2000L))
        transactionDao.insertTransaction(TransactionEntity(accountId = accountId, categoryId = catFood, type = TransactionType.WITHDRAWAL, amount = 150_000L, timestamp = 3000L))
        transactionDao.insertTransaction(TransactionEntity(accountId = accountId, categoryId = catTransport, type = TransactionType.WITHDRAWAL, amount = 50_000L, timestamp = 4000L))

        transactionDao.getCategoryBreakdown(
            type = TransactionType.WITHDRAWAL,
            startTime = 1000L,
            endTime = 5000L,
            accountId = accountId
        ).test {
            val breakdown = awaitItem()
            assertThat(breakdown).hasSize(2)

            val topCategory = breakdown.first()
            assertThat(topCategory.categoryId).isEqualTo(catFood)
            assertThat(topCategory.totalAmount).isEqualTo(250_000L)
            assertThat(topCategory.transactionCount).isEqualTo(2)

            val secondCategory = breakdown[1]
            assertThat(secondCategory.categoryId).isEqualTo(catTransport)
            assertThat(secondCategory.totalAmount).isEqualTo(50_000L)
            assertThat(secondCategory.transactionCount).isEqualTo(1)
        }
    }
}
