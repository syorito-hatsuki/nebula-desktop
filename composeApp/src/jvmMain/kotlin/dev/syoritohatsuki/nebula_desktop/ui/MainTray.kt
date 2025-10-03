package dev.syoritohatsuki.nebula_desktop.ui

import androidx.compose.runtime.*
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Tray
import dev.syoritohatsuki.nebula_desktop.util.chooseYamlFile
import nebula_desktop.composeapp.generated.resources.Res
import nebula_desktop.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource

@Composable
fun ApplicationScope.MainTray() {
    var isOpen by remember { mutableStateOf(true) }
    var active by remember { mutableStateOf(false) }

    if (isOpen) {
        Tray(icon = painterResource(Res.drawable.compose_multiplatform)) {
            Item(
                if (active) "Disable" else "Enable", onClick = {
                    active = !active
                    println(active)
                })
            Item(
                "Add Config",
                onClick = {
                    println(chooseYamlFile()?.readText() ?: "No file chosen")
                }
            )
            Separator()
            Item("Exit", onClick = ::exitApplication)
        }
    }
}