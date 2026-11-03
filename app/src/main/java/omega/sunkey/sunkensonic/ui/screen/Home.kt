package omega.sunkey.sunkensonic.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import omega.sunkey.sunkensonic.R
import omega.sunkey.sunkensonic.ui.viewmodel.HomeViewModel
import omega.sunkey.sunkensonic.ui.viewmodel.Scan
import omega.sunkey.sunkensonic.ui.viewmodel.Server

@Composable
fun Home(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
    onSettings: () -> Unit
) {
    val context = LocalContext.current
    val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scanState by homeViewModel.getScanStateAsFlow(context).collectAsState()
    val serverState by homeViewModel.getServerStateAsFlow(context).collectAsState()
    val port by homeViewModel.port.collectAsState(4040)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            homeViewModel.startService(context, port)
        }
    }

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
            homeViewModel.startScan(context)
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
                        onClick = { onSettings() }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings),
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
                    when(serverState) {
                        is Server.Disabled -> {
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else homeViewModel.startService(context, port)
                        }
                        is Server.Enabled -> {
                            return@ListItem
                        }
                    }
                },
                colors = listItemColors,
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = 3),
                content = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_media_play),
                            contentDescription = "",
                            modifier = Modifier.padding(end = 8.dp).size(24.dp)
                        )
                        Text("Start server")
                    }
                },
                enabled = when(serverState) {
                    is Server.Disabled -> true
                    is Server.Enabled -> false
                }
            )

            ListItem(
                onClick = {
                    when(serverState) {
                        is Server.Disabled -> {
                            return@ListItem
                        }
                        is Server.Enabled -> {
                            homeViewModel.stopService(context)
                        }
                    }
                },
                colors = listItemColors,
                shapes = ListItemDefaults.segmentedShapes(index = 1, count = 3),
                content = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_stop),
                            contentDescription = "",
                            modifier = Modifier.padding(end = 8.dp).size(24.dp)
                        )
                        Text("Stop server")
                    }
                },
                enabled = when(serverState) {
                    is Server.Disabled -> false
                    is Server.Enabled -> true
                }
            )

            ListItem(
                onClick = {
                    if(music) {
                        homeViewModel.startScan(context)
                    } else {
                        tempReq.launch(perm)
                    }
                },
                colors = listItemColors,
                shapes = ListItemDefaults.segmentedShapes(index = 2, count = 3),
                content = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_folder_scan),
                            contentDescription = "",
                            modifier = Modifier.padding(end = 8.dp).size(24.dp)
                        )
                        Text("Start scanning")
                    }
                },
                enabled = when(scanState) {
                    is Scan.Scanning -> false
                    is Scan.Success, Scan.Error, Scan.Idle -> true
                }
            )
            Column(modifier = Modifier.padding(8.dp)) {
                when (val currentState = scanState) {
                    is Scan.Idle -> Text("")
                    is Scan.Scanning -> {
                        Text(currentState.status)
                        LinearProgressIndicator(
                            progress = { currentState.progress / 100f },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }
                    is Scan.Success -> Text("Finished scanning everything! You can start the server now.")
                    is Scan.Error -> Text("Oops! Something happening during scanning")
                }
            }
        }
    }
}