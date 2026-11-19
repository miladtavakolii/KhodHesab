package ir.khodhesab.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.khodhesab.data.repository.AccountRepositoryImpl
import ir.khodhesab.data.repository.BankRepositoryImpl
import ir.khodhesab.data.repository.CategoryRepositoryImpl
import ir.khodhesab.data.repository.TransactionRepositoryImpl
import ir.khodhesab.domain.repository.AccountRepository
import ir.khodhesab.domain.repository.BankRepository
import ir.khodhesab.domain.repository.CategoryRepository
import ir.khodhesab.domain.repository.TransactionRepository
import javax.inject.Singleton

/**
 * Hilt module binding domain repository interfaces to data layer implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBankRepository(impl: BankRepositoryImpl): BankRepository

    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository
}
