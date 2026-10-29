package omega.sunkey.sunkensonic

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import omega.sunkey.sunkensonic.navigation.Screen
import omega.sunkey.sunkensonic.ui.screen.Home

@Composable
fun App() {
    val backStack = rememberNavBackStack(Screen.Home)
    val onBack = {
        if (backStack.size >= 2) {
            backStack.removeLastOrNull()
        }
    }

    val entryProvider: (NavKey) -> NavEntry<NavKey> = entryProvider {
        entry<Screen.Home> {
            Home(
                modifier = Modifier.fillMaxSize()
            )
        }

        // maybe some more screens later on
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    Scaffold { _ ->
        NavDisplay(
            backStack = backStack,
            entryProvider = entryProvider,
            onBack = onBack,
            sceneStrategies = remember { listOf(DialogSceneStrategy()) },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
            )
        )
    }
}