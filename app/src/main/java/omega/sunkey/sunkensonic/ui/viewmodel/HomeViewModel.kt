package omega.sunkey.sunkensonic.ui.viewmodel

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import omega.sunkey.sunkensonic.SunkensonicApplication
import omega.sunkey.sunkensonic.server.MediaScanner
import omega.sunkey.sunkensonic.server.ServerService
import omega.sunkey.sunkensonic.settings.Settings
import omega.sunkey.sunkensonic.settings.SettingsRepo

class HomeViewModel(private val settingsRepo: SettingsRepo) : ViewModel() {
    fun getScanStateAsFlow(context: Context): StateFlow<Scan> {
        val scanState = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow("media_scan")
            .map { workInfos ->
                val workInfo = workInfos.firstOrNull()

                when (workInfo?.state) {
                    WorkInfo.State.RUNNING -> {
                        val status = workInfo.progress.getString("status") ?: "Scanning..."
                        val progress = workInfo.progress.getInt("progress", 0)
                        Scan.Scanning(status, progress)
                    }
                    WorkInfo.State.SUCCEEDED -> Scan.Success
                    WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> Scan.Error
                    else -> Scan.Idle
                }
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, Scan.Idle)
        return scanState
    }

    // Source - https://stackoverflow.com/a/58404522
    // Posted by Oscar Emilio Perez Martinez, modified by community. See post 'Timeline' for change history
    // Retrieved 2026-10-30, License - CC BY-SA 4.0
    @Suppress("DEPRECATION")
    fun Context.isMyServiceRunning(serviceClass: Class<out Service>) = try {
        (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    } catch (_: Exception) {
        false
    }

    fun getServerStateAsFlow(context: Context): StateFlow<Server> {
        when(context.isMyServiceRunning(ServerService::class.java)) {
                true -> _serverState.value = Server.Enabled
                false -> _serverState.value = Server.Disabled
        }
        return serverState
    }

    private val _serverState = MutableStateFlow<Server>(Server.Disabled)
    val serverState: StateFlow<Server> = _serverState

    val port = runBlocking {
        settingsRepo.port.read()
    }

    fun startService(context: Context) {
        val intent = Intent(context, ServerService::class.java)
        intent.putExtra("PORT", port ?: 4040)
        context.startForegroundService(intent)
        _serverState.value = Server.Enabled
    }

    fun stopService(context: Context) {
        val intent = Intent(context, ServerService::class.java)
        context.stopService(intent)
        _serverState.value = Server.Disabled
    }

    fun startScan(context: Context) {
        val req = OneTimeWorkRequestBuilder<MediaScanner>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "media_scan",
            ExistingWorkPolicy.REPLACE,
            req
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel((this[APPLICATION_KEY] as SunkensonicApplication).settings)
            }
        }
    }
}

sealed class Scan {
    object Idle : Scan()
    data class Scanning(val status: String, val progress: Int) : Scan()
    object Success : Scan()
    object Error : Scan()
}

sealed class Server {
    object Disabled : Server()
    object Enabled : Server()
}