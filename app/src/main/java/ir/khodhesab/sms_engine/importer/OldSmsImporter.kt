package ir.khodhesab.sms_engine.importer

import android.content.Context
import android.provider.Telephony
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.khodhesab.core.dispatcher.DispatcherProvider
import ir.khodhesab.sms_engine.SmsProcessResult
import ir.khodhesab.sms_engine.SmsProcessingCoordinator
import ir.khodhesab.sms_engine.parser.model.RawSmsMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scanner that imports historical bank SMS messages from Android Telephony Inbox.
 */
@Singleton
class OldSmsImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coordinator: SmsProcessingCoordinator,
    private val dispatcherProvider: DispatcherProvider
) {

    /**
     * Scans Telephony Inbox in chronological order (`DATE ASC`) and imports bank transactions.
     *
     * @param minTimestamp Optional filter timestamp in epoch milliseconds to scan only messages after this time.
     * @return [Flow] emitting progress updates.
     */
    fun importHistoricalSms(minTimestamp: Long? = null): Flow<SmsImportProgress> = flow {
        val projection = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        val selection = if (minTimestamp != null && minTimestamp > 0L) {
            "${Telephony.Sms.DATE} >= ?"
        } else {
            null
        }

        val selectionArgs = if (minTimestamp != null && minTimestamp > 0L) {
            arrayOf(minTimestamp.toString())
        } else {
            null
        }

        val sortOrder = "${Telephony.Sms.DATE} ASC"

        var totalCount = 0
        val messages = mutableListOf<RawSmsMessage>()

        try {
            context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val addressIdx = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = cursor.getColumnIndex(Telephony.Sms.DATE)

                while (cursor.moveToNext()) {
                    val address = if (addressIdx >= 0) cursor.getString(addressIdx) ?: "" else ""
                    val body = if (bodyIdx >= 0) cursor.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx >= 0) cursor.getLong(dateIdx) else 0L

                    if (address.isNotBlank() && body.isNotBlank()) {
                        messages.add(RawSmsMessage(sender = address, body = body, timestamp = date))
                    }
                }
            }
        } catch (e: SecurityException) {
            emit(SmsImportProgress(isCompleted = true))
            return@flow
        } catch (e: Exception) {
            emit(SmsImportProgress(isCompleted = true))
            return@flow
        }

        totalCount = messages.size
        if (totalCount == 0) {
            emit(SmsImportProgress(totalMessages = 0, isCompleted = true))
            return@flow
        }

        var processed = 0
        var recorded = 0
        var duplicate = 0
        var ignoredOrUnrecognized = 0

        emit(SmsImportProgress(totalMessages = totalCount, processedCount = 0))

        for (rawSms in messages) {
            when (coordinator.processSms(rawSms)) {
                is SmsProcessResult.Recorded -> recorded++
                is SmsProcessResult.Duplicate -> duplicate++
                is SmsProcessResult.Ignored, is SmsProcessResult.Unrecognized, is SmsProcessResult.Failed -> ignoredOrUnrecognized++
            }
            processed++

            if (processed % 20 == 0 || processed == totalCount) {
                emit(
                    SmsImportProgress(
                        totalMessages = totalCount,
                        processedCount = processed,
                        recordedCount = recorded,
                        duplicateCount = duplicate,
                        ignoredOrUnrecognizedCount = ignoredOrUnrecognized,
                        isCompleted = processed == totalCount
                    )
                )
            }
        }
    }.flowOn(dispatcherProvider.io)
}
