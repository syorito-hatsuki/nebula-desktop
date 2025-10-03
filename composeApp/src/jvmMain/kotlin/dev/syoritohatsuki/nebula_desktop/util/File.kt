package dev.syoritohatsuki.nebula_desktop.util

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipInputStream
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

fun getNebulaBinDir(): Path = when {
    System.getProperty("os.name").lowercase().contains("win") -> Path.of(System.getenv("APPDATA"), "NebulaT ray", "bin")

    else -> Path.of(System.getProperty("user.home"), ".local", "share", "nebula-tray", "bin")
}

fun getNebulaBinaryPath(): Path = getNebulaBinDir().resolve(
    when {
        System.getProperty("os.name").lowercase().contains("win") -> "nebula.exe"
        else -> "nebula"
    }
)

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

fun chooseYamlFile(): File? {
    val chooser = JFileChooser().apply {
        dialogTitle = "Select Nebula Config"
        fileSelectionMode = JFileChooser.FILES_ONLY
        isAcceptAllFileFilterUsed = false
        fileFilter = FileNameExtensionFilter("YAML config", "yaml", "yml")
    }
    val result = chooser.showOpenDialog(null)
    return if (result == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
}