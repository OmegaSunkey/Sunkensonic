package omega.sunkey.sunkensonic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import omega.sunkey.sunkensonic.SunkensonicApplication
import omega.sunkey.sunkensonic.settings.SettingsRepo

class SettingsViewModel(private val settingsRepo: SettingsRepo) : ViewModel() {
    val port = settingsRepo.port()
    val blacklistedFolders = settingsRepo.dirBlacklist()
    val lastFmKey = settingsRepo.lastFmKey()
    fun writePort(value: Int) {
        viewModelScope.launch {
            settingsRepo.port.update { port ->
                port.copy(port = value)
            }
        }
    }

    fun writeBlacklist(value: List<String>) {
        viewModelScope.launch {
            settingsRepo.dirBlacklist.update { dir ->
                dir.copy(dirBlacklist = value)
            }
        }
    }
    fun writeLastFmKey(value: String) {
        viewModelScope.launch {
            settingsRepo.lastFmKey.update { key ->
                key.copy(lastFmKey = value)
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