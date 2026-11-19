package ir.khodhesab.sms_engine.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import ir.khodhesab.domain.model.Bank
import ir.khodhesab.domain.repository.BankRepository
import ir.khodhesab.sms_engine.SmsProcessResult
import ir.khodhesab.sms_engine.SmsProcessingCoordinator
import ir.khodhesab.sms_engine.notification.TransactionNotificationManager
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [ProcessIncomingSmsWorker].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class ProcessIncomingSmsWorkerTest {

    private val coordinator: SmsProcessingCoordinator = mockk()
    private val notificationManager: TransactionNotificationManager = mockk(relaxed = true)
    private val bankRepository: BankRepository = mockk()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun doWork_processesSmsAndShowsNotificationWhenRecorded() = runTest {
        val inputData = workDataOf(
            ProcessIncomingSmsWorker.KEY_SENDER to "200033",
            ProcessIncomingSmsWorker.KEY_BODY to "برداشت 100000",
            ProcessIncomingSmsWorker.KEY_TIMESTAMP to 1000L
        )

        val workerParams = mockk<WorkerParameters>(relaxed = true)
        every { workerParams.inputData } returns inputData
        every { workerParams.runAttemptCount } returns 0

        val bank = Bank(id = 1, code = "MELLAT", persianName = "بانک ملت", colorHex = "#R", iconKey = "i")
        coEvery { coordinator.processSms(any()) } returns SmsProcessResult.Recorded(10L, "MELLAT", 100_000L)
        coEvery { bankRepository.getBankByCode("MELLAT") } returns bank

        val worker = ProcessIncomingSmsWorker(
            context = context,
            workerParams = workerParams,
            coordinator = coordinator,
            notificationManager = notificationManager,
            bankRepository = bankRepository
        )

        val result = worker.doWork()
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify(exactly = 1) { notificationManager.showTransactionNotification(10L, "بانک ملت", any(), 100_000L) }
    }
}
