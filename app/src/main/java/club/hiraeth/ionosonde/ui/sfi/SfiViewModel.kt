package club.hiraeth.ionosonde.ui.sfi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import club.hiraeth.ionosonde.data.model.SfiForecastEntry
import club.hiraeth.ionosonde.data.repository.IonosondeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SfiViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = IonosondeRepository.getInstance(application)

    private val _entries = MutableStateFlow<List<SfiForecastEntry>>(emptyList())
    val entries: StateFlow<List<SfiForecastEntry>> = _entries.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeSfiForecast().collect { data ->
                _entries.value = data
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.refreshSfiForecast()
            } catch (_: Exception) {
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
