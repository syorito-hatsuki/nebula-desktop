package dev.syoritohatsuki.nebula_desktop

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.awt.*
import java.awt.image.BufferedImage
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipInputStream
import kotlin.system.exitProcess

@Serializable
data class GithubReleaseAsset(val name: String, val browser_download_url: String)

@Serializable
data class GithubRelease(val tag_name: String, val assets: List<GithubReleaseAsset>)

fun getPlatform(): String {
    val os = System.getProperty("os.name").lowercase()
    val arch = System.getProperty("os.arch").lowercase()
    return when {
        os.contains("win") -> "windows-amd64.zip"
        os.contains("mac") -> if (arch.contains("aarch64") || arch.contains("arm")) "darwin-arm64.zip" else "darwin-amd64.zip"
        os.contains("nux") || os.contains("linux") -> "linux-amd64.tar.gz"
        else -> error("Unsupported OS")
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

suspend fun downloadFile(url: String, target: Path, onProgress: (Double) -> Unit) {
    val client = HttpClient(CIO)
    val response = client.get(url)
    val total = response.contentLength() ?: -1L
    var downloaded = 0L

    response.bodyAsChannel().let { channel ->
        FileOutputStream(target.toFile()).use { fos ->
            val buffer = ByteArray(8192)
            val byteBuffer = ByteBuffer.wrap(buffer)
            while (!channel.isClosedForRead) {
                byteBuffer.clear()
                val read = channel.readAvailable(byteBuffer)
                if (read == -1) break
                fos.write(buffer, 0, read)
                downloaded += read
                if (total > 0) onProgress(downloaded.toDouble() / total)
            }
        }
    }
    client.close()
}

fun extractZip(zipFile: Path, targetDir: Path, onProgress: (Double) -> Unit) {
    val zis = ZipInputStream(Files.newInputStream(zipFile))
    val entries = mutableListOf<java.util.zip.ZipEntry>()
    var entry = zis.nextEntry
    while (entry != null) {
        entries.add(entry)
        entry = zis.nextEntry
    }
    zis.close()

    var count = 0
    val zis2 = ZipInputStream(Files.newInputStream(zipFile))
    var e = zis2.nextEntry
    while (e != null) {
        val outFile = targetDir.resolve(e.name).toFile()
        if (e.isDirectory) {
            outFile.mkdirs()
        } else {
            outFile.parentFile.mkdirs()
            FileOutputStream(outFile).use { fos ->
                zis2.copyTo(fos)
            }
        }
        count++
        onProgress(count.toDouble() / entries.size)
        e = zis2.nextEntry
    }
    zis2.close()
}

// TAR.GZ extraction
fun extractTarGz(tarGzFile: Path, targetDir: Path, onProgress: (Double) -> Unit) {
    val fis = FileInputStream(tarGzFile.toFile())
    val gzis = GzipCompressorInputStream(fis)
    val tais = TarArchiveInputStream(gzis)
    val entries = mutableListOf<org.apache.commons.compress.archivers.tar.TarArchiveEntry>()
    var entry = tais.nextTarEntry
    while (entry != null) {
        entries.add(entry)
        entry = tais.nextTarEntry
    }
    tais.close()
    fis.close()

    // Reopen stream to extract
    val fis2 = FileInputStream(tarGzFile.toFile())
    val gzis2 = GzipCompressorInputStream(fis2)
    val tais2 = TarArchiveInputStream(gzis2)
    var e2 = tais2.nextTarEntry
    var count = 0
    while (e2 != null) {
        val outFile = targetDir.resolve(e2.name).toFile()
        if (e2.isDirectory) {
            outFile.mkdirs()
        } else {
            outFile.parentFile.mkdirs()
            BufferedOutputStream(FileOutputStream(outFile)).use { bos ->
                tais2.copyTo(bos)
            }
        }
        count++
        onProgress(count.toDouble() / entries.size)
        e2 = tais2.nextTarEntry
    }
    tais2.close()
    gzis2.close()
    fis2.close()
}

// Tray App
fun startTrayApp() {
    if (!SystemTray.isSupported()) {
        println("System tray not supported on this platform.")
        return
    }

    val tray = SystemTray.getSystemTray()
    val iconOff = createTrayImage(Color.RED)
    val iconOn = createTrayImage(Color.GREEN)
    var active = false

    val trayIcon = TrayIcon(iconOff, "Nebula Tray")
    trayIcon.isImageAutoSize = true

    val popup = PopupMenu()

    val toggleItem = MenuItem("Toggle ON/OFF")
    toggleItem.addActionListener {
        active = !active
        trayIcon.image = if (active) iconOn else iconOff
        // TODO: launch/stop Nebula binary if needed
    }
    popup.add(toggleItem)

    val quitItem = MenuItem("Quit")
    quitItem.addActionListener {
        tray.remove(trayIcon)
        exitProcess(0)
    }
    popup.addSeparator()
    popup.add(quitItem)

    trayIcon.popupMenu = popup
    tray.add(trayIcon)

    trayIcon.addActionListener { // click toggles ON/OFF
        active = !active
        trayIcon.image = if (active) iconOn else iconOff
        // TODO: launch/stop Nebula
    }
}

// Simple colored tray icon for demonstration
fun createTrayImage(color: Color): Image {
    val img = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
    val g = img.graphics
    g.color = color
    g.fillRect(0, 0, 16, 16)
    g.dispose()
    return img
}

// Helpers to detect paths
fun getNebulaBinDir(): Path = when {
    System.getProperty("os.name").lowercase().contains("win") -> Path.of(System.getenv("APPDATA"), "NebulaTray", "bin")

    else -> Path.of(System.getProperty("user.home"), ".local", "share", "nebula-tray", "bin")
}

fun getNebulaBinaryPath(): Path {
    val dir = getNebulaBinDir()
    val binName = if (System.getProperty("os.name").lowercase().contains("win")) "nebula.exe" else "nebula"
    return dir.resolve(binName)
}

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
                downloadFile(platformAsset.browser_download_url, archiveFile) { p -> progress = p }

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

fun main() = application {
    val nebulaBinary = getNebulaBinaryPath()

    if (!nebulaBinary.toFile().exists()) {
        Window(onCloseRequest = {}, title = "Nebula Bootstrap") {
            val currentWindow = this.window
            BootstrapDialog(onClose = { currentWindow.dispose() })
        }
    }

    startTrayApp()
}