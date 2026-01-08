package dev.syoritohatsuki.nebuladesktop.ui.window.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import dev.syoritohatsuki.nebuladesktop.MIN_HEIGHT
import dev.syoritohatsuki.nebuladesktop.MIN_WIDTH
import dev.syoritohatsuki.nebuladesktop.ui.BACKGROUND_COLOR
import dev.syoritohatsuki.nebuladesktop.ui.TEXT_COLOR_SECONDARY
import dev.syoritohatsuki.nebuladesktop.ui.window.main.components.ContainerHeader
import dev.syoritohatsuki.nebuladesktop.ui.window.main.components.left.LeftPanel
import dev.syoritohatsuki.nebuladesktop.ui.window.main.components.tabs.LogsView
import dev.syoritohatsuki.nebuladesktop.ui.window.main.components.tabs.editor.EditorView
import nebula_desktop.composeapp.generated.resources.Res
import nebula_desktop.composeapp.generated.resources.icon
import org.jetbrains.compose.resources.painterResource
import java.awt.Dimension

@OptIn(ExperimentalMaterial3Api::class)
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

            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth().background(BACKGROUND_COLOR).padding(16.dp)) {
                selectedConnection?.let { connection ->
                    Column(modifier = Modifier.fillMaxSize()) {
                        ContainerHeader(mainWindowViewModel, connection, statusFlows)

                        var selectedDestination by rememberSaveable { mutableStateOf(ContainerTabs.LOGS) }

                        PrimaryScrollableTabRow(
                            selectedTabIndex = selectedDestination.ordinal,
                            modifier = Modifier.padding(vertical = 8.dp),
                            edgePadding = 0.dp,
                            contentColor = Color.White,
                            containerColor = Color.Transparent,
                            divider = {}
                        ) {
                            ContainerTabs.entries.forEachIndexed { index, tab ->
                                Tab(
                                    selected = selectedDestination.ordinal == index,
                                    onClick = {
                                        selectedDestination = tab
                                    },
                                    text = {
                                        Text(
                                            text = tab.name,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                )
                            }
                        }

                        when (selectedDestination) {
                            ContainerTabs.LOGS -> LogsView(mainWindowViewModel.logLines)
                            ContainerTabs.EDITOR -> EditorView(connection.configPath)
                        }
                    }
                } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select a connection", color = TEXT_COLOR_SECONDARY)
                }
            }
        }
    }
}

enum class ContainerTabs {
    LOGS,
    EDITOR,
}