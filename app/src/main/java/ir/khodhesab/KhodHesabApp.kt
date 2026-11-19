package ir.khodhesab

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class for KhodHesab (خودحساب).
 *
 * Serves as the application-level dependency injection container root annotated with [@HiltAndroidApp].
 * Implements [Configuration.Provider] to provide custom WorkManager initialization with [HiltWorkerFactory].
 */
@HiltAndroidApp
class KhodHesabApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
