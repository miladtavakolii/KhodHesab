package ir.khodhesab.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import ir.khodhesab.data.local.dao.CategoryDao
import ir.khodhesab.data.local.entity.CategoryEntity
import ir.khodhesab.data.local.model.TransactionType
import ir.khodhesab.domain.model.TransactionKind
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [CategoryRepositoryImpl].
 */
class CategoryRepositoryImplTest {

    private val categoryDao: CategoryDao = mockk()
    private lateinit var repository: CategoryRepositoryImpl

    @Before
    fun setup() {
        repository = CategoryRepositoryImpl(categoryDao)
    }

    @Test
    fun matchCategoryByKeywords_findsMatchingCategoryForNormalizedText() = runTest {
        val categoryEntities = listOf(
            CategoryEntity(id = 1, name = "رستوران", type = TransactionType.WITHDRAWAL, iconKey = "food", colorHex = "#RED", keywords = listOf("کافه", "اسنپ فود")),
            CategoryEntity(id = 2, name = "سوخت", type = TransactionType.WITHDRAWAL, iconKey = "gas", colorHex = "#YELLOW", keywords = listOf("بنزین"))
        )
        every { categoryDao.getCategoriesByType(TransactionType.WITHDRAWAL) } returns flowOf(categoryEntities)

        val match = repository.matchCategoryByKeywords("خرید از اسنپ فود", TransactionKind.WITHDRAWAL)
        assertThat(match).isNotNull()
        assertThat(match?.name).isEqualTo("رستوران")
    }
}
