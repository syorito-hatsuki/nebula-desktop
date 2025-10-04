package dev.syoritohatsuki.nebula_desktop.util

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipInputStream

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