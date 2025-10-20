package dev.syoritohatsuki.nebuladesktop.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection.ConnectionStatus
import dev.syoritohatsuki.nebuladesktop.runOnSwing
import dev.syoritohatsuki.nebuladesktop.ui.*
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun MainWindow(mainWindowViewModel: MainWindowViewModel) {
    var selectedName by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val connections by mainWindowViewModel.connections.collectAsState()
    val statusFlows by mainWindowViewModel.statusFlows.collectAsState()

    val selectedConnection = remember(connections, selectedName) {
        connections.firstOrNull { it.name == selectedName } ?: connections.firstOrNull()
    }

    val logLines = mainWindowViewModel.logLines

    val filePicker = rememberFilePickerLauncher(
        title = "Select Nebula Config", type = FileKitType.File(extensions = listOf("yml", "yaml"))
    ) { file ->
        file?.let {
            mainWindowViewModel.addConnection(it.file)
        }
    }


    LaunchedEffect(selectedConnection?.name) {
        selectedConnection?.let {
            mainWindowViewModel.preloadLogs(it)
            mainWindowViewModel.observeLogs(it)
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxHeight().fillMaxWidth(0.3f).background(SIDEBAR_BACKGROUND_COLOR).padding(8.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
                items(connections) { conn ->
                    val statusFlow = statusFlows[conn.name]
                    val status by (statusFlow ?: MutableStateFlow(ConnectionStatus.DISABLED)).collectAsState()
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(12.dp))
                            .clickable { selectedName = conn.name }, colors = CardDefaults.cardColors(
                            containerColor = when {
                                selectedConnection == conn -> CARD_SELECTED_BACKGROUND_COLOR
                                else -> CARD_BACKGROUND_COLOR
                            }
                        ), shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = conn.name,
                                    color = TEXT_COLOR,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = conn.configPath.toString(),
                                    color = TEXT_COLOR_SECONDARY,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Box(modifier = Modifier.width(24.dp).height(24.dp).padding(start = 4.dp)) {
                                Icon(
                                    modifier = Modifier.fillMaxSize(),
                                    imageVector = Icons.Filled.Shield,
                                    contentDescription = null,
                                    tint = when (status) {
                                        ConnectionStatus.ENABLED -> ENABLED_ICON_COLOR
                                        ConnectionStatus.DISABLED -> DISABLED_ICON_COLOR
                                        ConnectionStatus.STOPPING -> AWAIT_ICON_COLOR
                                        ConnectionStatus.STARTING -> AWAIT_ICON_COLOR
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        runOnSwing {
                            filePicker.launch()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ADD_BUTTON_COLOR)
            ) {
                Text("Add Connection", color = TEXT_COLOR)
            }
        }

        Box(
            modifier = Modifier.fillMaxHeight().fillMaxWidth().background(BACKGROUND_COLOR).padding(16.dp)
        ) {
            selectedConnection?.let { connection ->
                val statusFlow = statusFlows[connection.name]
                val status by (statusFlow ?: MutableStateFlow(ConnectionStatus.DISABLED)).collectAsState()

                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = connection.name,
                                color = TEXT_COLOR,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                fontSize = 20.sp,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = connection.configPath.toString(),
                                color = TEXT_COLOR_SECONDARY,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SelectionContainer(
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                            .background(LOGS_BACKGROUND_COLOR).border(1.dp, LOGS_BORDER_COLOR, RoundedCornerShape(8.dp))
                    ) {
                        LazyColumn(reverseLayout = true) {
                            items(logLines) {
                                Text(
                                    text = it,
                                    color = Color.Unspecified,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Select a connection", color = TEXT_COLOR_SECONDARY)
            }
        }
    }
}