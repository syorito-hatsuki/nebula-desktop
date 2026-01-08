package dev.syoritohatsuki.nebuladesktop.ui.window.main.components.tabs.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.syoritohatsuki.nebuladesktop.dto.LintError

@Composable
fun LintPanel(errors: List<LintError>) {
    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF252526)).padding(8.dp)) {
        if (errors.isEmpty()) Text("No issues", color = Color(0xFF6A9955))

        errors.forEach {
            Text(
                text = buildString {
                    append(it.severity)
                    append(": ")
                    append(it.message)
                    it.line?.let { l -> append(" (line $l)") }
                }, color = when (it.severity) {
                    LintError.Severity.ERROR -> Color(0xFFF44747)
                    else -> Color(0xFFFFC66D)
                }
            )
        }
    }
}
