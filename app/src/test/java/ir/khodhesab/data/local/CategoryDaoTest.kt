package ir.khodhesab.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import ir.khodhesab.data.local.dao.CategoryDao
import ir.khodhesab.data.local.entity.CategoryEntity
import ir.khodhesab.data.local.model.TransactionType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * In-Memory Room database unit tests for [CategoryDao].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class CategoryDaoTest {

    private lateinit var database: KhodHesabDatabase
    private lateinit var categoryDao: CategoryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, KhodHesabDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        categoryDao = database.categoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetCategoriesByType_filtersCorrectly() = runTest {
        val expense = CategoryEntity(name = "رستوران", type = TransactionType.WITHDRAWAL, iconKey = "food", colorHex = "#RED", isDefault = true)
        val income = CategoryEntity(name = "حقوق", type = TransactionType.DEPOSIT, iconKey = "money", colorHex = "#GREEN", isDefault = true)

        categoryDao.insertCategory(expense)
        categoryDao.insertCategory(income)

        categoryDao.getCategoriesByType(TransactionType.WITHDRAWAL).test {
            val items = awaitItem()
            assertThat(items).hasSize(1)
            assertThat(items.first().name).isEqualTo("رستوران")
        }

        categoryDao.getCategoriesByType(TransactionType.DEPOSIT).test {
            val items = awaitItem()
            assertThat(items).hasSize(1)
            assertThat(items.first().name).isEqualTo("حقوق")
        }
    }

    @Test
    fun deleteCustomCategory_deletesOnlyNonDefaultCategories() = runTest {
        val defaultCatId = categoryDao.insertCategory(
            CategoryEntity(name = "پیش‌فرض", type = TransactionType.WITHDRAWAL, iconKey = "def", colorHex = "#000", isDefault = true)
        )
        val customCatId = categoryDao.insertCategory(
            CategoryEntity(name = "سفارشی", type = TransactionType.WITHDRAWAL, iconKey = "cust", colorHex = "#FFF", isDefault = false)
        )

        // Attempt deleting default category -> should fail (0 rows deleted)
        val deletedDefault = categoryDao.deleteCustomCategory(defaultCatId)
        assertThat(deletedDefault).isEqualTo(0)
        assertThat(categoryDao.getCategoryById(defaultCatId)).isNotNull()

        // Attempt deleting custom category -> should succeed (1 row deleted)
        val deletedCustom = categoryDao.deleteCustomCategory(customCatId)
        assertThat(deletedCustom).isEqualTo(1)
        assertThat(categoryDao.getCategoryById(customCatId)).isNull()
    }
}
