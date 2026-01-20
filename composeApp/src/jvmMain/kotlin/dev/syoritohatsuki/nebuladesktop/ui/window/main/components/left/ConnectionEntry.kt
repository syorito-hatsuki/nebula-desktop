package dev.syoritohatsuki.nebuladesktop.ui.window.main.components.left

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection
import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection.ConnectionStatus
import dev.syoritohatsuki.nebuladesktop.ui.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ConnectionEntry(
    statusFlows: Map<String, StateFlow<ConnectionStatus>>,
    connection: NebulaConnection,
    selectedConnection: NebulaConnection?,
    onSelected: (String) -> Unit
) {
    val statusFlow = statusFlows[connection.uuid]
    val status by (statusFlow ?: MutableStateFlow(ConnectionStatus.DISABLED)).collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(12.dp))
            .clickable { onSelected(connection.uuid) }, colors = CardDefaults.cardColors(
            containerColor = when {
                selectedConnection == connection -> CARD_SELECTED_BACKGROUND_COLOR
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
                    text = connection.name,
                    color = TEXT_COLOR,
                    fontWeight = FontWeight.SemiBold,
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