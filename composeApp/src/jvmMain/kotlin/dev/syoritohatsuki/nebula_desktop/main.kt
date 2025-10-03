package dev.syoritohatsuki.nebula_desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.kdroid.composetray.tray.api.Tray
import com.kdroid.composetray.utils.*
import dev.syoritohatsuki.nebula_desktop.ui.dialog.BootstrapDialog
import dev.syoritohatsuki.nebula_desktop.util.getNebulaBinaryPath
import nebula_desktop.composeapp.generated.resources.Res
import nebula_desktop.composeapp.generated.resources.compose_multiplatform

fun main() = application {
    if (!getNebulaBinaryPath().toFile().exists()) {
        Window(onCloseRequest = {}, title = "Nebula Bootstrap") {
            BootstrapDialog(onClose = { this.window.dispose() })
        }
    }

//    MainTray()

    allowComposeNativeTrayLogging = true
    composeNativeTrayLoggingLevel = ComposeNativeTrayLoggingLevel.DEBUG

    val logTag = "NativeTray"

    println("$logTag: TrayPosition: ${getTrayPosition()}")

    var isWindowVisible by remember { mutableStateOf(true) }
    var textVisible by remember { mutableStateOf(false) }
    var alwaysShowTray by remember { mutableStateOf(false) }
    var hideOnClose by remember { mutableStateOf(true) }

    val isSingleInstance = SingleInstanceManager.isSingleInstance(onRestoreRequest = {
        isWindowVisible = true
    })

    if (!isSingleInstance) {
        exitApplication()
        return@application
    }

    // Always create the Tray composable, but make it conditional on visibility
    // This ensures it's recomposed when alwaysShowTray changes
    val showTray = alwaysShowTray || !isWindowVisible

    var showAdvancedOptions by remember { mutableStateOf(true) }
    var dynamicItemLabel by remember { mutableStateOf("Dynamic Item") }
    var itemCounter by remember { mutableStateOf(0) }

    if (showTray) {

        Tray(
            iconContent = {
                // Use alwaysShowTray as a key to force recomposition when it changes
                val alpha = if (alwaysShowTray) 0.5f else 0.5f
                Box(
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(300.dp))
                        .background(Color.Red.copy(alpha = alpha))
                )
            },
            primaryAction = {
                isWindowVisible = true
                println("$logTag: On Primary Clicked")
            },
            tooltip = "My Application"
        ) {
            Item(label = dynamicItemLabel, icon = Icons.Filled.Adb) {
                itemCounter++
                dynamicItemLabel = "Clicked $itemCounter times"
                println("$logTag: Dynamic item clicked: $dynamicItemLabel")
            }

            Divider()

            // Options SubMenu
            SubMenu(label = "Options", icon = Icons.Default.Notifications) {
                Item(label = "Show Text") {
                    println("$logTag: Show Text selected")
                    textVisible = true
                }
                Item(label = "Hide Text") {
                    println("$logTag: Hide Text selected")
                    textVisible = false
                }

                // Conditionally show advanced options
                if (showAdvancedOptions) {
                    SubMenu(label = "Advanced Sub-options") {
                        Item(label = "Advanced Option 1") {
                            println("$logTag: Advanced Option 1 selected")
                        }
                        Item(label = "Advanced Option 2", icon = Icons.Default.ZoomOut) {
                            println("$logTag: Advanced Option 2 selected")
                        }
                    }
                }
            }

            CheckableItem(
                label = "Show advanced options",
                checked = showAdvancedOptions,
                onCheckedChange = { checked ->
                    showAdvancedOptions = checked
                    println("$logTag: Advanced options ${if (checked) "shown" else "hidden"}")
                }
            )


        }
    }

    Window(
        onCloseRequest = {
            if (hideOnClose) {
                isWindowVisible = false
            } else {
                exitApplication()
            }
        },
        title = "Compose Desktop Application with Two Screens",
        visible = isWindowVisible,
        icon = org.jetbrains.compose.resources.painterResource(Res.drawable.compose_multiplatform) // Optional: Set window icon
    ) {
        App(textVisible, alwaysShowTray, hideOnClose) { alwaysShow, hideOnCloseState ->
            alwaysShowTray = alwaysShow
            hideOnClose = hideOnCloseState
        }
    }

}