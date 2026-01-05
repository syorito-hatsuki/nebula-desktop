package dev.syoritohatsuki.nebuladesktop.ui.window.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.syoritohatsuki.nebuladesktop.BuildKonfig
import dev.syoritohatsuki.nebuladesktop.api.GithubApi
import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection
import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection.ConnectionStatus
import dev.syoritohatsuki.nebuladesktop.runOnSwing
import dev.syoritohatsuki.nebuladesktop.ui.*
import dev.syoritohatsuki.nebuladesktop.ui.window.main.MainWindowViewModel
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

@Composable
fun LeftPanel(
    mainWindowViewModel: MainWindowViewModel,
    connections: List<NebulaConnection>,
    statusFlows: Map<String, StateFlow<ConnectionStatus>>,
    selectedConnection: NebulaConnection?,
    onSelected: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    var remoteAppVersion by remember { mutableStateOf("") }

    val filePicker = rememberFilePickerLauncher(
        title = "Select Nebula Config", type = FileKitType.File(extensions = listOf("yml", "yaml"))
    ) { file ->
        file?.let {
            mainWindowViewModel.addConnection(it.file)
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            val latestTag = GithubApi.fetchLatestRelease("syorito-hatsuki/nebula-desktop").tagName
            remoteAppVersion = latestTag
        }
    }

    Column(
        modifier = Modifier.fillMaxHeight().fillMaxWidth(0.3f).background(SIDEBAR_BACKGROUND_COLOR).padding(8.dp)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
            items(connections) { conn ->
                val statusFlow = statusFlows[conn.name]
                val status by (statusFlow ?: MutableStateFlow(ConnectionStatus.DISABLED)).collectAsState()
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(12.dp))
                        .clickable { onSelected(conn.name) }, colors = CardDefaults.cardColors(
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
                        Box(modifier = Modifier.size(24.dp).padding(start = 4.dp)) {
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
            Text(text = "Add Connection", color = TEXT_COLOR)
        }
        if (BuildKonfig.appVersion == remoteAppVersion) {
            Text(
                text = "Build: ${BuildKonfig.appVersion}",
                color = TEXT_COLOR_SECONDARY,
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
                modifier = Modifier.padding(4.dp).fillMaxWidth()
            )
        } else {
            Text(
                text = "Update available :)",
                color = TEXT_COLOR,
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
                modifier = Modifier.padding(4.dp).fillMaxWidth().clickable {
                    val url = "https://github.com/syorito-hatsuki/nebula-desktop/releases"
                    try {
                        Runtime.getRuntime().exec(
                            when (hostOs) {
                                OS.Linux -> arrayOf("xdg-open", url)
                                OS.MacOS -> arrayOf("open", url)
                                OS.Windows -> arrayOf("rundll32", "url.dll,FileProtocolHandler", url)
                                else -> error("Unsupported OS: $hostOs")
                            }
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                })
        }
    }
}