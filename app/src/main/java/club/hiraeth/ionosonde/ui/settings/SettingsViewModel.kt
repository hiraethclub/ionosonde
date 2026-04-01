package club.hiraeth.ionosonde.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import club.hiraeth.ionosonde.data.preferences.AppSettings
import club.hiraeth.ionosonde.data.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = UserPreferences.getInstance(application)

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.observeSettings().collect { _settings.value = it }
        }
    }

    fun updateRefreshInterval(minutes: Int) {
        viewModelScope.launch { prefs.updateRefreshInterval(minutes) }
    }

    fun updateKIndexAlert(level: Int) {
        viewModelScope.launch { prefs.updateKIndexAlert(level) }
    }

    fun updateFlareAlert(flareClass: String) {
        viewModelScope.launch { prefs.updateFlareAlert(flareClass) }
    }

    fun updateProtonAlert(enabled: Boolean) {
        viewModelScope.launch { prefs.updateProtonAlert(enabled) }
    }

    fun updateWidgetBgColor(color: Long) {
        viewModelScope.launch { prefs.updateWidgetBgColor(color) }
    }

    fun updateWidgetBgAlpha(alpha: Int) {
        viewModelScope.launch { prefs.updateWidgetBgAlpha(alpha) }
    }

    fun updateWidgetTextColor(color: Long) {
        viewModelScope.launch { prefs.updateWidgetTextColor(color) }
    }

    fun updateUseFixedColors(use: Boolean) {
        viewModelScope.launch { prefs.updateUseFixedColors(use) }
    }

    fun updateWidgetAccent(color: Long) {
        viewModelScope.launch { prefs.updateWidgetAccent(color) }
    }

    fun updateTimeFormat(utcOnly: Boolean) {
        viewModelScope.launch { prefs.updateTimeFormat(utcOnly) }
    }

    fun updateTheme(mode: String) {
        viewModelScope.launch { prefs.updateTheme(mode) }
    }
}
