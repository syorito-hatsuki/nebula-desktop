package dev.syoritohatsuki.nebuladesktop.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.syoritohatsuki.nebuladesktop.api.GithubApi
import dev.syoritohatsuki.nebuladesktop.network.downloadFile
import dev.syoritohatsuki.nebuladesktop.util.StorageManager.nebulaBinaryDirPath
import dev.syoritohatsuki.nebuladesktop.util.StorageManager.nebulaBinaryPath
import dev.syoritohatsuki.nebuladesktop.util.extractTarGz
import dev.syoritohatsuki.nebuladesktop.util.extractZip
import kotlinx.coroutines.launch

@Composable
fun NebulaDownloadDialog(onClose: () -> Unit) {
    var progressText by remember { mutableStateOf("Starting...") }
    var progress by remember { mutableStateOf(0.0) }
    var done by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var retryKey by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()

    fun startBootstrap() {

        fun getReleaseNameForPlatform(): String {
            val os = System.getProperty("os.name").lowercase()
            return when {
                os.contains("win") -> "windows-amd64.zip"
                os.contains("mac") -> "darwin.zip"
                os.contains("nux") || os.contains("linux") -> "linux-amd64.tar.gz"
                else -> error("Unsupported OS")
            }
        }

        scope.launch {
            try {
                errorMessage = null
                done = false
                progress = 0.0
                progressText = "Fetching latest release..."

                val targetDir = nebulaBinaryDirPath
                targetDir.toFile().mkdirs()

                val release = GithubApi.fetchLatestRelease("slackhq/nebula")
                val platformAsset = release.assets.firstOrNull { it.name.contains(getReleaseNameForPlatform()) }
                    ?: error("No suitable asset for ${getReleaseNameForPlatform()}")

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

    LaunchedEffect(retryKey) { startBootstrap() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(progressText)
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(progress = progress.toFloat(), modifier = Modifier.fillMaxWidth())

        if (errorMessage != null) {
            Spacer(Modifier.height(16.dp))
            Text(errorMessage!!, color = MaterialTheme.colors.error)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { retryKey++ }) { Text("Retry") }
        }

        if (done) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onClose) { Text("Close") }
        }
    }
}