package ir.khodhesab.domain.repository

import ir.khodhesab.domain.model.Category
import ir.khodhesab.domain.model.TransactionKind
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for managing transaction categories and keyword matching.
 */
interface CategoryRepository {

    /**
     * Observes all transaction categories sorted by kind and name.
     */
    fun observeAllCategories(): Flow<List<Category>>

    /**
     * Observes categories filtered by [TransactionKind] (`DEPOSIT` or `WITHDRAWAL`).
     */
    fun observeCategoriesByKind(kind: TransactionKind): Flow<List<Category>>

    /**
     * Creates a new custom user category (`isDefault = false`).
     */
    suspend fun createCustomCategory(
        name: String,
        kind: TransactionKind,
        iconKey: String,
        colorHex: String,
        keywords: List<String>
    ): Long

    /**
     * Updates an existing category.
     */
    suspend fun updateCategory(category: Category)

    /**
     * Deletes a custom user category by ID. Fails if category is a default system category.
     */
    suspend fun deleteCustomCategory(categoryId: Long)

    /**
     * Matches raw/normalized SMS text against category keywords for auto-categorization.
     */
    suspend fun matchCategoryByKeywords(text: String, kind: TransactionKind): Category?
}
