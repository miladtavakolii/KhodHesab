package ir.khodhesab.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.khodhesab.domain.model.CurrencyUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Repository for managing user preference persistence via Jetpack DataStore Preferences.
 */
@Singleton
class AppPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val KEY_CURRENCY_UNIT = stringPreferencesKey("key_currency_unit")
        private val KEY_INITIAL_SMS_SYNC_DONE = booleanPreferencesKey("key_initial_sms_sync_done")
    }

    /**
     * Observes current preferred display [CurrencyUnit] (defaults to [CurrencyUnit.TOMAN]).
     */
    val currencyUnit: Flow<CurrencyUnit> = context.dataStore.data.map { preferences ->
        val rawUnit = preferences[KEY_CURRENCY_UNIT]
        if (rawUnit == CurrencyUnit.RIAL.name) CurrencyUnit.RIAL else CurrencyUnit.TOMAN
    }

    /**
     * Updates preferred display [CurrencyUnit].
     */
    suspend fun setCurrencyUnit(unit: CurrencyUnit) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CURRENCY_UNIT] = unit.name
        }
    }

    /**
     * Observes whether the initial historical SMS import has been completed.
     */
    val isInitialSmsSyncDone: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_INITIAL_SMS_SYNC_DONE] ?: false
    }

    /**
     * Sets the initial historical SMS import status.
     */
    suspend fun setInitialSmsSyncDone(done: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_INITIAL_SMS_SYNC_DONE] = done
        }
    }
}
