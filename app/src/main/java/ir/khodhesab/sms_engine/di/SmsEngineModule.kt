package ir.khodhesab.sms_engine.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import ir.khodhesab.sms_engine.parser.strategy.BankParserStrategy
import ir.khodhesab.sms_engine.parser.strategy.banks.BluSamanPasargadStrategy
import ir.khodhesab.sms_engine.parser.strategy.banks.MellatMelliStrategy
import ir.khodhesab.sms_engine.parser.strategy.banks.TejaratSaderatKeshavarziSepahStrategy
import javax.inject.Singleton

/**
 * Hilt module providing SMS engine dependencies and multi-bound parsing strategies.
 */
@Module
@InstallIn(SingletonComponent::class)
object SmsEngineModule {

    @Provides
    @Singleton
    @IntoSet
    fun provideMellatMelliStrategy(): BankParserStrategy = MellatMelliStrategy()

    @Provides
    @Singleton
    @IntoSet
    fun provideBluSamanPasargadStrategy(): BankParserStrategy = BluSamanPasargadStrategy()

    @Provides
    @Singleton
    @IntoSet
    fun provideTejaratSaderatKeshavarziSepahStrategy(): BankParserStrategy = TejaratSaderatKeshavarziSepahStrategy()
}
