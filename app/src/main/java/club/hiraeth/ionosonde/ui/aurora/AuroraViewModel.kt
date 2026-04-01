package club.hiraeth.ionosonde.ui.aurora

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import club.hiraeth.ionosonde.data.model.KIndexForecastEntry
import club.hiraeth.ionosonde.data.model.SolarData
import club.hiraeth.ionosonde.data.repository.IonosondeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuroraViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = IonosondeRepository.getInstance(application)

    private val _solarData = MutableStateFlow<SolarData?>(null)
    val solarData: StateFlow<SolarData?> = _solarData.asStateFlow()

    private val _forecast = MutableStateFlow<List<KIndexForecastEntry>>(emptyList())
    val forecast: StateFlow<List<KIndexForecastEntry>> = _forecast.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeSolarData().collect { _solarData.value = it }
        }
        viewModelScope.launch {
            repository.observeKIndexForecast().collect { _forecast.value = it }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.refreshKIndexForecast()
                repository.refreshSolarData()
            } catch (_: Exception) {
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
