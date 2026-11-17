package ir.khodhesab.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.khodhesab.core.dispatcher.DefaultDispatcherProvider
import ir.khodhesab.core.dispatcher.DispatcherProvider
import javax.inject.Singleton

/**
 * Hilt module providing core application dependencies across the application.
 */
@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    /**
     * Provides the application-wide singleton instance of [DispatcherProvider].
     *
     * @return [DefaultDispatcherProvider] for production code execution.
     */
    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider {
        return DefaultDispatcherProvider()
    }
}
