package dev.syoritohatsuki.nebuladesktop.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Shield
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ApplicationScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kdroid.composetray.tray.api.Tray
import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection.ConnectionStatus
import dev.syoritohatsuki.nebuladesktop.runOnSwing
import dev.syoritohatsuki.nebuladesktop.ui.main.MainWindowViewModel
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun ApplicationScope.MainTray(mainWindowViewModel: MainWindowViewModel = viewModel(), openWindow: () -> Unit) {

    val scope = rememberCoroutineScope()

    val connections by mainWindowViewModel.connections.collectAsState()
    val statusFlows by mainWindowViewModel.statusFlows.collectAsState()

    val isEnabledByName: Map<String, Boolean> = connections.associate { conn ->
        val flow: StateFlow<ConnectionStatus> =
            statusFlows[conn.name] ?: MutableStateFlow(ConnectionStatus.DISABLED)
        val status by flow.collectAsState()
        conn.name to (status == ConnectionStatus.ENABLED)
    }

    val anyConnected = isEnabledByName.values.any { it }
    val activeCount = isEnabledByName.values.count { it }

    val filePicker = rememberFilePickerLauncher(
        title = "Select Nebula Config", type = FileKitType.File(extensions = listOf("yml", "yaml"))
    ) { platformFile ->
        platformFile?.let {
            mainWindowViewModel.addConnection(it.file)
        }
    }

    Tray(
        iconContent = {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = "VPN",
                    tint = when {
                        anyConnected -> ENABLED_ICON_COLOR
                        else -> DISABLED_ICON_COLOR
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (anyConnected) {
                    Text(
                        text = if (activeCount > 9) "+" else activeCount.toString(),
                        color = TEXT_COLOR,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
        primaryAction = { openWindow() },
        tooltip = "Nebula Desktop"
    ) {
        connections.forEach { conn ->
            CheckableItem(
                label = conn.name,
                checked = isEnabledByName[conn.name] == true,
                onCheckedChange = { isChecked ->
                    when {
                        isChecked -> mainWindowViewModel.startConnection(conn.configPath)
                        else -> mainWindowViewModel.stopConnection(conn.configPath)
                    }
                })
        }
        if (connections.isNotEmpty()) Divider()

        Item("Open", icon = Icons.AutoMirrored.Filled.OpenInNew) { openWindow() }
        Item("Add Config", icon = Icons.Filled.Add) {
            scope.launch {
                runOnSwing { filePicker.launch() }
            }
        }
        Item("Quit", icon = Icons.AutoMirrored.Filled.ExitToApp) { exitApplication() }
    }
}