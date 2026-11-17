package ir.khodhesab

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for KhodHesab (خودحساب).
 *
 * Serves as the application-level dependency injection container root annotated with [@HiltAndroidApp].
 */
@HiltAndroidApp
class KhodHesabApp : Application()
