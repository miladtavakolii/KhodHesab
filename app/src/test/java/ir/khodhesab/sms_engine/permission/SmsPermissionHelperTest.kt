package ir.khodhesab.sms_engine.permission

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [SmsPermissionHelper].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class SmsPermissionHelperTest {

    @Test
    fun requiredPermissions_includesReadAndReceiveSms() {
        val permissions = SmsPermissionHelper.requiredPermissions()
        assertThat(permissions).asList().containsAtLeast("android.permission.READ_SMS", "android.permission.RECEIVE_SMS")
    }

    @Test
    fun createAppSettingsIntent_targetsAppPackageName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = SmsPermissionHelper.createAppSettingsIntent(context)

        assertThat(intent.action).isEqualTo("android.settings.APPLICATION_DETAILS_SETTINGS")
        assertThat(intent.data?.schemeSpecificPart).isEqualTo(context.packageName)
    }
}
