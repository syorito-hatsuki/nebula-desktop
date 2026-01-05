package dev.syoritohatsuki.nebuladesktop.ui.window.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.syoritohatsuki.nebuladesktop.ui.LOGS_BACKGROUND_COLOR
import dev.syoritohatsuki.nebuladesktop.ui.LOGS_BORDER_COLOR

@Composable
fun LogsView(logLines: SnapshotStateList<AnnotatedString>) {
    SelectionContainer(
        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)).background(LOGS_BACKGROUND_COLOR)
            .border(1.dp, LOGS_BORDER_COLOR, RoundedCornerShape(8.dp))
    ) {
        LazyColumn(reverseLayout = true) {
            items(logLines) {
                Text(
                    text = it,
                    color = Color.Unspecified,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                )
            }
        }
    }
}