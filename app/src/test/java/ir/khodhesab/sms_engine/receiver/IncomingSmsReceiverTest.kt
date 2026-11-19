package ir.khodhesab.sms_engine.receiver

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [IncomingSmsReceiver].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class IncomingSmsReceiverTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    @Test
    fun onReceive_handlesNonSmsActionGracefully() {
        val receiver = IncomingSmsReceiver()
        val intent = Intent("INVALID_ACTION")
        receiver.onReceive(context, intent)

        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosByTag("ir.khodhesab.sms_engine.worker.ProcessIncomingSmsWorker").get()
        assertThat(workInfos).isEmpty()
    }
}
