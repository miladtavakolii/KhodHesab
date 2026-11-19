package ir.khodhesab.data.repository

import ir.khodhesab.core.util.PersianTextNormalizer
import ir.khodhesab.data.local.dao.CategoryDao
import ir.khodhesab.data.local.entity.CategoryEntity
import ir.khodhesab.data.mapper.toDomain
import ir.khodhesab.data.mapper.toEntity
import ir.khodhesab.domain.model.Category
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data layer implementation of [CategoryRepository].
 */
@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun observeAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeCategoriesByKind(kind: TransactionKind): Flow<List<Category>> {
        return categoryDao.getCategoriesByType(kind.toEntity()).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun createCustomCategory(
        name: String,
        kind: TransactionKind,
        iconKey: String,
        colorHex: String,
        keywords: List<String>
    ): Long {
        val entity = CategoryEntity(
            name = name,
            type = kind.toEntity(),
            iconKey = iconKey,
            colorHex = colorHex,
            isDefault = false,
            keywords = keywords
        )
        return categoryDao.insertCategory(entity)
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category.toEntity())
    }

    override suspend fun deleteCustomCategory(categoryId: Long) {
        categoryDao.deleteCustomCategory(categoryId)
    }

    override suspend fun matchCategoryByKeywords(text: String, kind: TransactionKind): Category? {
        val normalizedText = PersianTextNormalizer.normalize(text)
        val categoriesOfKind = observeCategoriesByKind(kind).first()

        for (category in categoriesOfKind) {
            for (keyword in category.keywords) {
                if (keyword.isBlank()) continue
                val normalizedKeyword = PersianTextNormalizer.normalize(keyword)
                if (normalizedText.contains(normalizedKeyword, ignoreCase = true)) {
                    return category
                }
            }
        }
        return null
    }
}
