package dev.syoritohatsuki.nebula_desktop.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.syoritohatsuki.nebula_desktop.dto.github.GithubRelease
import dev.syoritohatsuki.nebula_desktop.util.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Composable
fun BootstrapDialog(onClose: () -> Unit) {
    var progressText by remember { mutableStateOf("Starting...") }
    var progress by remember { mutableStateOf(0.0) }
    var done by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var retryKey by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()

    fun startBootstrap() {
        scope.launch {
            try {
                errorMessage = null
                done = false
                progress = 0.0
                progressText = "Fetching latest release..."

                val targetDir = getNebulaBinDir()
                targetDir.toFile().mkdirs()

                val release = fetchLatestNebulaRelease()
                val platformAsset = release.assets.firstOrNull { it.name.contains(getPlatform()) }
                    ?: error("No suitable asset for ${getPlatform()}")

                val archiveFile = targetDir.resolve(platformAsset.name)
                progressText = "Downloading ${platformAsset.name}..."
                downloadFile(platformAsset.browserDownloadUrl, archiveFile) { p -> progress = p }

                progressText = "Extracting..."
                when {
                    platformAsset.name.endsWith(".zip") -> extractZip(archiveFile, targetDir) { p -> progress = p }
                    platformAsset.name.endsWith(".tar.gz") -> extractTarGz(archiveFile, targetDir) { p -> progress = p }
                }

                val binary = getNebulaBinaryPath()
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

suspend fun fetchLatestNebulaRelease(): GithubRelease = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
}.use {
    return it.get("https://api.github.com/repos/slackhq/nebula/releases/latest").body<GithubRelease>()
}