package club.hiraeth.ionosonde.ui.kindex

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import club.hiraeth.ionosonde.data.model.KIndexEntry
import club.hiraeth.ionosonde.data.repository.IonosondeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KIndexViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = IonosondeRepository.getInstance(application)

    private val _entries = MutableStateFlow<List<KIndexEntry>>(emptyList())
    val entries: StateFlow<List<KIndexEntry>> = _entries.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeKIndexHistory().collect { data ->
                _entries.value = data
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.refreshKIndexHistory()
            } catch (_: Exception) {
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
