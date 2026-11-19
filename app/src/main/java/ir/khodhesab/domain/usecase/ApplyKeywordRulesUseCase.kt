package ir.khodhesab.domain.usecase

import ir.khodhesab.core.util.PersianTextNormalizer
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.CategoryRepository
import ir.khodhesab.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * UseCase that retroactively scans all uncategorized transactions and applies category keyword rules.
 */
class ApplyKeywordRulesUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(): Int {
        val allTransactions = transactionRepository.observeTransactions().first()
        val allCategories = categoryRepository.observeAllCategories().first()

        val uncategorized = allTransactions.filter { it.category == null && it.kind != TransactionKind.TRANSFER }
        if (uncategorized.isEmpty()) return 0

        var matchCount = 0

        for (tx in uncategorized) {
            val textToSearch = listOfNotNull(tx.description, tx.rawSmsBody).joinToString(" ")
            if (textToSearch.isBlank()) continue

            val normalizedSearch = PersianTextNormalizer.normalize(textToSearch)
            val matchingCategory = allCategories
                .filter { it.kind == tx.kind }
                .firstOrNull { cat ->
                    cat.keywords.any { kw ->
                        kw.isNotBlank() && normalizedSearch.contains(PersianTextNormalizer.normalize(kw), ignoreCase = true)
                    }
                }

            if (matchingCategory != null) {
                transactionRepository.updateTransactionDetails(
                    transactionId = tx.id,
                    categoryId = matchingCategory.id,
                    description = tx.description
                )
                matchCount++
            }
        }

        return matchCount
    }
}
