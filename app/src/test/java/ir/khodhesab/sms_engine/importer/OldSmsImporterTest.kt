package ir.khodhesab.sms_engine.importer

import android.content.ContentResolver
import android.content.Context
import android.database.MatrixCursor
import android.provider.Telephony
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import ir.khodhesab.core.dispatcher.DefaultDispatcherProvider
import ir.khodhesab.sms_engine.SmsProcessResult
import ir.khodhesab.sms_engine.SmsProcessingCoordinator
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [OldSmsImporter].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class OldSmsImporterTest {

    private val coordinator: SmsProcessingCoordinator = mockk()
    private val dispatcherProvider = DefaultDispatcherProvider()

    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var importer: OldSmsImporter

    @Before
    fun setup() {
        val appCtx = ApplicationProvider.getApplicationContext<Context>()
        contentResolver = mockk()
        context = mockk()

        every { context.contentResolver } returns contentResolver
        every { context.applicationContext } returns appCtx

        importer = OldSmsImporter(context, coordinator, dispatcherProvider)
    }

    @Test
    fun importHistoricalSms_processesMessagesInChronologicalOrder() = runTest {
        val cursor = MatrixCursor(arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE))
        cursor.addRow(arrayOf<Any>("200033", "پیامک ۱", 1000L))
        cursor.addRow(arrayOf<Any>("9611", "پیامک ۲", 2000L))

        every {
            contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                any(),
                any(),
                any(),
                "${Telephony.Sms.DATE} ASC"
            )
        } returns cursor

        coEvery { coordinator.processSms(match { it.body == "پیامک ۱" }) } returns SmsProcessResult.Recorded(1L, "MELLAT", 100L)
        coEvery { coordinator.processSms(match { it.body == "پیامک ۲" }) } returns SmsProcessResult.Duplicate

        importer.importHistoricalSms().test {
            val initial = awaitItem()
            assertThat(initial.totalMessages).isEqualTo(2)

            val finalProgress = awaitItem()
            assertThat(finalProgress.totalMessages).isEqualTo(2)
            assertThat(finalProgress.recordedCount).isEqualTo(1)
            assertThat(finalProgress.duplicateCount).isEqualTo(1)
            assertThat(finalProgress.isCompleted).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun importHistoricalSms_handlesSecurityExceptionGracefully() = runTest {
        every {
            contentResolver.query(any(), any(), any(), any(), any())
        } throws SecurityException("Permission denied")

        importer.importHistoricalSms().test {
            val progress = awaitItem()
            assertThat(progress.isCompleted).isTrue()
            awaitComplete()
        }
    }
}
