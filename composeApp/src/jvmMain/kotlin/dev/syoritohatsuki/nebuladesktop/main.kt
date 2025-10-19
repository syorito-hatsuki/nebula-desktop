package dev.syoritohatsuki.nebuladesktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.kdroid.composetray.utils.SingleInstanceManager
import dev.syoritohatsuki.nebuladesktop.ui.MainTray
import dev.syoritohatsuki.nebuladesktop.ui.dialog.NebulaDownloadDialog
import dev.syoritohatsuki.nebuladesktop.ui.main.MainWindow
import dev.syoritohatsuki.nebuladesktop.ui.main.MainWindowViewModel
import dev.syoritohatsuki.nebuladesktop.util.StorageManager
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

//    if (operationSystem.contains("win")) {
//        Window(
//            onCloseRequest = ::exitApplication,
//            title = "Nebula Desktop - Admin Right Error",
//            state = rememberWindowState(size = DpSize(Dp.Unspecified, Dp.Unspecified)),
//        ) {
//            window.isResizable = false
//            Box(modifier = Modifier.padding(16.dp)) {
//                Text(
//                    text = "Nebula Desktop requires admin rights to run Please restart the application with admin rights."
//                )
//            }
//        }
//        return@application
//    }

    Window(
        icon = rememberVectorPainter(Icons.Filled.Shield),
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
            window.minimumSize = Dimension(MIN_WIDTH, MIN_HEIGHT)
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
    ProcessBuilder("net", "session").start().waitFor() == 0
} catch (_: Exception) {
    false
}