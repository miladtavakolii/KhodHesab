package ir.khodhesab.sms_engine.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * Utility helper for managing runtime permissions (`READ_SMS`, `RECEIVE_SMS`, `POST_NOTIFICATIONS`)
 * and providing intents for Android 13+ restricted settings.
 */
object SmsPermissionHelper {

    /**
     * Checks if all required SMS permissions (`READ_SMS` and `RECEIVE_SMS`) are granted.
     *
     * @param context Application context.
     * @return `true` if both permissions are granted, `false` otherwise.
     */
    fun hasCoreSmsPermissions(context: Context): Boolean {
        val hasRead = context.checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        val hasReceive = context.checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        return hasRead && hasReceive
    }

    /**
     * Checks if `POST_NOTIFICATIONS` permission is granted (always true on SDK < 33).
     *
     * @param context Application context.
     * @return `true` if notification permission is granted or not required.
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Returns the array of required runtime permissions for SMS processing and notifications.
     */
    fun requiredPermissions(): Array<String> {
        val list = mutableListOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return list.toTypedArray()
    }

    /**
     * Checks if the device is running Android 13 (API level 33) or higher.
     */
    fun isAndroid13OrHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    /**
     * Creates an [Intent] to open App Details in System Settings.
     * Useful for guiding users on Android 13+ to enable "Allow restricted settings" for sideloaded APKs.
     *
     * @param context Application context.
     * @return [Intent] targeting application settings page.
     */
    fun createAppSettingsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
