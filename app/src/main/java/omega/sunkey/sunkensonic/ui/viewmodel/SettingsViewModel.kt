package omega.sunkey.sunkensonic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import omega.sunkey.sunkensonic.SunkensonicApplication
import omega.sunkey.sunkensonic.settings.SettingsRepo

class SettingsViewModel(private val settingsRepo: SettingsRepo) : ViewModel() {
    val port = settingsRepo.port()

    fun writePort(value: Int) {
        viewModelScope.launch {
            settingsRepo.port.update { port ->
                port.copy(port = value)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel((this[APPLICATION_KEY] as SunkensonicApplication).settings)
            }
        }
    }
}