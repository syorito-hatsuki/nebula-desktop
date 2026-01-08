package dev.syoritohatsuki.nebuladesktop.ui.window.main.components.tabs.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.syoritohatsuki.nebuladesktop.ui.LOGS_BACKGROUND_COLOR
import dev.syoritohatsuki.nebuladesktop.ui.LOGS_BORDER_COLOR
import java.nio.file.Path

@Composable
fun EditorView(configPath: Path) {
    val yamlEditorViewModel = remember { YamlEditorViewModel(configPath) }

    SelectionContainer(
        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)).background(LOGS_BACKGROUND_COLOR)
            .border(1.dp, LOGS_BORDER_COLOR, RoundedCornerShape(8.dp))
    ) {
        Column {
            YamlEditor(yamlEditorViewModel, Modifier.weight(1f))
            Divider(color = Color.DarkGray)
            LintPanel(emptyList())
        }
    }
}