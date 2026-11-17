package ir.khodhesab.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import ir.khodhesab.data.local.dao.BankDao
import ir.khodhesab.data.local.dao.CategoryDao
import ir.khodhesab.data.local.seed.DatabaseSeeder
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests verifying default [DatabaseSeeder] content integrity.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class DatabaseSeederTest {

    private lateinit var database: KhodHesabDatabase
    private lateinit var bankDao: BankDao
    private lateinit var categoryDao: CategoryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, KhodHesabDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        bankDao = database.bankDao()
        categoryDao = database.categoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun databaseSeeder_containsAtLeast16IranianBanks() {
        assertThat(DatabaseSeeder.DEFAULT_BANKS.size).isAtLeast(16)

        val codes = DatabaseSeeder.DEFAULT_BANKS.map { it.code }
        assertThat(codes).containsAtLeast("MELLI", "MELLAT", "SADERAT", "TEJARAT", "PASARGAD", "SAMAN", "BLU")
    }

    @Test
    fun databaseSeeder_populatesBanksAndCategoriesWithoutDuplicates() = runTest {
        bankDao.insertAll(DatabaseSeeder.DEFAULT_BANKS)
        categoryDao.insertAll(DatabaseSeeder.DEFAULT_CATEGORIES)

        bankDao.getAllBanks().test {
            val banks = awaitItem()
            assertThat(banks.size).isEqualTo(DatabaseSeeder.DEFAULT_BANKS.size)
        }

        categoryDao.getAllCategories().test {
            val categories = awaitItem()
            assertThat(categories.size).isEqualTo(DatabaseSeeder.DEFAULT_CATEGORIES.size)
        }
    }
}
