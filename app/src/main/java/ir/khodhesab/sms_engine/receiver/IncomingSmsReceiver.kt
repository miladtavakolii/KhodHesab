package ir.khodhesab.sms_engine.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import ir.khodhesab.sms_engine.worker.ProcessIncomingSmsWorker

/**
 * Real-time BroadcastReceiver listening for incoming SMS messages (`SMS_RECEIVED`).
 * Concatenates multi-part SMS fragments and hands off background processing to [ProcessIncomingSmsWorker].
 */
@AndroidEntryPoint
class IncomingSmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val groupedMessages = messages.groupBy { it.originatingAddress ?: "UNKNOWN" }

        for ((sender, partList) in groupedMessages) {
            val fullBody = partList.joinToString("") { it.messageBody ?: "" }
            val timestamp = partList.firstOrNull()?.timestampMillis ?: System.currentTimeMillis()

            if (fullBody.isNotBlank()) {
                val inputData = Data.Builder()
                    .putString(ProcessIncomingSmsWorker.KEY_SENDER, sender)
                    .putString(ProcessIncomingSmsWorker.KEY_BODY, fullBody)
                    .putLong(ProcessIncomingSmsWorker.KEY_TIMESTAMP, timestamp)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<ProcessIncomingSmsWorker>()
                    .setInputData(inputData)
                    .build()

                WorkManager.getInstance(context).enqueue(workRequest)
            }
        }
    }
}
