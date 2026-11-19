package ir.khodhesab.domain.usecase

import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * UseCase for creating and deleting custom categories.
 */
class ManageCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend fun createCustomCategory(
        name: String,
        kind: TransactionKind,
        iconKey: String,
        colorHex: String,
        keywords: List<String>
    ): Result<Long> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("نام دسته‌بندی نمی‌تواند خالی باشد."))
        }
        val id = categoryRepository.createCustomCategory(name.trim(), kind, iconKey, colorHex, keywords)
        return Result.success(id)
    }

    suspend fun deleteCustomCategory(categoryId: Long) {
        categoryRepository.deleteCustomCategory(categoryId)
    }
}
