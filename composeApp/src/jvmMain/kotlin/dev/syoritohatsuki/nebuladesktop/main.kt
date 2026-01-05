package dev.syoritohatsuki.nebuladesktop

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.application
import com.kdroid.composetray.utils.SingleInstanceManager
import dev.syoritohatsuki.nebuladesktop.ui.MainTray
import dev.syoritohatsuki.nebuladesktop.ui.dialog.AdminRequiredWarning
import dev.syoritohatsuki.nebuladesktop.ui.dialog.NebulaDownloadDialog
import dev.syoritohatsuki.nebuladesktop.ui.window.main.MainWindow
import dev.syoritohatsuki.nebuladesktop.ui.window.main.MainWindowViewModel
import dev.syoritohatsuki.nebuladesktop.util.StorageManager
import dev.syoritohatsuki.nebuladesktop.util.focusAppWindow
import dev.syoritohatsuki.nebuladesktop.util.isRequireAdminRights
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext

const val MIN_WIDTH = 640
const val MIN_HEIGHT = 360

fun main() = application {
    var mainWindowRef by remember { mutableStateOf<ComposeWindow?>(null) }
    var isMainWindowVisible by remember { mutableStateOf(false) }
    var showTray by remember { mutableStateOf(false) }

    val mainWindowViewModel = remember { MainWindowViewModel }

    System.setProperty("apple.awt.application.appearance", "system")

    if (isRequireAdminRights()) {
        AdminRequiredWarning(::exitApplication)
        return@application
    }

    MainWindow(
        windowRef = { mainWindowRef = it },
        windowVisible = isMainWindowVisible,
        onCloseRequest = { isMainWindowVisible = false },
        mainWindowViewModel = mainWindowViewModel
    )

    when {
        !StorageManager.nebulaBinaryPath.toFile().exists() -> NebulaDownloadDialog(onClose = {
            showTray = true
        })

        else -> showTray = true
    }

    if (!SingleInstanceManager.isSingleInstance(onRestoreRequest = {
        isMainWindowVisible = true
        focusAppWindow(mainWindowRef)
    })) return@application exitApplication()

    MainTray(showTray, mainWindowViewModel, openWindow = {
        isMainWindowVisible = true
        focusAppWindow(mainWindowRef)
    })
}

suspend fun <T> runOnSwing(block: () -> T): T = withContext(Dispatchers.Swing) { block() }