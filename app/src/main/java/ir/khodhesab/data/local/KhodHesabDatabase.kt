package ir.khodhesab.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ir.khodhesab.data.local.converter.RoomTypeConverters
import ir.khodhesab.data.local.dao.AccountDao
import ir.khodhesab.data.local.dao.BankDao
import ir.khodhesab.data.local.dao.CategoryDao
import ir.khodhesab.data.local.dao.TransactionDao
import ir.khodhesab.data.local.entity.AccountEntity
import ir.khodhesab.data.local.entity.BankEntity
import ir.khodhesab.data.local.entity.CategoryEntity
import ir.khodhesab.data.local.entity.TransactionEntity

/**
 * Main Room Database for KhodHesab application.
 *
 * Stores banks, user accounts, transaction categories, and financial transactions.
 */
@Database(
    entities = [
        BankEntity::class,
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomTypeConverters::class)
abstract class KhodHesabDatabase : RoomDatabase() {

    abstract fun bankDao(): BankDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
}
