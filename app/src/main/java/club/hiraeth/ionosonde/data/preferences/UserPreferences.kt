package club.hiraeth.ionosonde.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ionosonde_prefs")

data class AppSettings(
    val refreshIntervalMinutes: Int = 15,
    val kIndexAlertLevel: Int = 4,
    val flareClassAlert: String = "M",
    val protonEventAlert: Boolean = true,
    val widgetBgColor: Long = 0xFF000000,
    val widgetBgAlpha: Int = 60,
    val widgetTextColor: Long = 0xFFFFFFFF,
    val useFixedConditionColors: Boolean = true,
    val widgetAccentColor: Long = 0xFF4CAF50,
    val timeFormatUtcOnly: Boolean = true,
    val themeMode: String = "system", // "light", "dark", "system"
)

class UserPreferences private constructor(private val context: Context) {
    private val store = context.dataStore

    companion object {
        private val KEY_REFRESH_INTERVAL = intPreferencesKey("refresh_interval")
        private val KEY_K_INDEX_ALERT = intPreferencesKey("k_index_alert")
        private val KEY_FLARE_ALERT = stringPreferencesKey("flare_alert")
        private val KEY_PROTON_ALERT = booleanPreferencesKey("proton_alert")
        private val KEY_WIDGET_BG_COLOR = longPreferencesKey("widget_bg_color")
        private val KEY_WIDGET_BG_ALPHA = intPreferencesKey("widget_bg_alpha")
        private val KEY_WIDGET_TEXT_COLOR = longPreferencesKey("widget_text_color")
        private val KEY_USE_FIXED_COLORS = booleanPreferencesKey("use_fixed_colors")
        private val KEY_WIDGET_ACCENT = longPreferencesKey("widget_accent")
        private val KEY_TIME_UTC_ONLY = booleanPreferencesKey("time_utc_only")
        private val KEY_THEME = stringPreferencesKey("theme_mode")

        @Volatile
        private var INSTANCE: UserPreferences? = null

        fun getInstance(context: Context): UserPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun observeSettings(): Flow<AppSettings> = store.data.map { prefs ->
        AppSettings(
            refreshIntervalMinutes = prefs[KEY_REFRESH_INTERVAL] ?: 15,
            kIndexAlertLevel = prefs[KEY_K_INDEX_ALERT] ?: 4,
            flareClassAlert = prefs[KEY_FLARE_ALERT] ?: "M",
            protonEventAlert = prefs[KEY_PROTON_ALERT] ?: true,
            widgetBgColor = prefs[KEY_WIDGET_BG_COLOR] ?: 0xFF000000,
            widgetBgAlpha = prefs[KEY_WIDGET_BG_ALPHA] ?: 60,
            widgetTextColor = prefs[KEY_WIDGET_TEXT_COLOR] ?: 0xFFFFFFFF,
            useFixedConditionColors = prefs[KEY_USE_FIXED_COLORS] ?: true,
            widgetAccentColor = prefs[KEY_WIDGET_ACCENT] ?: 0xFF4CAF50,
            timeFormatUtcOnly = prefs[KEY_TIME_UTC_ONLY] ?: true,
            themeMode = prefs[KEY_THEME] ?: "system",
        )
    }

    suspend fun getSettings(): AppSettings = observeSettings().first()

    suspend fun updateRefreshInterval(minutes: Int) {
        store.edit { it[KEY_REFRESH_INTERVAL] = minutes }
    }

    suspend fun updateKIndexAlert(level: Int) {
        store.edit { it[KEY_K_INDEX_ALERT] = level }
    }

    suspend fun updateFlareAlert(flareClass: String) {
        store.edit { it[KEY_FLARE_ALERT] = flareClass }
    }

    suspend fun updateProtonAlert(enabled: Boolean) {
        store.edit { it[KEY_PROTON_ALERT] = enabled }
    }

    suspend fun updateWidgetBgColor(color: Long) {
        store.edit { it[KEY_WIDGET_BG_COLOR] = color }
    }

    suspend fun updateWidgetBgAlpha(alpha: Int) {
        store.edit { it[KEY_WIDGET_BG_ALPHA] = alpha }
    }

    suspend fun updateWidgetTextColor(color: Long) {
        store.edit { it[KEY_WIDGET_TEXT_COLOR] = color }
    }

    suspend fun updateUseFixedColors(use: Boolean) {
        store.edit { it[KEY_USE_FIXED_COLORS] = use }
    }

    suspend fun updateWidgetAccent(color: Long) {
        store.edit { it[KEY_WIDGET_ACCENT] = color }
    }

    suspend fun updateTimeFormat(utcOnly: Boolean) {
        store.edit { it[KEY_TIME_UTC_ONLY] = utcOnly }
    }

    suspend fun updateTheme(mode: String) {
        store.edit { it[KEY_THEME] = mode }
    }
}
