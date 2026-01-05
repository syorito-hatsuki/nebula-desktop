package dev.syoritohatsuki.nebuladesktop.ui.window.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import dev.syoritohatsuki.nebuladesktop.MIN_HEIGHT
import dev.syoritohatsuki.nebuladesktop.MIN_WIDTH
import dev.syoritohatsuki.nebuladesktop.ui.BACKGROUND_COLOR
import dev.syoritohatsuki.nebuladesktop.ui.TEXT_COLOR_SECONDARY
import dev.syoritohatsuki.nebuladesktop.ui.window.main.components.ContainerHeader
import dev.syoritohatsuki.nebuladesktop.ui.window.main.components.LeftPanel
import dev.syoritohatsuki.nebuladesktop.ui.window.main.components.LogsView
import nebula_desktop.composeapp.generated.resources.Res
import nebula_desktop.composeapp.generated.resources.icon
import org.jetbrains.compose.resources.painterResource
import java.awt.Dimension

@Composable
fun MainWindow(
    windowRef: (ComposeWindow) -> Unit,
    windowVisible: Boolean,
    onCloseRequest: () -> Unit,
    mainWindowViewModel: MainWindowViewModel
) {
    val connections by mainWindowViewModel.connections.collectAsState()
    val statusFlows by mainWindowViewModel.statusFlows.collectAsState()

    var selectedName by remember { mutableStateOf<String?>(null) }

    val selectedConnection = remember(connections, selectedName) {
        connections.firstOrNull { it.name == selectedName }
    }

    LaunchedEffect(selectedConnection?.name) {
        selectedConnection?.let {
            mainWindowViewModel.preloadLogs(it)
            mainWindowViewModel.observeLogs(it)
        }
    }

    Window(
        icon = painterResource(Res.drawable.icon),
        onCloseRequest = onCloseRequest,
        title = "Nebula Desktop",
        visible = windowVisible,
    ) {
        SideEffect {
            windowRef(window)
        }

        window.minimumSize = Dimension(MIN_WIDTH, MIN_HEIGHT)

        Row(modifier = Modifier.fillMaxSize()) {
            LeftPanel(mainWindowViewModel, connections, statusFlows, selectedConnection) {
                selectedName = it
            }

            Box(
                modifier = Modifier.fillMaxHeight().fillMaxWidth().background(BACKGROUND_COLOR).padding(16.dp)
            ) {
                selectedConnection?.let { connection ->
                    Column(modifier = Modifier.fillMaxSize()) {
                        ContainerHeader(mainWindowViewModel, connection, statusFlows)
                        Spacer(modifier = Modifier.height(16.dp))
                        LogsView(mainWindowViewModel.logLines)
                    }
                } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select a connection", color = TEXT_COLOR_SECONDARY)
                }
            }
        }
    }
}