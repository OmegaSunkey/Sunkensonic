package omega.sunkey.sunkensonic.ui.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import omega.sunkey.sunkensonic.R
import omega.sunkey.sunkensonic.server.MediaScanner
import omega.sunkey.sunkensonic.server.ServerService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(modifier: Modifier = Modifier) {
    var started by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startService(context)
            started = true
        }
    } // i shall merge start and scan into one later

    var music by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                perm
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val tempReq = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { gra ->
        if (gra) {
            startScan(context)
            music = true
        }
    }

    val listItemColors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        trailingContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("SunkenSonic") },
                actions = {
                    // this can be used for settings
                    IconButton(
                        onClick = { /* something something */ }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.pets),
                            contentDescription = null
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
    ) { contentPadding ->
        // using listitem for now
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ListItem(
                onClick = {
                    if (!started && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else if (!started) {
                        startService(context)
                        started = true
                    }
                },
                colors = listItemColors,
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = 3),
                content = {
                    Text("Start server")
                },
                enabled = !started
            )

            ListItem(
                onClick = {
                    stopService(context)
                    started = false
                },
                colors = listItemColors,
                shapes = ListItemDefaults.segmentedShapes(index = 1, count = 3),
                content = {
                    Text("Stop server")
                },
                enabled = started
            )

            ListItem(
                onClick = {
                    if(music) {
                        startScan(context)
                    } else {
                        tempReq.launch(perm)
                    }
                },
                colors = listItemColors,
                shapes = ListItemDefaults.segmentedShapes(index = 2, count = 3),
                content = {
                    Text("Start scanning (needs storage permission)")
                }
            )
        }
    }
}

// maaybe these can be moved to a seperate utility/manager file later on
fun startScan(context: Context) {
    val req = OneTimeWorkRequestBuilder<MediaScanner>().build()
    WorkManager.getInstance(context).enqueueUniqueWork(
        "media_scan",
        ExistingWorkPolicy.REPLACE,
        req
    )
}
fun startService(context: Context) {
    val intent = Intent(context, ServerService::class.java)
    context.startForegroundService(intent)
}

fun stopService(context: Context) {
    val intent = Intent(context, ServerService::class.java)
    context.stopService(intent)
}