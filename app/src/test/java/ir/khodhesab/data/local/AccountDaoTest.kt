package ir.khodhesab.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import ir.khodhesab.data.local.dao.AccountDao
import ir.khodhesab.data.local.dao.BankDao
import ir.khodhesab.data.local.entity.AccountEntity
import ir.khodhesab.data.local.entity.BankEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * In-Memory Room database unit tests for [AccountDao].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class AccountDaoTest {

    private lateinit var database: KhodHesabDatabase
    private lateinit var bankDao: BankDao
    private lateinit var accountDao: AccountDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, KhodHesabDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        bankDao = database.bankDao()
        accountDao = database.accountDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAccount_createsAccountSuccessfully() = runTest {
        val bankId = bankDao.insertBank(
            BankEntity(code = "MELLAT", persianName = "بانک ملت", senderNumbers = listOf("200033"), colorHex = "#RED", iconKey = "icon")
        )

        val account = AccountEntity(bankId = bankId, accountIdentifier = "1234", title = "حساب اصلی", currentBalance = 1_000_000L)
        val accountId = accountDao.insertAccount(account)

        assertThat(accountId).isGreaterThan(0L)

        val fetched = accountDao.getAccountById(accountId)
        assertThat(fetched).isNotNull()
        assertThat(fetched?.title).isEqualTo("حساب اصلی")
        assertThat(fetched?.currentBalance).isEqualTo(1_000_000L)
    }

    @Test
    fun insertAccount_enforcesUniqueCompositeIndexOnBankIdAndIdentifier() = runTest {
        val bankId = bankDao.insertBank(
            BankEntity(code = "MELLI", persianName = "بانک ملی", senderNumbers = listOf("9611"), colorHex = "#BLUE", iconKey = "icon")
        )

        val account1 = AccountEntity(bankId = bankId, accountIdentifier = "9999", title = "حساب ۱")
        val account2 = AccountEntity(bankId = bankId, accountIdentifier = "9999", title = "حساب ۲")

        val id1 = accountDao.insertAccount(account1)
        val id2 = accountDao.insertAccount(account2)

        assertThat(id1).isGreaterThan(0L)
        assertThat(id2).isEqualTo(-1L) // OnConflictStrategy.IGNORE returns -1L on conflict
    }

    @Test
    fun updateBalanceIfNewer_updatesBalanceOnlyWhenTimestampIsNewer() = runTest {
        val bankId = bankDao.insertBank(
            BankEntity(code = "SAMAN", persianName = "بانک سامان", senderNumbers = listOf("20008000"), colorHex = "#PURPLE", iconKey = "icon")
        )
        val accountId = accountDao.insertAccount(
            AccountEntity(bankId = bankId, accountIdentifier = "5555", title = "حساب سامان", currentBalance = 500_000L, lastBalanceUpdate = 1000L)
        )

        // Try updating with newer timestamp (2000L) -> should succeed
        val updatedRows1 = accountDao.updateBalanceIfNewer(accountId, 1_000_000L, 2000L)
        assertThat(updatedRows1).isEqualTo(1)

        val accountAfterNewer = accountDao.getAccountById(accountId)
        assertThat(accountAfterNewer?.currentBalance).isEqualTo(1_000_000L)
        assertThat(accountAfterNewer?.lastBalanceUpdate).isEqualTo(2000L)

        // Try updating with older timestamp (1500L) -> should NOT update
        val updatedRows2 = accountDao.updateBalanceIfNewer(accountId, 200_000L, 1500L)
        assertThat(updatedRows2).isEqualTo(0)

        val accountAfterOlder = accountDao.getAccountById(accountId)
        assertThat(accountAfterOlder?.currentBalance).isEqualTo(1_000_000L)
        assertThat(accountAfterOlder?.lastBalanceUpdate).isEqualTo(2000L)
    }

    @Test
    fun getTotalActiveBalance_calculatesSumOfActiveAccountsOnly() = runTest {
        val bankId = bankDao.insertBank(
            BankEntity(code = "BLU", persianName = "بلوبانک", senderNumbers = listOf("10008000"), colorHex = "#BLUE", iconKey = "icon")
        )

        accountDao.insertAccount(AccountEntity(bankId = bankId, accountIdentifier = "0001", title = "فعال ۱", currentBalance = 100_000L, isActive = true))
        accountDao.insertAccount(AccountEntity(bankId = bankId, accountIdentifier = "0002", title = "فعال ۲", currentBalance = 200_000L, isActive = true))
        accountDao.insertAccount(AccountEntity(bankId = bankId, accountIdentifier = "0003", title = "غیرفعال", currentBalance = 500_000L, isActive = false))

        accountDao.getTotalActiveBalance().test {
            val total = awaitItem()
            assertThat(total).isEqualTo(300_000L)
        }
    }
}
