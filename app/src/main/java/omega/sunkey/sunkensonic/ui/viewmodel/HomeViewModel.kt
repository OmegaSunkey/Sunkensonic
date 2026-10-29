package omega.sunkey.sunkensonic.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel() : ViewModel() {
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
}

sealed class Scan {
    object Idle : Scan()
    data class Scanning(val status: String, val progress: Int) : Scan()
    object Success : Scan()
    object Error : Scan()
}