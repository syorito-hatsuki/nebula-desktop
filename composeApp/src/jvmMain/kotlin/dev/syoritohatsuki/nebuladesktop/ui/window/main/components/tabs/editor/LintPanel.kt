package dev.syoritohatsuki.nebuladesktop.ui.window.main.components.tabs.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.syoritohatsuki.nebuladesktop.dto.LexResult
import dev.syoritohatsuki.nebuladesktop.ui.START_BUTTON_COLOR
import dev.syoritohatsuki.nebuladesktop.ui.STOP_BUTTON_COLOR

@Composable
fun LintPanel(yamlEditorViewModel: YamlEditorViewModel) {
    val errors by yamlEditorViewModel.errors.collectAsState()

    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF252526)).padding(8.dp)) {
        if (errors.isEmpty()) Text("No issues", color = START_BUTTON_COLOR)

        errors.forEach {
            Text(
                text = buildString {
                    append(it.message)
                    if (it.column != null || it.line != null) {
                        append(" (")
                        it.line?.let { l -> append("Ln $l,") }
                        it.column?.let { l -> append(" Col $l") }
                        append(")")
                    }
                }, color = when (it.severity) {
                    LexResult.LintError.Severity.ERROR -> STOP_BUTTON_COLOR
                    else -> START_BUTTON_COLOR
                }
            )
        }
    }
}
