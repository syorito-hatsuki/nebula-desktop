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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ApplicationScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kdroid.composetray.tray.api.Tray
import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection
import dev.syoritohatsuki.nebuladesktop.ui.main.MainWindowViewModel
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher

@Composable
fun ApplicationScope.MainTray(mainWindowViewModel: MainWindowViewModel = viewModel(), openWindow: () -> Unit) {

    val connections by mainWindowViewModel.connections.collectAsState()

    val filePicker = rememberFilePickerLauncher(
        title = "Select Nebula Config", type = FileKitType.File(extensions = listOf("yml", "yaml"))
    ) { platformFile ->
        println(platformFile?.file?.readText())
    }

    Tray(
        iconContent = {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                val anyConnected = connections.any {
                    it.status.value == NebulaConnection.ConnectionStatus.ON
                }

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
                        text = if (connections.size > 9) "+" else connections.size.toString(),
                        color = TEXT_COLOR,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
        primaryAction = { openWindow() },
        tooltip = "Active connections: ${connections.size}",
    ) {
        connections.forEach {
            CheckableItem(
                label = it.name,
                checked = it.status.value == NebulaConnection.ConnectionStatus.ON,
                onCheckedChange = { isChecked ->
                    when {
                        isChecked -> mainWindowViewModel.stopConnection(it.name)
                        else -> mainWindowViewModel.startConnection(it.name)
                    }
                })
        }
        Divider()
        Item("Open", icon = Icons.AutoMirrored.Filled.OpenInNew) { openWindow() }
        Item("Add Config", icon = Icons.Filled.Add) { filePicker.launch() }
        Item("Quit", icon = Icons.AutoMirrored.Filled.ExitToApp) { exitApplication() }
    }
}