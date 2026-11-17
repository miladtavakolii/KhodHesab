package ir.khodhesab.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.khodhesab.core.dispatcher.DispatcherProvider
import ir.khodhesab.data.local.KhodHesabDatabase
import ir.khodhesab.data.local.dao.AccountDao
import ir.khodhesab.data.local.dao.BankDao
import ir.khodhesab.data.local.dao.CategoryDao
import ir.khodhesab.data.local.dao.TransactionDao
import ir.khodhesab.data.local.seed.DatabaseSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Hilt module providing Database and DAO dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides application-wide singleton instance of [KhodHesabDatabase].
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        databaseProvider: Provider<KhodHesabDatabase>,
        dispatcherProvider: DispatcherProvider
    ): KhodHesabDatabase {
        return Room.databaseBuilder(
            context,
            KhodHesabDatabase::class.java,
            "khodhesab.db"
        )
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(SupervisorJob() + dispatcherProvider.io).launch {
                    val database = databaseProvider.get()
                    database.bankDao().insertAll(DatabaseSeeder.DEFAULT_BANKS)
                    database.categoryDao().insertAll(DatabaseSeeder.DEFAULT_CATEGORIES)
                }
            }
        })
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideBankDao(db: KhodHesabDatabase): BankDao = db.bankDao()

    @Provides
    fun provideAccountDao(db: KhodHesabDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideCategoryDao(db: KhodHesabDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideTransactionDao(db: KhodHesabDatabase): TransactionDao = db.transactionDao()
}
