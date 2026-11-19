package ir.khodhesab.domain.usecase

import ir.khodhesab.domain.repository.AccountRepository
import javax.inject.Inject

/**
 * UseCase for managing user accounts (renaming titles and toggling active status).
 */
class ManageAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    suspend fun renameAccount(accountId: Long, newTitle: String): Result<Unit> {
        if (newTitle.isBlank()) {
            return Result.failure(IllegalArgumentException("عنوان حساب نمی‌تواند خالی باشد."))
        }
        accountRepository.updateAccountTitle(accountId, newTitle.trim())
        return Result.success(Unit)
    }

    suspend fun toggleAccountActive(accountId: Long, isActive: Boolean) {
        accountRepository.setAccountActive(accountId, isActive)
    }
}
