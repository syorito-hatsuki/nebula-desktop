package dev.syoritohatsuki.nebuladesktop

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
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

fun main() = application {

    var windowRef: ComposeWindow? = null

    val mainWindowViewModel = remember { MainWindowViewModel() }

    var windowVisible by remember { mutableStateOf(false) }
    var showTray by remember { mutableStateOf(false) }

    System.setProperty("apple.awt.application.appearance", "system")

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
            onCloseRequest = { showTray = true },
            title = "Nebula Downloader",
        ) {
            window.minimumSize = Dimension(MIN_WIDTH, MIN_HEIGHT)
            NebulaDownloadDialog(onClose = { this.window.dispose() })
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

fun focusAppWindow(window: ComposeWindow?) {
    if (window == null) return

    if (!window.isVisible) {
        window.isVisible = true
    }

    // On some platforms (esp. Linux/GNOME), this alone doesn’t work unless you also “de-iconify”
    if (window.state == Frame.ICONIFIED) {
        window.state = Frame.NORMAL
    }

    window.toFront()
    window.requestFocus()
}