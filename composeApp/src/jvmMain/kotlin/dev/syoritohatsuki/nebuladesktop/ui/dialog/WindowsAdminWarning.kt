package dev.syoritohatsuki.nebuladesktop.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.syoritohatsuki.nebuladesktop.ui.BACKGROUND_COLOR
import dev.syoritohatsuki.nebuladesktop.ui.TEXT_COLOR

@Composable
fun WindowsAdminWarning() {
    Box(modifier = Modifier.fillMaxSize().background(color = BACKGROUND_COLOR)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = buildString {
                    append("Nebula Desktop requires admin rights to manage network connections\n")
                    append("Please restart the application with admin rights.")
                },
                color = TEXT_COLOR,
            )
        }
    }
}