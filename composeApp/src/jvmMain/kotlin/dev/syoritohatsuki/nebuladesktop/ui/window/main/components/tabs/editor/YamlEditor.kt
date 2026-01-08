package dev.syoritohatsuki.nebuladesktop.ui.window.main.components.tabs.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.syoritohatsuki.nebuladesktop.ui.TEXT_COLOR_SECONDARY
import dev.syoritohatsuki.nebuladesktop.util.editor.YamlHighlightTransformation


@Composable
fun YamlEditor(yamlEditorViewModel: YamlEditorViewModel, modifier: Modifier = Modifier) {
    val text by yamlEditorViewModel.text.collectAsState()
    val tokens by yamlEditorViewModel.tokens.collectAsState()

    val state = remember { TextFieldState(text) }
    val transformation = remember(tokens) { YamlHighlightTransformation(tokens) }

    LaunchedEffect(Unit) {
        snapshotFlow { state.text }.collect { yamlEditorViewModel.onTextChange(it as String) }
    }

    BasicTextField(
        state = state,
        modifier = modifier.fillMaxSize().background(Color(0xFF1E1E1E)).padding(12.dp),
        textStyle = TextStyle(
            fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = TEXT_COLOR_SECONDARY
        ),
        cursorBrush = SolidColor(Color.White),
        outputTransformation = transformation,
        scrollState = rememberScrollState()
    )
}