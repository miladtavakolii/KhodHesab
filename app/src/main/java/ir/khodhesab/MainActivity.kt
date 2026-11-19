package ir.khodhesab

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.AndroidEntryPoint
import ir.khodhesab.sms_engine.notification.TransactionNotificationManager
import ir.khodhesab.ui.navigation.MainAppScaffold
import ir.khodhesab.ui.theme.KhodHesabTheme

/**
 * Main Activity entry point for KhodHesab application.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val editTransactionIdState = mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleDeepLinkIntent(intent)

        setContent {
            KhodHesabTheme {
                MainAppScaffold(
                    initialEditTransactionId = editTransactionIdState.value
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        if (intent?.action == TransactionNotificationManager.ACTION_EDIT_TRANSACTION) {
            val txId = intent.getLongExtra(TransactionNotificationManager.EXTRA_TRANSACTION_ID, -1L)
            if (txId > 0L) {
                editTransactionIdState.value = txId
            }
        }
    }
}
