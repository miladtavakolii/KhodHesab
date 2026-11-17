package ir.khodhesab.data.local.converter

import androidx.room.TypeConverter
import ir.khodhesab.data.local.model.TransactionType

/**
 * Room type converters for non-primitive types stored in SQLite database.
 *
 * Handles [TransactionType] enum mapping and [List] of [String] conversion.
 */
object RoomTypeConverters {

    private const val DELIMITER = "|||"

    /**
     * Converts a string stored in database to [TransactionType].
     */
    @TypeConverter
    @JvmStatic
    fun toTransactionType(value: String?): TransactionType? {
        return value?.let {
            try {
                TransactionType.valueOf(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Converts [TransactionType] to string for database storage.
     */
    @TypeConverter
    @JvmStatic
    fun fromTransactionType(type: TransactionType?): String? {
        return type?.name
    }

    /**
     * Converts a delimiter-separated string to a [List] of [String].
     */
    @TypeConverter
    @JvmStatic
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(DELIMITER)
    }

    /**
     * Converts a [List] of [String] to a delimiter-separated string for storage.
     */
    @TypeConverter
    @JvmStatic
    fun fromStringList(list: List<String>?): String {
        if (list.isNullOrEmpty()) return ""
        return list.joinToString(DELIMITER)
    }
}
