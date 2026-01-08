package dev.syoritohatsuki.nebuladesktop.ui.window.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.syoritohatsuki.nebuladesktop.ui.CARD_BACKGROUND_COLOR
import dev.syoritohatsuki.nebuladesktop.ui.STOP_BUTTON_COLOR
import dev.syoritohatsuki.nebuladesktop.ui.TEXT_COLOR_SECONDARY

@Composable
fun ConnectionOptionsMenu(onRemove: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
        IconButton(onClick = { expanded = true }) {
            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options", tint = TEXT_COLOR_SECONDARY)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 0.dp, y = 4.dp),
            modifier = Modifier.background(CARD_BACKGROUND_COLOR)
        ) {
            DropdownMenuItem(text = { Text("Remove", color = STOP_BUTTON_COLOR) }, onClick = {
                expanded = false
                onRemove()
            }, leadingIcon = {
                Icon(Icons.Default.Delete, contentDescription = null, tint = STOP_BUTTON_COLOR)
            })
        }
    }
}