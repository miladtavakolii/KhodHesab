package ir.khodhesab.sms_engine.notification

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import ir.khodhesab.domain.model.TransactionKind
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Unit tests for [TransactionNotificationManager].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class TransactionNotificationManagerTest {

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    private lateinit var manager: TransactionNotificationManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        shadowOf(context as Application).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager = TransactionNotificationManager(context)
    }

    @Test
    fun createNotificationChannel_registersChannelWithHighImportance() {
        val channel = notificationManager.getNotificationChannel(TransactionNotificationManager.CHANNEL_ID)
        assertThat(channel).isNotNull()
        assertThat(channel.name.toString()).isEqualTo(TransactionNotificationManager.CHANNEL_NAME)
        assertThat(channel.importance).isEqualTo(NotificationManager.IMPORTANCE_HIGH)
    }

    @Test
    fun showTransactionNotification_postsNotificationWithCorrectPendingIntentExtra() {
        manager.showTransactionNotification(
            transactionId = 42L,
            bankName = "بانک ملت",
            kind = TransactionKind.WITHDRAWAL,
            amountRials = 250_000L,
            balanceAfterRials = 1_000_000L
        )

        val shadowManager = shadowOf(notificationManager)
        val notifications = shadowManager.allNotifications

        assertThat(notifications).hasSize(1)
        val notification = notifications.first()
        val shadowNotification = shadowOf(notification)

        assertThat(shadowNotification.contentTitle.toString()).contains("برداشت از بانک ملت")

        val pendingIntent = notification.contentIntent
        val shadowPendingIntent = shadowOf(pendingIntent)
        val savedIntent = shadowPendingIntent.savedIntent

        assertThat(savedIntent.action).isEqualTo(TransactionNotificationManager.ACTION_EDIT_TRANSACTION)
        assertThat(savedIntent.getLongExtra(TransactionNotificationManager.EXTRA_TRANSACTION_ID, -1L)).isEqualTo(42L)
    }
}
