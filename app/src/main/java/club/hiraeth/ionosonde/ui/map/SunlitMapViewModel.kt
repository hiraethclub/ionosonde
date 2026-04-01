package club.hiraeth.ionosonde.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import club.hiraeth.ionosonde.data.model.SolarData
import club.hiraeth.ionosonde.data.repository.IonosondeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SunlitMapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = IonosondeRepository.getInstance(application)

    private val _solarData = MutableStateFlow<SolarData?>(null)
    val solarData: StateFlow<SolarData?> = _solarData.asStateFlow()

    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMillis: StateFlow<Long> = _currentTimeMillis.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeSolarData().collect { _solarData.value = it }
        }
        // Update terminator every 60 seconds
        viewModelScope.launch {
            while (isActive) {
                _currentTimeMillis.value = System.currentTimeMillis()
                delay(60_000L)
            }
        }
    }
}
