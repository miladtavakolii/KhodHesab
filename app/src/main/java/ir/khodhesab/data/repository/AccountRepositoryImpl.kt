package ir.khodhesab.data.repository

import ir.khodhesab.data.local.dao.AccountDao
import ir.khodhesab.data.local.dao.BankDao
import ir.khodhesab.data.local.entity.AccountEntity
import ir.khodhesab.data.mapper.toDomain
import ir.khodhesab.domain.model.Account
import ir.khodhesab.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data layer implementation of [AccountRepository].
 */
@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val bankDao: BankDao
) : AccountRepository {

    override fun observeAllAccounts(): Flow<List<Account>> {
        return accountDao.getAllAccountsWithBank().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeTotalActiveBalance(): Flow<Long> {
        return accountDao.getTotalActiveBalance().map { sum -> sum ?: 0L }
    }

    override suspend fun getOrCreateAccount(
        bankId: Long,
        accountIdentifier: String,
        defaultTitle: String
    ): Account {
        val existingEntity = accountDao.getAccountByBankAndIdentifier(bankId, accountIdentifier)
        val accountId = if (existingEntity != null) {
            existingEntity.id
        } else {
            val newEntity = AccountEntity(
                bankId = bankId,
                accountIdentifier = accountIdentifier,
                title = defaultTitle
            )
            val insertedId = accountDao.insertAccount(newEntity)
            if (insertedId > 0L) insertedId else accountDao.getAccountByBankAndIdentifier(bankId, accountIdentifier)?.id ?: 0L
        }

        val accountEntity = accountDao.getAccountById(accountId)!!
        val bankEntity = bankDao.getBankById(bankId)!!
        return Account(
            id = accountEntity.id,
            bank = bankEntity.toDomain(),
            accountIdentifier = accountEntity.accountIdentifier,
            title = accountEntity.title,
            currentBalance = accountEntity.currentBalance,
            lastBalanceUpdate = accountEntity.lastBalanceUpdate,
            isActive = accountEntity.isActive
        )
    }

    override suspend fun updateAccountTitle(accountId: Long, newTitle: String) {
        accountDao.updateAccountTitle(accountId, newTitle)
    }

    override suspend fun setAccountActive(accountId: Long, isActive: Boolean) {
        val account = accountDao.getAccountById(accountId) ?: return
        accountDao.updateAccount(account.copy(isActive = isActive))
    }

    override suspend fun updateBalanceIfNewer(accountId: Long, newBalance: Long, timestamp: Long) {
        accountDao.updateBalanceIfNewer(accountId, newBalance, timestamp)
    }
}
