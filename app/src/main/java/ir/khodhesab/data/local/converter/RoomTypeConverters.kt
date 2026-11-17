package ir.khodhesab.data.local.converter

import androidx.room.TypeConverter
import ir.khodhesab.data.local.model.TransactionType

/**
 * Room type converters for non-primitive types stored in SQLite database.
 *
 * Handles [TransactionType] enum mapping and [List] of [String] conversion.
 */
class RoomTypeConverters {

    private val delimiter = "|||"

    /**
     * Converts a string stored in database to [TransactionType].
     */
    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? {
        return value?.let {
            try {
                TransactionType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    /**
     * Converts [TransactionType] to string for database storage.
     */
    @TypeConverter
    fun fromTransactionType(type: TransactionType?): String? {
        return type?.name
    }

    /**
     * Converts a delimiter-separated string to a [List] of [String].
     */
    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(delimiter)
    }

    /**
     * Converts a [List] of [String] to a delimiter-separated string for storage.
     */
    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list.isNullOrEmpty()) return ""
        return list.joinToString(delimiter)
    }
}
