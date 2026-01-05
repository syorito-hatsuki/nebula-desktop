package dev.syoritohatsuki.nebuladesktop.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import dev.syoritohatsuki.nebuladesktop.MIN_HEIGHT
import dev.syoritohatsuki.nebuladesktop.MIN_WIDTH
import dev.syoritohatsuki.nebuladesktop.api.GithubApi
import dev.syoritohatsuki.nebuladesktop.network.downloadFile
import dev.syoritohatsuki.nebuladesktop.ui.*
import dev.syoritohatsuki.nebuladesktop.util.StorageManager.nebulaBinaryDirPath
import dev.syoritohatsuki.nebuladesktop.util.StorageManager.nebulaBinaryPath
import dev.syoritohatsuki.nebuladesktop.util.extractTarGz
import dev.syoritohatsuki.nebuladesktop.util.extractZip
import kotlinx.coroutines.launch
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import java.awt.Dimension

@Composable
fun NebulaDownloadDialog(onClose: () -> Unit) {
    var progressText by remember { mutableStateOf("Starting...") }
    var progress by remember { mutableStateOf(0.0) }
    var done by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var retryKey by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()

    val releaseNameForPlatform: String = when(hostOs) {
        OS.Windows -> "windows-amd64.zip"
        OS.MacOS -> "darwin.zip"
        OS.Linux -> "linux-amd64.tar.gz"
        else -> error("Unsupported OS")
    }

    LaunchedEffect(retryKey) {
        scope.launch {
            try {
                errorMessage = null
                done = false
                progress = 0.0
                progressText = "Fetching latest release..."

                val targetDir = nebulaBinaryDirPath
                targetDir.toFile().mkdirs()

                val release = GithubApi.fetchLatestRelease("slackhq/nebula")
                val platformAsset = release.assets.firstOrNull { it.name.contains(releaseNameForPlatform) }
                    ?: error("No suitable asset for ${releaseNameForPlatform}")

                val archiveFile = targetDir.resolve(platformAsset.name)
                progressText = "Downloading ${platformAsset.name}..."
                downloadFile(platformAsset.browserDownloadUrl, archiveFile) { p -> progress = p }

                progressText = "Extracting..."
                when {
                    platformAsset.name.endsWith(".zip") -> extractZip(archiveFile, targetDir) { p -> progress = p }
                    platformAsset.name.endsWith(".tar.gz") -> extractTarGz(archiveFile, targetDir) { p -> progress = p }
                }

                val binary = nebulaBinaryPath
                binary.toFile().setExecutable(true)

                progressText = "Done! Binary at $binary"
                done = true
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Error: ${e.message}"
            }
        }
    }

    Window(
        onCloseRequest = {},
        title = "Nebula Downloader",
    ) {
        window.isResizable = false
        window.maximumSize = Dimension(MIN_WIDTH / 2, MIN_HEIGHT / 2)

        Box(modifier = Modifier.fillMaxSize().background(color = BACKGROUND_COLOR)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(progressText, color = TEXT_COLOR)
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = progress.toFloat(),
                    color = PROGRESS_BAR_COLOR,
                    backgroundColor = PROGRESS_BAR_BACKGROUND_COLOR
                )

                if (errorMessage != null) {
                    Spacer(Modifier.height(16.dp))
                    Text(errorMessage!!, color = MaterialTheme.colors.error)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { retryKey++ },
                        colors = ButtonDefaults.buttonColors(containerColor = STOP_BUTTON_COLOR)
                    ) { Text("Retry") }
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onClose.also {
                        window.dispose()
                    },
                    enabled = done,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ADD_BUTTON_COLOR
                    )
                ) { Text("Close") }
            }
        }
    }
}