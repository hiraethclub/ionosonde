package club.hiraeth.ionosonde.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import club.hiraeth.ionosonde.data.model.SolarData
import club.hiraeth.ionosonde.data.repository.IonosondeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = IonosondeRepository.getInstance(application)

    private val _solarData = MutableStateFlow<SolarData?>(null)
    val solarData: StateFlow<SolarData?> = _solarData.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeSolarData().collect { data ->
                _solarData.value = data
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.refreshSolarData()
            } catch (_: Exception) {
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
