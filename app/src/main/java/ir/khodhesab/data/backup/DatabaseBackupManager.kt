package ir.khodhesab.data.backup

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import ir.khodhesab.core.date.JalaliCalendarHelper
import ir.khodhesab.data.local.KhodHesabDatabase
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

/**
 * Utility for exporting and restoring full SQLite database backups.
 */
class DatabaseBackupManager @Inject constructor(
    private val database: KhodHesabDatabase
) {

    companion object {
        const val SQLITE_HEADER = "SQLite format 3\u0000"
    }

    /**
     * Checkpoints WAL journal, copies the database file to cache directory, and returns a shareable [FileProvider] Uri.
     */
    fun exportDatabaseBackup(context: Context): Result<Uri> {
        return try {
            database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()

            val dbFile = context.getDatabasePath("khodhesab.db")
            require(dbFile.exists()) { "Database file does not exist." }

            val backupDir = File(context.cacheDir, "backups")
            if (!backupDir.exists()) backupDir.mkdirs()

            val jDate = JalaliCalendarHelper.fromEpochMillis(System.currentTimeMillis())
            val dateStr = "${jDate.year}_${jDate.month}_${jDate.day}"
            val backupFile = File(backupDir, "khodhesab_backup_$dateStr.db")

            dbFile.copyTo(backupFile, overwrite = true)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )
            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validates SQLite header magic bytes and restores database from the specified input stream URI.
     */
    fun restoreDatabaseBackup(context: Context, sourceUri: Uri): Result<Unit> {
        return try {
            val inputStream: InputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return Result.failure(IllegalArgumentException("Cannot open stream for Uri."))

            val headerBytes = ByteArray(16)
            val readBytes = inputStream.read(headerBytes)
            if (readBytes < 16 || String(headerBytes, Charsets.US_ASCII) != SQLITE_HEADER) {
                inputStream.close()
                return Result.failure(IllegalArgumentException("فایل انتخاب شده دیتابیس معتبر SQLite نیست."))
            }

            inputStream.close()

            val dbFile = context.getDatabasePath("khodhesab.db")
            val walFile = File(dbFile.path + "-wal")
            val shmFile = File(dbFile.path + "-shm")

            if (database.isOpen) {
                database.close()
            }

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
