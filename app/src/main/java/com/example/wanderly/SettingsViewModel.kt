import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(isDarkTheme: Boolean) : ViewModel() {
    private val _isDarkTheme = MutableStateFlow(isDarkTheme)
    val isDarkTheme: StateFlow<Boolean> get() = _isDarkTheme

    private val _highContrast = MutableStateFlow(false)
    val highContrast: StateFlow<Boolean> get() = _highContrast

    fun toggleDarkTheme() {
        viewModelScope.launch {
            _isDarkTheme.value = !_isDarkTheme.value
        }
    }

    fun toggleHighContrast() {
        viewModelScope.launch {
            _highContrast.value = !_highContrast.value
        }
    }
}