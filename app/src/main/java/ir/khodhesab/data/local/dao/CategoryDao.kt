package ir.khodhesab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ir.khodhesab.data.local.entity.CategoryEntity
import ir.khodhesab.data.local.model.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [CategoryEntity].
 */
@Dao
interface CategoryDao {

    /**
     * Inserts a single category record.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity): Long

    /**
     * Inserts a list of categories ignoring conflicts.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>): List<Long>

    /**
     * Updates a category record. Returns number of updated rows.
     */
    @Update
    suspend fun updateCategory(category: CategoryEntity): Int

    /**
     * Deletes a user-created custom category (`isDefault = 0`). Default system categories cannot be deleted.
     */
    @Query("DELETE FROM categories WHERE id = :categoryId AND isDefault = 0")
    suspend fun deleteCustomCategory(categoryId: Long): Int

    /**
     * Observes all categories sorted by type and name.
     */
    @Query("SELECT * FROM categories ORDER BY type ASC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    /**
     * Observes categories filtered by [TransactionType].
     */
    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    fun getCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>>

    /**
     * Retrieves a single category by ID.
     */
    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    suspend fun getCategoryById(categoryId: Long): CategoryEntity?
}
