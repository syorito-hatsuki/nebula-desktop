package dev.syoritohatsuki.nebuladesktop.ui.window.main.components.tabs.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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

@Composable
fun LintPanel(yamlEditorViewModel: YamlEditorViewModel) {
    val errors by yamlEditorViewModel.errors.collectAsState()

    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF252526)).padding(8.dp)) {
        if (errors.isEmpty()) Text("No issues", color = Color(0xFF6A9955))

        errors.forEach {
            Text(
                text = buildString {
                    append(it.severity)
                    append(": ")
                    append(it.message)
                    it.line?.let { l -> append(" (Ln $l,") }
                    it.column?.let { l -> append(" Col $l)") }
                }, color = when (it.severity) {
                    LexResult.LintError.Severity.ERROR -> Color(0xFFF44747)
                    else -> Color(0xFFFFC66D)
                }
            )
        }
    }
}
