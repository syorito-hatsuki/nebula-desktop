package dev.syoritohatsuki.nebuladesktop.ui.window.main.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection
import dev.syoritohatsuki.nebuladesktop.ui.TEXT_COLOR_SECONDARY
import dev.syoritohatsuki.nebuladesktop.ui.window.main.MainWindowViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun ContainerHeader(
    mainWindowViewModel: MainWindowViewModel,
    connection: NebulaConnection,
    statusFlows: Map<String, StateFlow<NebulaConnection.ConnectionStatus>>
) {
    val scope = rememberCoroutineScope()

    var editMode by remember { mutableStateOf(false) }
    var editedName by remember(connection.uuid, connection.name) {
        mutableStateOf(connection.name)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (editMode) {
                    Box(
                        modifier = Modifier.wrapContentHeight(Alignment.CenterVertically).border(
                            width = 1.dp, color = Color.White.copy(alpha = 0.5f), shape = RoundedCornerShape(4.dp)
                        ).padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        BasicTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.White
                            ),
                            cursorBrush = SolidColor(Color.White)
                        )
                    }

                    IconButton(onClick = {
                        mainWindowViewModel.updateConnectionName(
                            connection.uuid, editedName.trim().ifEmpty { connection.name })
                        editMode = false
                    }) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Save",
                            tint = Color.White
                        )
                    }

                    IconButton(onClick = {
                        editedName = connection.name
                        editMode = false
                    }) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cancel",
                            tint = Color.White
                        )
                    }
                } else {
                    Text(
                        text = connection.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    IconButton(onClick = { editMode = true }) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit name",
                            tint = Color.White
                        )
                    }
                }
            }

            Text(
                text = connection.configPath.toString(),
                color = TEXT_COLOR_SECONDARY,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row {
            ConnectButton(mainWindowViewModel, connection, statusFlows)
            ConnectionOptionsMenu(onRemove = {
                scope.launch {
                    mainWindowViewModel.deleteConnection(connection.uuid)
                }
            })
        }
    }
}