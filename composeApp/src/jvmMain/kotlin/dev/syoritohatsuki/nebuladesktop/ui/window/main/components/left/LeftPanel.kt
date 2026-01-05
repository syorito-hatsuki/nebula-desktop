package dev.syoritohatsuki.nebuladesktop.ui.window.main.components.left

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection
import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection.ConnectionStatus
import dev.syoritohatsuki.nebuladesktop.runOnSwing
import dev.syoritohatsuki.nebuladesktop.ui.ADD_BUTTON_COLOR
import dev.syoritohatsuki.nebuladesktop.ui.SIDEBAR_BACKGROUND_COLOR
import dev.syoritohatsuki.nebuladesktop.ui.TEXT_COLOR
import dev.syoritohatsuki.nebuladesktop.ui.window.main.MainWindowViewModel
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun LeftPanel(
    mainWindowViewModel: MainWindowViewModel,
    connections: List<NebulaConnection>,
    statusFlows: Map<String, StateFlow<ConnectionStatus>>,
    selectedConnection: NebulaConnection?,
    onSelected: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    val filePicker = rememberFilePickerLauncher(
        title = "Select Nebula Config", type = FileKitType.File(extensions = listOf("yml", "yaml"))
    ) { file ->
        file?.let {
            mainWindowViewModel.addConnection(it.file)
        }
    }

    Column(
        modifier = Modifier.fillMaxHeight().fillMaxWidth(0.3f).background(SIDEBAR_BACKGROUND_COLOR).padding(8.dp)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
            items(connections) { connection ->
                ConnectionEntry(statusFlows, connection, selectedConnection) {
                    onSelected(it)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ADD_BUTTON_COLOR),
            onClick = {
                scope.launch {
                    runOnSwing(filePicker::launch)
                }
            },
        ) {
            Text(text = "Add Connection", color = TEXT_COLOR)
        }

        UpdateAvailableText()
    }
}