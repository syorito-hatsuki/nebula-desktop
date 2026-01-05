package dev.syoritohatsuki.nebuladesktop.ui.window.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection
import dev.syoritohatsuki.nebuladesktop.ui.TEXT_COLOR
import dev.syoritohatsuki.nebuladesktop.ui.TEXT_COLOR_SECONDARY
import dev.syoritohatsuki.nebuladesktop.ui.window.main.MainWindowViewModel
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ContainerHeader(
    mainWindowViewModel: MainWindowViewModel,
    connection: NebulaConnection,
    statusFlows: Map<String, StateFlow<NebulaConnection.ConnectionStatus>>
) {
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
            ConnectButton(mainWindowViewModel, connection, statusFlows)
        }
    }
}