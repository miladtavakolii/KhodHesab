package ir.khodhesab.sms_engine.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.khodhesab.core.util.PersianTextNormalizer
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.sms_engine.permission.SmsPermissionHelper
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager responsible for posting user notifications upon automatic recording of new bank SMS transactions.
 */
@Singleton
class TransactionNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_ID = "khodhesab_transactions"
        const val CHANNEL_NAME = "تراکنش‌های خودکار بانکی"
        const val ACTION_EDIT_TRANSACTION = "ir.khodhesab.ACTION_EDIT_TRANSACTION"
        const val EXTRA_TRANSACTION_ID = "EXTRA_TRANSACTION_ID"
    }

    init {
        createNotificationChannel()
    }

    /**
     * Creates high importance notification channel on Android 8.0+ (API 26+).
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "اعلان تراکنش‌های دریافت شده از پیامک‌های بانکی"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    /**
     * Displays a notification for a newly recorded bank transaction.
     *
     * @param transactionId Database ID of the created transaction.
     * @param bankName Persian name of the bank (e.g. `"بانک ملت"`).
     * @param kind Transaction kind ([TransactionKind.DEPOSIT] or [TransactionKind.WITHDRAWAL]).
     * @param amountRials Transaction amount in Rials.
     * @param balanceAfterRials Remaining balance in Rials, if available.
     */
    fun showTransactionNotification(
        transactionId: Long,
        bankName: String,
        kind: TransactionKind,
        amountRials: Long,
        balanceAfterRials: Long? = null
    ) {
        if (!SmsPermissionHelper.hasNotificationPermission(context)) {
            return
        }

        val titleKind = when (kind) {
            TransactionKind.DEPOSIT -> "واریز به"
            TransactionKind.WITHDRAWAL -> "برداشت از"
            TransactionKind.TRANSFER -> "انتقال در"
        }
        val title = "$titleKind $bankName"

        val amountStr = PersianTextNormalizer.convertDigitsToEnglish("%1$,d".format(amountRials)).map { char ->
            if (char in '0'..'9') (char + 1728) else char
        }.joinToString("")

        var contentText = "مبلغ: $amountStr ریال"
        if (balanceAfterRials != null) {
            val balanceStr = PersianTextNormalizer.convertDigitsToEnglish("%1$,d".format(balanceAfterRials)).map { char ->
                if (char in '0'..'9') (char + 1728) else char
            }.joinToString("")
            contentText += " | مانده: $balanceStr ریال"
        }
        contentText += "\nبرای انتخاب دسته‌بندی و توضیحات لمس کنید."

        val intent = Intent(context, Class.forName("ir.khodhesab.MainActivity")).apply {
            action = ACTION_EDIT_TRANSACTION
            putExtra(EXTRA_TRANSACTION_ID, transactionId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            transactionId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(transactionId.toInt(), notification)
        } catch (e: SecurityException) {
            // Notification permission revoked at runtime
        }
    }
}
