package ir.khodhesab.sms_engine.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.BankRepository
import ir.khodhesab.sms_engine.SmsProcessResult
import ir.khodhesab.sms_engine.SmsProcessingCoordinator
import ir.khodhesab.sms_engine.notification.TransactionNotificationManager
import ir.khodhesab.sms_engine.parser.model.RawSmsMessage

/**
 * Hilt-injectable WorkManager CoroutineWorker for asynchronous processing of incoming bank SMS messages.
 */
@HiltWorker
class ProcessIncomingSmsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val coordinator: SmsProcessingCoordinator,
    private val notificationManager: TransactionNotificationManager,
    private val bankRepository: BankRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_SENDER = "KEY_SENDER"
        const val KEY_BODY = "KEY_BODY"
        const val KEY_TIMESTAMP = "KEY_TIMESTAMP"
    }

    override suspend fun doWork(): Result {
        val sender = inputData.getString(KEY_SENDER) ?: return Result.failure()
        val body = inputData.getString(KEY_BODY) ?: return Result.failure()
        val timestamp = inputData.getLong(KEY_TIMESTAMP, System.currentTimeMillis())

        val rawSms = RawSmsMessage(sender = sender, body = body, timestamp = timestamp)

        return try {
            when (val processResult = coordinator.processSms(rawSms)) {
                is SmsProcessResult.Recorded -> {
                    val bank = bankRepository.getBankByCode(processResult.bankCode)
                    val bankName = bank?.persianName ?: processResult.bankCode
                    notificationManager.showTransactionNotification(
                        transactionId = processResult.transactionId,
                        bankName = bankName,
                        kind = TransactionKind.WITHDRAWAL,
                        amountRials = processResult.amount
                    )
                    Result.success()
                }
                is SmsProcessResult.Duplicate, is SmsProcessResult.Ignored, is SmsProcessResult.Unrecognized -> {
                    Result.success()
                }
                is SmsProcessResult.Failed -> {
                    if (runAttemptCount < 3) Result.retry() else Result.failure()
                }
            }
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
