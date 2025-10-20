package dev.syoritohatsuki.nebuladesktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.kdroid.composetray.utils.SingleInstanceManager
import dev.syoritohatsuki.nebuladesktop.ui.BACKGROUND_COLOR
import dev.syoritohatsuki.nebuladesktop.ui.MainTray
import dev.syoritohatsuki.nebuladesktop.ui.TEXT_COLOR
import dev.syoritohatsuki.nebuladesktop.ui.dialog.NebulaDownloadDialog
import dev.syoritohatsuki.nebuladesktop.ui.main.MainWindow
import dev.syoritohatsuki.nebuladesktop.ui.main.MainWindowViewModel
import dev.syoritohatsuki.nebuladesktop.util.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import nebula_desktop.composeapp.generated.resources.Res
import nebula_desktop.composeapp.generated.resources.icon
import org.jetbrains.compose.resources.painterResource
import java.awt.Dimension
import java.awt.Frame

const val MIN_WIDTH = 640
const val MIN_HEIGHT = 360

val operationSystem = System.getProperty("os.name").lowercase()

fun main() = application {
    var windowRef: ComposeWindow? = null

    val mainWindowViewModel = remember { MainWindowViewModel }

    var windowVisible by remember { mutableStateOf(false) }
    var showTray by remember { mutableStateOf(false) }

    System.setProperty("apple.awt.application.appearance", "system")

    if (operationSystem.contains("win") && !isRunningAsAdmin()) {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Nebula Desktop - Admin Right Error",
        ) {
            window.isResizable = false
            window.minimumSize = Dimension(MIN_WIDTH / 2, MIN_HEIGHT / 2)

            Box(modifier = Modifier.fillMaxSize().background(color = BACKGROUND_COLOR)) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Nebula Desktop requires admin rights to run Please restart the application with admin rights.",
                        color = TEXT_COLOR,
                    )
                }
            }
        }
        return@application
    }

    Window(
        icon = painterResource(Res.drawable.icon),
        onCloseRequest = { windowVisible = false },
        title = "Nebula Desktop",
        visible = windowVisible,
    ) {
        window.minimumSize = Dimension(MIN_WIDTH, MIN_HEIGHT)
        MainWindow(mainWindowViewModel)
        windowRef = window
    }

    when {
        !StorageManager.nebulaBinaryPath.toFile().exists() -> Window(
            onCloseRequest = {},
            title = "Nebula Downloader",
        ) {
            window.isResizable = false
            window.maximumSize = Dimension(MIN_WIDTH / 2, MIN_HEIGHT / 2)

            NebulaDownloadDialog(onClose = {
                this.window.dispose()
                showTray = true
            })
        }

        else -> showTray = true
    }

    val isSingleInstance = SingleInstanceManager.isSingleInstance(onRestoreRequest = {
        windowVisible = true
        focusAppWindow(windowRef)
    })

    if (!isSingleInstance) {
        exitApplication()
        return@application
    }

    if (showTray) {
        MainTray(mainWindowViewModel, openWindow = {
            windowVisible = true
            focusAppWindow(windowRef)
        })
    }
}

private fun focusAppWindow(window: ComposeWindow?) {
    if (window == null) return
    if (!window.isVisible) window.isVisible = true
    if (window.state == Frame.ICONIFIED) window.state = Frame.NORMAL

    window.toFront()
    window.requestFocus()
}

fun isRunningAsAdmin(): Boolean = try {
    ProcessBuilder(
        "powershell",
        "-Command",
        "[bool]([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)"
    ).start().inputStream.bufferedReader().readText().trim().equals("True", ignoreCase = true)
} catch (_: Exception) {
    false
}

suspend fun <T> runOnSwing(block: () -> T): T = withContext(Dispatchers.Swing) { block() }