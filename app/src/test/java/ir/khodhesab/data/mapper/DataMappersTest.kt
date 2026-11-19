package ir.khodhesab.data.mapper

import com.google.common.truth.Truth.assertThat
import ir.khodhesab.data.local.entity.AccountEntity
import ir.khodhesab.data.local.entity.BankEntity
import ir.khodhesab.data.local.entity.CategoryEntity
import ir.khodhesab.data.local.model.TransactionType
import ir.khodhesab.data.local.relation.AccountWithBank
import ir.khodhesab.data.local.relation.CategoryExpenseSummary
import ir.khodhesab.domain.model.TransactionKind
import org.junit.Test

/**
 * Unit tests for Entity <-> Domain mappers.
 */
class DataMappersTest {

    @Test
    fun bankMapper_mapsBidirectionallyWithoutDataLoss() {
        val entity = BankEntity(
            id = 1L,
            code = "MELLI",
            persianName = "بانک ملی",
            senderNumbers = listOf("9611"),
            colorHex = "#1E88E5",
            iconKey = "ic_bank_melli"
        )

        val domain = entity.toDomain()
        assertThat(domain.id).isEqualTo(1L)
        assertThat(domain.code).isEqualTo("MELLI")
        assertThat(domain.persianName).isEqualTo("بانک ملی")

        val reMappedEntity = domain.toEntity()
        assertThat(reMappedEntity).isEqualTo(entity)
    }

    @Test
    fun accountMapper_mapsRelationToDomainAndEntity() {
        val bankEntity = BankEntity(id = 2L, code = "BLU", persianName = "بلوبانک", senderNumbers = listOf("10008000"), colorHex = "#BLUE", iconKey = "blu")
        val accountEntity = AccountEntity(id = 10L, bankId = 2L, accountIdentifier = "1234", title = "بلو اصلی", currentBalance = 500_000L)
        val relation = AccountWithBank(account = accountEntity, bank = bankEntity)

        val domainAccount = relation.toDomain()
        assertThat(domainAccount.id).isEqualTo(10L)
        assertThat(domainAccount.bank.code).isEqualTo("BLU")
        assertThat(domainAccount.title).isEqualTo("بلو اصلی")

        val entityFromDomain = domainAccount.toEntity()
        assertThat(entityFromDomain.id).isEqualTo(10L)
        assertThat(entityFromDomain.bankId).isEqualTo(2L)
    }

    @Test
    fun categoryMapper_mapsEnumAndFieldsCorrectly() {
        val categoryEntity = CategoryEntity(
            id = 5L,
            name = "رستوران",
            type = TransactionType.WITHDRAWAL,
            iconKey = "food",
            colorHex = "#RED",
            isDefault = true,
            keywords = listOf("کافه", "غذا")
        )

        val domainCategory = categoryEntity.toDomain()
        assertThat(domainCategory.kind).isEqualTo(TransactionKind.WITHDRAWAL)
        assertThat(domainCategory.keywords).contains("غذا")

        val reMappedEntity = domainCategory.toEntity()
        assertThat(reMappedEntity).isEqualTo(categoryEntity)
    }

    @Test
    fun categoryExpenseSummaryMapper_computesPercentagesSafely() {
        val summary = CategoryExpenseSummary(
            categoryId = 1L,
            categoryName = "غذا",
            colorHex = "#RED",
            iconKey = "food",
            totalAmount = 250_000L,
            transactionCount = 5
        )

        val breakdown = summary.toDomain(totalPeriodAmount = 1_000_000L)
        assertThat(breakdown.category?.name).isEqualTo("غذا")
        assertThat(breakdown.percentage).isEqualTo(25.0f)

        // Zero total period amount -> 0% without division by zero exception
        val zeroBreakdown = summary.toDomain(totalPeriodAmount = 0L)
        assertThat(zeroBreakdown.percentage).isEqualTo(0.0f)
    }
}
