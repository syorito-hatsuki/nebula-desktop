package dev.syoritohatsuki.nebuladesktop.ui.window.main.components

import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection
import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection.ConnectionStatus
import dev.syoritohatsuki.nebuladesktop.ui.AWAIT_BUTTON_COLOR
import dev.syoritohatsuki.nebuladesktop.ui.START_BUTTON_COLOR
import dev.syoritohatsuki.nebuladesktop.ui.STOP_BUTTON_COLOR
import dev.syoritohatsuki.nebuladesktop.ui.TEXT_COLOR
import dev.syoritohatsuki.nebuladesktop.ui.window.main.MainWindowViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ConnectButton(
    mainWindowViewModel: MainWindowViewModel,
    connection: NebulaConnection,
    statusFlows: Map<String, StateFlow<ConnectionStatus>>
) {
    val statusFlow = statusFlows[connection.uuid]
    val status by (statusFlow ?: MutableStateFlow(ConnectionStatus.DISABLED)).collectAsState()

    when (status) {
        ConnectionStatus.DISABLED -> Button(
            onClick = { mainWindowViewModel.startConnection(connection.configPath) },
            colors = ButtonDefaults.buttonColors(containerColor = START_BUTTON_COLOR)
        ) { Text("Start", color = TEXT_COLOR) }

        ConnectionStatus.STARTING -> Button(
            onClick = {},
            enabled = false,
            colors = ButtonDefaults.buttonColors(disabledContainerColor = AWAIT_BUTTON_COLOR)
        ) { Text("Starting…", color = TEXT_COLOR) }

        ConnectionStatus.ENABLED -> Button(
            onClick = { mainWindowViewModel.stopConnection(connection.configPath) },
            colors = ButtonDefaults.buttonColors(containerColor = STOP_BUTTON_COLOR)
        ) { Text("Stop", color = TEXT_COLOR) }

        ConnectionStatus.STOPPING -> Button(
            enabled = false,
            onClick = {},
            colors = ButtonDefaults.buttonColors(disabledContainerColor = AWAIT_BUTTON_COLOR)
        ) { Text("Stopping…", color = TEXT_COLOR) }
    }
}