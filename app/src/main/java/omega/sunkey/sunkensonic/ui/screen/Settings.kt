package omega.sunkey.sunkensonic.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import omega.sunkey.sunkensonic.R
import omega.sunkey.sunkensonic.ui.viewmodel.SettingsViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Settings(onBack: () -> Unit, setViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)) {
    val port = setViewModel.port.collectAsStateWithLifecycle(initialValue = null)
    val portTextField = rememberTextFieldState()
    when {
        port.value != null -> LaunchedEffect(Unit) {
            portTextField.setTextAndPlaceCursorAtEnd(port.value.toString())
        }
    }
    val blacklistedFolders = setViewModel.blacklistedFolders.collectAsStateWithLifecycle(listOf())


    val showPortDialog = remember { mutableStateOf(false) }
    val showBlacklistDialog = remember { mutableStateOf(false) }

    when {
        showPortDialog.value -> {
            AlertDialog(
                onDismissRequest = { showPortDialog.value = false },
                title = { Text("Change port") },
                text = {
                    OutlinedTextField(
                        state = portTextField,
                        placeholder = { Text("A port number") }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            setViewModel.writePort(portTextField.text.toString().toInt())
                            showPortDialog.value = false
                        }
                    ) {
                        Text("Confirm")
                    }
                }
            )
        }
        showBlacklistDialog.value -> {
            BlacklistDialog(showBlacklistDialog, blacklistedFolders.value, setViewModel)
        }
    }

    val listItemColors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        trailingContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Go back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            ListItem(
                content = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_123),
                            contentDescription = "",
                            modifier = Modifier.padding(end = 8.dp).size(24.dp)
                        )
                        Text("Set the server's port")
                    }
                },
                onClick = {
                    showPortDialog.value = true
                },
                colors = listItemColors,
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = 2)
            )
            ListItem(
                content = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_folder_limited),
                            contentDescription = "",
                            modifier = Modifier.padding(end = 8.dp).size(24.dp)
                        )
                        Text("Blacklist folders from scan")
                    }
                },
                onClick = {
                    showBlacklistDialog.value = true
                },
                colors = listItemColors,
                shapes = ListItemDefaults.segmentedShapes(index = 1, count = 2)
            )
        }
    }
}

@Composable
fun BlacklistDialog(showBlacklistDialog: MutableState<Boolean>, blacklistedFolders: List<String>, setViewModel: SettingsViewModel) {
    val tempBlacklist = remember {
        mutableStateListOf<String>()
    }
    when {
        blacklistedFolders.isNotEmpty() -> LaunchedEffect(Unit) {
            tempBlacklist.addAll(blacklistedFolders)
        }
    }
    AlertDialog(
        onDismissRequest = {
            showBlacklistDialog.value = false
        },
        title = {Text("Blacklisted Folders")},
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(tempBlacklist) { index, folder ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = folder,
                            onValueChange = { new -> tempBlacklist[index] = new },
                            placeholder = { Text("/storage/emulated/0/...") },
                            modifier = Modifier.weight(4f, true)
                        )
                        IconButton(
                            onClick = { tempBlacklist.removeAt(index) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = "Delete blacklist element"
                            )
                        }
                    }
                }
                item {
                    TextButton(onClick = {tempBlacklist.add("")}, modifier = Modifier.fillMaxWidth()) {
                        Text("Add a folder")
                    }
                }
            } },
        confirmButton = {
            TextButton(onClick = { setViewModel.writeBlacklist(tempBlacklist); showBlacklistDialog.value = false }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = { showBlacklistDialog.value = false }) {
                Text("Cancel")
            }
        }
    )
}