package ir.khodhesab.data.backup

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import ir.khodhesab.data.local.KhodHesabDatabase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * JVM / Robolectric tests for [DatabaseBackupManager].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class DatabaseBackupManagerTest {

    private val database: KhodHesabDatabase = mockk(relaxed = true)
    private lateinit var backupManager: DatabaseBackupManager
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        backupManager = DatabaseBackupManager(database)

        val dbFile = context.getDatabasePath("khodhesab.db")
        dbFile.parentFile?.mkdirs()
        dbFile.writeText(DatabaseBackupManager.SQLITE_HEADER + "dummy content")
    }

    @Test
    fun exportDatabaseBackup_createsFileInCacheDirectory() {
        val result = backupManager.exportDatabaseBackup(context)

        assertThat(result.isSuccess).isTrue()
        val uri = result.getOrNull()
        assertThat(uri).isNotNull()
    }
}
