package dev.syoritohatsuki.nebuladesktop.ui.window.main.components.tabs.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.syoritohatsuki.nebuladesktop.ui.CARD_BACKGROUND_COLOR
import dev.syoritohatsuki.nebuladesktop.ui.ThemeManager

@Composable
fun ThemeSelector(
    modifier: Modifier = Modifier, options: List<String>, selected: String, onSelect: (String) -> Unit
) {
    val theme = ThemeManager.appTheme.value
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.clickable { expanded = true }.padding(8.dp), contentAlignment = Alignment.Center) {
        Text(
            text = "Theme: $selected " + when {
                expanded -> "▲"
                else -> "▼"
            },
            color = theme.textPrimary,
        )

        DropdownMenu(
            offset = DpOffset(x = 42.dp, y = 0.dp),
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = CARD_BACKGROUND_COLOR
        ) {
            DisableSelection {
                options.forEach { option ->
                    Text(
                        text = option, color = theme.textPrimary, modifier = Modifier.fillMaxWidth().clickable {
                            onSelect(option)
                            expanded = false
                        }.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}